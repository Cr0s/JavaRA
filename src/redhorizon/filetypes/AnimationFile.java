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
 * Interface for animation filetypes.
 *
 * @author Emanuel Rabina
 */
public interface AnimationFile extends ImagesFile {

	/**
	 * Returns the adjustment factor for the height of the animation.  Used
	 * mainly for older file formats of C&C where the display was either 320x200
	 * or 640x400 (a 8:5 or 1.6 pixel ratio).
	 * 
	 * @return A value to multiply the height of the animation by to get the
	 * 		   right look and feel.  Returns 1 if no adjustment is necessary.
	 */
	public float adjustmentFactor();

	/**
	 * Returns the speed at which this animation should be run, in
	 * frames/second.
	 * 
	 * @return Animation speed, as the frames component of frames/second.
	 */
	public float frameRate();
}
