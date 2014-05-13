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
 * Representation of the header section of an AUD file, which contains data on
 * the AUD file and it's contents.
 * 
 * @author Emanuel Rabina
 */
public class AudFileHeader {

	static final int HEADER_SIZE = 12;

	final short frequency;
	final int filesize;
	final int datasize;
	final byte flags;
	final byte type;

	/**
	 * Constructor, assigns the variables of the header using the bytes from the
	 * given {@link ByteBuffer}.
	 * 
	 * @param bytes Aud file header data.
	 */
	AudFileHeader(ByteBuffer bytes) {

		frequency = bytes.getShort();
		filesize  = bytes.getInt();
		datasize  = bytes.getInt();
		flags     = bytes.get();
		type      = bytes.get();
	}
}
