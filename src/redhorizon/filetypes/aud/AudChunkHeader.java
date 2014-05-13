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

package redhorizon.filetypes.aud;

import java.nio.ByteBuffer;

/**
 * Representation of a chunk header in an AUD file.
 * 
 * @author Emanuel Rabina.
 */
public class AudChunkHeader {

//	private static final int CHUNK_ID = 0x0000deaf;

	static final int CHUNK_HEADER_SIZE = 8;

	// Chunk header data
	final short filesize;
	final short datasize;
	final int id;

	/**
	 * Constructor, assigns the variables of this chunk with the given bytes.
	 * 
	 * @param bytes Aud file data.
	 */
	AudChunkHeader(ByteBuffer bytes) {

		filesize = bytes.getShort();
		datasize = bytes.getShort();
		id       = bytes.getInt();
	}
}
