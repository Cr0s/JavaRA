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

package redhorizon.filetypes.vqa;

import java.nio.ByteBuffer;

/**
 * Representation of the VQA file's lookup table.  In the first chunk, there is
 * 1 complete lookup table, but in subsequent ones only fractions of the table
 * are available.  A new one is created once all these fractions add to a new
 * table, which should then replace the previous one.
 * 
 * @author Emanuel Rabina
 */
public class VqaTable {

	final ByteBuffer table;

	/**
	 * Constructor, builds an initial lookup table of the given dimensions.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing a complete lookup table.
	 */
	VqaTable(ByteBuffer bytes) {

		table = bytes;
	}

	/**
	 * Returns the value at the given position in the lookup table.
	 * 
	 * @param index Position in the lookup table.
	 * @return The value at the given position.
	 */
	byte getValueAt(int index) {

		return table.get(index);
	}
}
