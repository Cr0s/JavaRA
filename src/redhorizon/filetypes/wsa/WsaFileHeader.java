/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package redhorizon.filetypes.wsa;

import java.nio.ByteBuffer;

/**
 * Abstract WSA file header class containing only the parts similar between both
 * C&C WSA file and Dune 2 WSA file headers.
 * 
 * @author Emanuel Rabina
 */
public abstract class WsaFileHeader {

	final short numframes;

	/**
	 * Constructor, fills-out the common number-of-frames and offsets part.
	 * 
	 * @param numframes The number of frames the WSA file contains.
	 */
	WsaFileHeader(short numframes) {

		this.numframes = numframes;
	}

	/**
	 * Returns this header in it's {@link ByteBuffer} representation.
	 * 
	 * @return {@link ByteBuffer} containing this header's data.
	 */
	abstract ByteBuffer toByteBuffer();
}
