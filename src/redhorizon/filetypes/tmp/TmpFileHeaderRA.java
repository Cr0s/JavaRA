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

package redhorizon.filetypes.tmp;

import java.nio.ByteBuffer;

/**
 * Representation of the header used in Red Alert's map tile files.
 * 
 * @author Emanuel Rabina
 */
public class TmpFileHeaderRA {

	static final int HEADER_SIZE = 40;

	short width;
	short height;
	short numtiles;
	//final short unknown1;
	short tilewidth;
	short tileheight;
	//final int filesize;
	int imagedata;
	//final int unknown2;
	//final int unknown3;
	int index2;
	//final int unknown4;
	int index1;

	/**
	 * Constructor, generates header data from the given <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing RA template header data.
	 */
	TmpFileHeaderRA() {
	}
}
