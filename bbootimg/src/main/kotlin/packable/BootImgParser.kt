// Copyright 2021 yuyezhong@gmail.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package cfig.packable

import avb.blob.Footer
import cfig.Avb
import cfig.bootimg.Common.Companion.probeHeaderVersion
import cfig.bootimg.v2.BootV2
import cfig.bootimg.v3.BootV3
import com.fasterxml.jackson.databind.ObjectMapper
import de.vandermeer.asciitable.AsciiTable
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

class BootImgParser : IPackable {
    override val loopNo: Int
        get() = 0

    override fun capabilities(): List<String> {
        return listOf("^boot(-debug)?\\.img$", "^recovery\\.img$", "^recovery-two-step\\.img$")
    }

    override fun unpack(fileName: String) {
        cleanUp()
        val hv = probeHeaderVersion(fileName)
        log.info("header version $hv")
        if (hv in 0..2) {
            val b2 = BootV2
                .parse(fileName)
                .extractImages()
                .extractVBMeta()
                .printSummary()
            log.debug(b2.toString())
        } else {
            val b3 = BootV3
                .parse(fileName)
                .extractImages()
                .extractVBMeta()
                .printSummary()
            log.debug(b3.toString())
        }
    }

    override fun pack(fileName: String) {
        val cfgFile = outDir + fileName.removeSuffix(".img") + ".json"
        log.info("Loading config from $cfgFile")
        if (!File(cfgFile).exists()) {
            val tab = AsciiTable().let {
                it.addRule()
                it.addRow("'$cfgFile' doesn't exist, did you forget to 'unpack' ?")
                it.addRule()
                it
            }
            log.info("\n{}", tab.render())
            return
        }
        when (val hv = probeHeaderVersion(fileName)) {
            0, 1, 2 ->
                ObjectMapper().readValue(File(cfgFile), BootV2::class.java)
                    .pack()
                    .sign()
                    .updateVbmeta()
                    .printPackSummary()
            3, 4 ->
                ObjectMapper().readValue(File(cfgFile), BootV3::class.java)
                    .pack()
                    .sign(fileName)
                    .updateVbmeta()
                    .printPackSummary()
            else -> throw IllegalArgumentException("do not support header version $hv")
        }
    }

    override fun flash(fileName: String, deviceName: String) {
        val stem = fileName.substring(0, fileName.indexOf("."))
        super.flash("$fileName.signed", stem)

        if (File("vbmeta.img.signed").exists()) {
            super.flash("vbmeta.img.signed", "vbmeta")
        }
    }

    // invoked solely by reflection
    fun `@footer`(image_file: String) {
        FileInputStream(image_file).use { fis ->
            fis.skip(File(image_file).length() - Footer.SIZE)
            try {
                val footer = Footer(fis)
                log.info("\n" + ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(footer))
            } catch (e: IllegalArgumentException) {
                log.info("image $image_file has no AVB Footer")
            }
        }
    }

    override fun `@verify`(fileName: String) {
        super.`@verify`(fileName)
    }

    override fun pull(fileName: String, deviceName: String) {
        super.pull(fileName, deviceName)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BootImgParser::class.java)
    }
}
