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

/**
 * Interface which identifies the file format as using indexed data to represent
 * the image information returned by the implementing class.  Thus requiring a
 * matching external palette to obtain the whole image.
 * <p>
 * A class that implements this interface redefines the meaning of any methods
 * that retrieve the images (eg: <tt>get*()</tt> methods) as those will now just
 * return the raw indexed image data.
 * <p>
 * A file format which contains an internal palette is not considered
 * <tt>Paletted</tt>.
 * 
 * @author Emanuel Rabina
 */
public interface Paletted extends ImageCommon {

}
