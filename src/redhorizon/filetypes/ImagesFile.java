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

import java.nio.channels.ReadableByteChannel;

/**
 * Interface for files which contain multiple images.  These form the basis for
 * animation filetypes, as well as files which just contain several images to
 * represent a whole (eg: map tiles).
 * 
 * @author Emanuel Rabina
 */
public interface ImagesFile extends ImageCommon, File {

	/**
	 * Returns the image data for all of the images in this file.
	 * 
	 * @return Image data for each image.
	 */
	public ReadableByteChannel getImagesData();

	/**
	 * Returns the number of images in this file.
	 *
	 * @return Number of images.
	 */
	public int numImages();
}
