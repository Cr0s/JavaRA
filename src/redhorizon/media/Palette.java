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

package redhorizon.media;

import redhorizon.filetypes.ColourFormat;

/**
 * Interface for colour palettes.
 * 
 * @author Emanuel Rabina
 */
public interface Palette {

	/**
	 * Colour format used by this palette.
	 * 
	 * @return Palette colour format, RGB(A).
	 */
	public ColourFormat format();

	/**
	 * Return the colour data at the specified index.
	 * 
	 * @param index Position in the palette.
	 * @return <tt>byte</tt> array of the RGB(A) values of the requested colour.
	 */
	public byte[] getColour(int index);

	/**
	 * The number of colours in the palette.
	 * 
	 * @return Number of colours.
	 */
	public int size();
}
