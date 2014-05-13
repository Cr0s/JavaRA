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

import redhorizon.filetypes.AnimationFile;
import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.PalettedInternal;
import redhorizon.utilities.ImageUtility;
import static redhorizon.filetypes.ColourFormat.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Abstract WSA file class containing only the parts similar between both C&C
 * WSA files and Dune 2 WSA files.
 * 
 * @author Emanuel Rabina
 * @param <H> WSA header implementation type.
 */
public abstract class WsaFile<H extends WsaFileHeader> extends AbstractFile implements AnimationFile {

	// Save-to information
	static final String PARAM_WIDTH     = "-w:";
	static final String PARAM_HEIGHT    = "-h:";
	static final String PARAM_NUMIMGS   = "-n:";
	static final String PARAM_FRAMERATE = "-f:";
	static final String PARAM_LOOPING   = "-loop";

	static final int MAX_WIDTH   = 65535;
	static final int MAX_HEIGHT  = 65535;
	static final int MAX_NUMIMGS = 65534;	// -1 to make room for loop frame

	H wsaheader;
	int[] wsaoffsets;
	ByteBuffer[] wsaframes;

	/**
	 * Constructor, creates a new WSA file with the given name.
	 * 
	 * @param name The name of this file.
	 */
	WsaFile(String name) {

		super(name);
	}

	/**
	 * Constructor, builds this file from an existing {@link ImageFile}.
	 * 
	 * @param name		Name of this file.
	 * @param imagefile File to source data from.
	 * @param params	Additonal parameters: width, height, numimgs, framerate,
	 * 					looping.
	 */
	WsaFile(String name, ImageFile imagefile, String... params) {

		super(name);

		int width = -1;
		int height = -1;
		int numimgs = -1;
		float framerate = -1f;
		boolean looping = false;

		// Grab the parameters
		for (String param: params) {
			if (param.startsWith(PARAM_WIDTH)) {
				width = Integer.parseInt(param.substring(PARAM_WIDTH.length()));
			}
			else if (param.startsWith(PARAM_HEIGHT)) {
				height = Integer.parseInt(param.substring(PARAM_HEIGHT.length()));
			}
			else if (param.startsWith(PARAM_NUMIMGS)) {
				numimgs = Integer.parseInt(param.substring(PARAM_NUMIMGS.length()));
			}
			else if (param.startsWith(PARAM_FRAMERATE)) {
				framerate = Float.parseFloat(param.substring(PARAM_FRAMERATE.length()));
			}
			else if (param.equals(PARAM_LOOPING)) {
				looping = true;
			}
		}

		// Ensure each parameter was filled
		if (width == -1 || height == -1 || numimgs == -1 || framerate == -1f) {
			throw new IllegalArgumentException();
		}

		// Add loop frame if applicable
		if (looping) {
			numimgs++;
		}

		// Check source will 'fit' into WSA file
		String sourcename = imagefile.getFileName();
		String sourceclass = getClass().getSimpleName();

		ImageUtility.checkSize(sourcename, sourceclass, width, height, MAX_WIDTH, MAX_HEIGHT);
		ImageUtility.checkNumImages(sourcename, sourceclass, numimgs, MAX_NUMIMGS);
		ImageUtility.checkPaletted(sourcename, sourceclass, imagefile);

		// Build file
		ByteBuffer[] frames = imagefile instanceof PalettedInternal ?
				ImageUtility.splitImage(width, height, numimgs, imagefile.width(),
					imagefile.height(), FORMAT_INDEXED, ((PalettedInternal)imagefile).getRawImageData()):
				ImageUtility.splitImage(width, height, numimgs, imagefile);
		buildFile(width, height, framerate, looping, frames);
	}

	/**
	 * Constructor, builds this file from a series of {@link ImageFile}s.
	 * 
	 * @param name		 Name of this file.
	 * @param imagefiles File to source data from.
	 * @param params	 Additonal parameters: framerate, looping, nohires.
	 */
	WsaFile(String name, ImageFile[] imagefiles, String... params) {

		super(name);

		float framerate = -1f;
		boolean looping = false;

		// Grab the parameters
		for (String param: params) {
			if (param.startsWith(PARAM_FRAMERATE)) {
				framerate = Float.parseFloat(param.substring(PARAM_FRAMERATE.length()));
			}
			else if (param.equals(PARAM_LOOPING)) {
				looping = true;
			}
		}

		// Ensure each parameter was filled
		if (framerate == -1f) {
			throw new IllegalArgumentException();
		}

		// Check source will 'fit' into WSA file
		String sourceclass = getClass().getSimpleName();
		ImageUtility.checkNumImages("(list of images)", sourceclass, looping ? imagefiles.length - 1 :
				imagefiles.length, MAX_NUMIMGS);
		ImageUtility.checkConsistentImages(imagefiles);
		for (ImageFile imagefile: imagefiles) {
			String sourcename = imagefile.getFileName();
			ImageUtility.checkSize(sourcename, sourceclass, imagefile.width(), imagefile.height(),
					MAX_WIDTH, MAX_HEIGHT);
			ImageUtility.checkPaletted(sourcename, sourceclass, imagefile);
		}

		// Build file
		ByteBuffer[] images = new ByteBuffer[imagefiles.length];
		for (int i = 0; i < images.length; i++) {
			ImageFile imagefile = imagefiles[i];
			ByteBuffer imagedata = ByteBuffer.allocate(imagefile.width() * imagefile.height() *
					imagefile.format().size);
			try {
				(imagefile instanceof PalettedInternal ?
						((PalettedInternal)imagefile).getRawImageData() : imagefile.getImageData())
						.read(imagedata);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			images[i] = (ByteBuffer)imagedata.rewind();
		}
		buildFile(imagefiles[0].width(), imagefiles[0].height(), framerate, looping, images);
	}

	/**
	 * Constructs this file from the parts of various other objects.
	 * 
	 * @param width		Width of each frame.
	 * @param height	Height of each frame.
	 * @param framerate Framerate of the animation.
	 * @param looping	Whether the animation loops or not.
	 * @param frames	Array of frame data.
	 */
	abstract void buildFile(int width, int height, float framerate, boolean looping, ByteBuffer[] frames);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ColourFormat format() {

		return FORMAT_RGB;
	}

	/**
	 * Returns whether or not this animation loops.
	 * 
	 * @return <tt>true</tt> if the animation loops, <tt>false</tt> otherwise.
	 */
	public boolean isLooping() {

		return wsaoffsets[wsaheader.numframes + 1] != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int numImages() {

		return wsaheader.numframes & 0xffff;
	}
}
