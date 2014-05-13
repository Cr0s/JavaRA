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

package redhorizon.filetypes;

import redhorizon.media.Palette;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface to expose an image file's internal palette and raw (indexed) image
 * data.
 * 
 * @author Emanuel Rabina
 */
public interface PalettedInternal {

	/**
	 * Retrieves the internal palette.
	 * 
	 * @return An image file's internal palette.
	 */
	public Palette getPalette();

	/**
	 * Returns the raw indexed data which constructs this file's image.  The
	 * returned data is not a copy, and so any changes to the returned data will
	 * affect this image's data.
	 * 
	 * @return The indexed image data.
	 */
	public ReadableByteChannel getRawImageData();
}
