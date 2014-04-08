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

import redhorizon.media.AbstractPalette;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.nio.ByteBuffer;

/**
 * Dynamic palette found within a vqa file.
 * 
 * @author Emanuel Rabina
 */
public class VqaPalette extends AbstractPalette {

	/**
	 * Constructor, builds this palette.
	 * 
	 * @param size
	 * @param bytes Buffer containing the palette data.
	 */
	VqaPalette(int size, ByteBuffer bytes) {

		super(size, FORMAT_RGB, bytes);
	}
}
