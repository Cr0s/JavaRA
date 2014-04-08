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

package redhorizon.filetypes.shp;

import redhorizon.filetypes.AnimationFile;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.PalettedInternal;
import redhorizon.filetypes.WritableFile;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.ImageUtility;
import static redhorizon.filetypes.ColourFormat.FORMAT_INDEXED;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Implementation of a C&C SHP file.  SHP files are the graphics format for
 * almost everything in the game.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("shp")
@FileType(ImagesFile.class)
public class ShpFileCnc extends ShpFile<ShpFileHeaderCnc> implements AnimationFile, WritableFile {

	// Read-from information
	private static final byte FORMAT20 = (byte)0x20;
	private static final byte FORMAT40 = (byte)0x40;
	private static final byte FORMAT80 = (byte)0x80;

	// Save-to information
	private static final int MAX_HEIGHT = 65535;

	/**
	 * Constructor, creates a new shp file with the given name and file data.
	 * 
	 * @param name		  The name of this file.
	 * @param bytechannel Data of the file.
	 */
	public ShpFileCnc(String name, ReadableByteChannel bytechannel) {

		super(name);

		try {
			// Construct file header
			ByteBuffer headerbytes = ByteBuffer.allocate(ShpFileHeaderCnc.HEADER_SIZE);
			headerbytes.order(ByteOrder.LITTLE_ENDIAN);
			bytechannel.read(headerbytes);
			headerbytes.rewind();
			
			shpfileheader = new ShpFileHeaderCnc(headerbytes);
			System.out.println("NumImages: " + shpfileheader.numimages + " | Width: " + shpfileheader.width + " | Height: " + shpfileheader.height);
			
			ShpImageOffsetCnc[] offsets = new ShpImageOffsetCnc[numImages() + 2];
			for (int i = 0; i < numImages() + 2; i++) {
				ByteBuffer offsetbytes = ByteBuffer.allocate(ShpImageOffsetCnc.OFFSET_SIZE);
				
				offsetbytes.order(ByteOrder.LITTLE_ENDIAN);
				bytechannel.read(offsetbytes);
				offsetbytes.rewind();
				
				offsets[i] = new ShpImageOffsetCnc(offsetbytes);
			}

			shpimages = new ByteBuffer[numImages()];
			
			for (int i = 0; i < numImages(); i++) {
				ShpImageOffsetCnc imageoffset = offsets[i];

				// Format conversion buffers
				ByteBuffer sourcebytes = ByteBuffer.allocate(offsets[i + 1].offset - imageoffset.offset);
				bytechannel.read(sourcebytes);
				sourcebytes.rewind();
				
				ByteBuffer destbytes = ByteBuffer.allocate(width() * height());
				
				switch (imageoffset.offsetformat) {
					// Format80 image
					case FORMAT80:
						CodecUtility.decodeFormat80(sourcebytes, destbytes);
						break;
	
					// Format40 image
					case FORMAT40:
						int refoffset = imageoffset.refoff;
						int j;
						for (j = 0; j < numImages(); j++) {
							if (refoffset == offsets[j].offset) {
								break;
							}
						}
						CodecUtility.decodeFormat40(sourcebytes, destbytes, shpimages[j]);
						break;
	
					// Format20 image
					case FORMAT20:
						CodecUtility.decodeFormat20(sourcebytes, shpimages[i - 1], destbytes);
						break;
					}

				// Add the decompressed image to the image array
				shpimages[i] = destbytes;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				bytechannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Constructor, creates a new shp file from another {@link ImageFile} type.
	 * 
	 * @param name		Name of this file.
	 * @param imagefile File to source data from.
	 * @param params	Additional parameters: width, height, numimgs.
	 */
	public ShpFileCnc(String name, ImageFile imagefile, String... params) {

		super(name);

		int width = -1;
		int height = -1;
		int numimgs = -1;

		// Grab the parameters, check they fall within file limits
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
		}

		// Ensure each parameter was filled
		if (width == -1 || height == -1 || numimgs == -1) {
			throw new IllegalArgumentException();
		}

		// Check source will 'fit' into SHP file
		String sourcename = imagefile.getFileName();
		String targetclass = getClass().getSimpleName();

		ImageUtility.checkSize(sourcename, targetclass, width, height, MAX_WIDTH, MAX_HEIGHT);
		ImageUtility.checkNumImages(sourcename, targetclass, numimgs, MAX_NUMIMGS);
		ImageUtility.checkPaletted(sourcename, targetclass, imagefile);

		// Build file from ImageFile
		buildFile(width, height, ImageUtility.splitImage(width, height, numimgs,
				imagefile.width(), imagefile.height(), FORMAT_INDEXED,
				((PalettedInternal)imagefile).getRawImageData()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float adjustmentFactor() {

		return 1f;
	}

	/**
	 * Builds this SHP file object from various parts of other objects.
	 * 
	 * @param width	 Width of the images.
	 * @param height Height of the images.
	 * @param images Image data.
	 */
	private void buildFile(int width, int height, ByteBuffer[] images) {

		// Build temporary header
		shpfileheader = new ShpFileHeaderCnc((short)images.length, (short)width, (short)height);

		// Copy images
		shpimages = images;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float frameRate() {

		return 15f;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return shpfileheader.height & 0xffff;
	}

	/**
	 * Returns some information on this SHP file.
	 * 
	 * @return SHP file info.
	 */
	@Override
	public String toString() {

		return filename + " (C&C SHP file)" +
			"\n  Number of images: " + numImages() +
			"\n  Image width: " + width() +
			"\n  Image height: " + height() +
			"\n  Colour depth: 8-bit";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return shpfileheader.width & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(GatheringByteChannel outputchannel) {

		try {
			int numimages = numImages();

			// Encode each image
			ByteBuffer[] images = new ByteBuffer[numimages];
			for (int i = 0; i < images.length; i++) {
				ByteBuffer image = ByteBuffer.allocate(shpimages[i].capacity());
				CodecUtility.encodeFormat80(shpimages[i], image);
				images[i] = image;
			}

			// Construct image offset headers for each image
			ByteBuffer[] offsets = new ByteBuffer[numimages + 2];
			int offsettotal = ShpFileHeaderCnc.HEADER_SIZE + (ShpImageOffsetCnc.OFFSET_SIZE * offsets.length);
			for (int i = 0; i < numImages(); i++) {
				offsets[i] = new ShpImageOffsetCnc(offsettotal, FORMAT80, 0, (byte)0).toByteBuffer();
				offsettotal += images[i].limit();
			}

			// The 2 special image offsets at the end of the offset array
			offsets[numimages]     = new ShpImageOffsetCnc(offsettotal, (byte)0, 0, (byte)0).toByteBuffer();
			offsets[numimages + 1] = new ShpImageOffsetCnc(0, (byte)0, 0, (byte)0).toByteBuffer();

			// Build header
			ByteBuffer header = shpfileheader.toByteBuffer();

			// Write file
			outputchannel.write(header);
			outputchannel.write(offsets);
			outputchannel.write(images);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				outputchannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
