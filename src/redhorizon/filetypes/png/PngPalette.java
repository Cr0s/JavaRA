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

package redhorizon.filetypes.png;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.pal.PalFile;
import redhorizon.media.AbstractPalette;
import redhorizon.media.Palette;

/**
 * Palette internal to a png file.
 * 
 * @author Emanuel Rabina
 */
public class PngPalette extends AbstractPalette {

	/**
	 * Constructor, creates a png file internal palette.
	 * 
	 * @param format RGB(A) type of this palette.
	 * @param bytes Byte array containing the palette data.
	 */
	PngPalette(ColourFormat format, byte[][] bytes) {

		super(256, format, bytes);
	}

	/**
	 * Constructor, create a new palette from a VGA palette file.
	 * 
	 * @param palettefile
	 */
	PngPalette(PalFile palettefile) {

		super(palettefile);
	}

	/**
	 * Constructor, create a palette from another palette.
	 * 
	 * @param palette
	 */
	PngPalette(Palette palette) {

		super(palette);
	}
}
