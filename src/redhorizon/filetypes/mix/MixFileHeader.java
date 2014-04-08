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

package redhorizon.filetypes.mix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Representation of a Red Alert MIX file header, which contains information
 * about the mix file and it's contents.
 * 
 * @author Emanuel Rabina
 */
public class MixFileHeader {

	static final int HEADER_SIZE = 6;

	short numfiles;
	int bodylength; // seems like ignored

	/**
	 * Constructor, takes a <tt>ByteBuffer</tt> and assigns the bytes to the
	 * variables of the header.
	 * 
	 * @param bytes A wrapped array of bytes which consists of the header.
	 */
	MixFileHeader(ByteBuffer bytes) {
		numfiles   = bytes.getShort();
		bodylength = bytes.getInt();
		
		System.out.println("Mix header: " + numfiles + " | " + bodylength);
	}
}
