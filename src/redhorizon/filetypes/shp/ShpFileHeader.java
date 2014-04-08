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

package redhorizon.filetypes.shp;

import java.nio.ByteBuffer;

/**
 * Abstract SHP file header class containing only the parts similar between both
 * C&C SHP file and Dune 2 SHP file headers.
 * 
 * @author Emanuel Rabina
 */
public abstract class ShpFileHeader {

	final short numimages;

	/**
	 * Constructor, fills-out the common number-of-images part.
	 * 
	 * @param numimages The number of frames the SHP file contains.
	 */
	ShpFileHeader(short numimages) {

		this.numimages = numimages;
	}

	/**
	 * Returns this header in it's <tt>ByteBuffer</tt> representation.
	 * 
	 * @return <tt>ByteBuffer</tt> containing this header's data.
	 */
	abstract ByteBuffer toByteBuffer();
}
