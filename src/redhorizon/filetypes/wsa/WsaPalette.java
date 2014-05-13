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

import redhorizon.media.AbstractPalette;
import redhorizon.media.Palette;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.nio.ByteBuffer;

/**
 * Palette internal to a wsa file.
 * 
 * @author Emanuel Rabina
 */
public class WsaPalette extends AbstractPalette {

	/**
	 * Constructor, creates a wsa file internal palette.
	 * 
	 * @param bytes Buffer containing the palette data.
	 */
	WsaPalette(ByteBuffer bytes) {

		super(256, FORMAT_RGB, bytes);
	}

	/**
	 * Constructor, copy an existing palette.
	 * 
	 * @param palette
	 */
	WsaPalette(Palette palette) {

		super(palette);
	}
}
