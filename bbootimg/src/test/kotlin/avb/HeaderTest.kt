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

package avb

import avb.blob.Header
import org.apache.commons.codec.binary.Hex
import org.junit.Test
import java.io.ByteArrayInputStream

class HeaderTest {

    @Test
    fun readHeader() {
        val vbmetaHeaderStr = "4156423000000001000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c8000000000000000000000000000000c80000000000000000000000000000000000000000000000c800000000000000000000000000000000617662746f6f6c20312e312e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
        val header2 = Header(ByteArrayInputStream(Hex.decodeHex(vbmetaHeaderStr)))
        println(header2.toString())
    }
}
