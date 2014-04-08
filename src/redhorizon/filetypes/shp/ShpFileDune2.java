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

import redhorizon.filetypes.FileType;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.PalettedInternal;
import redhorizon.filetypes.WritableFile;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.ImageUtility;
import static redhorizon.filetypes.ColourFormat.FORMAT_INDEXED;
import static redhorizon.filetypes.shp.ShpFileHeaderDune2.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Implementation of the Dune 2 SHP file.
 * <p>
 * Details on this filetype are not very common, so I've written comments
 * (visible in the source, not in this JavaDoc) on how the file is structured,
 * so that others it can help others understand what I've learned about the file
 * if ever they wish to write programs to utilize the file.
 * <p>
 * The Dune 2 SHP file is only used for the conversion utility.
 * 
 * @author Emanuel Rabina
 */

/* 
 * ======================
 * Dune 2 SHP file format
 * ======================
 * 
 * The Dune 2 SHP file, is a multiple image filetype, where each image can have
 * it's own set of dimensions.  The file is structured as follows:
 * 
 * File header:
 *   NumImages (2 bytes)    - the number of images in the file
 *   Offsets[NumImages + 1] - offset to the image header for an image.  The last
 *     (2 or 4 bytes each)    offset points to the end of the file.  The offsets
 *                            don't take into account the NumImages bytes at the
 *                            beginning, so add 2 bytes to the offset value to
 *                            get the actual position of an image header in the
 *                            file
 * 
 * The size of the offsets can be either 2 or 4 bytes.  There is no simple way
 * to determine which it will be, but checking the file's 4th byte to see if
 * it's 0, seems to be a commonly accepted practice amongst existing Dune 2 SHP
 * file readers:
 * 
 * eg: A 2-byte offset file: 01 00 06 00 EC 00 45 0A ...
 *     A 4-byte offset file: 01 00 08 00 00 00 EC 00 ...
 *                                       ^^
 * The marked byte will be 0 in 4-byte offset files, non 0 in 2-byte offset
 * files.
 * Lastly, like C&C SHP files, there is an extra offset, pointing to the end of
 * the file (or what would have been the position of another image header/data
 * pair).
 * 
 * Following the file header, are a series of image header & image data pairs.
 * The image header is structured as follows:
 * 
 * Image header:
 *   Flags (2 bytes)      - flags to identify the type of data following the
 *                          header, and/or the compression schemes used
 *   Slices (1 byte)      - number of Format2 slices used to encode the image
 *                          data.  Often this is the same as the height of the
 *                          image
 *   Width (2 bytes)      - width of the image
 *   Height (1 byte)      - height of the image
 *   File size (2 bytes)  - size of both this image header and the image data
 *                          on the file
 *   Image size (2 bytes) - size of the image data in Format2 form.
 * 
 * Regarding the flags, there seem to be 3 known flags:
 *  - The first bit controls whether there is a lookup table in the image data,
 *    used for remapping SHP colours to faction-specific colours in-game.
 *      0 = no lookup table
 *      1 = lookup table
 *  - The second bit controls what compression to apply to the file.
 *      0 = Format2, followed by Format80
 *      1 = Format2 only
 *  - The third bit, used in conjunction with the first, means the first byte of
 *    the image data gives the size of the lookup table that follows.
 *      0 = Fixed-length lookup table (16 bytes)
 *      1 = Variable-length lookup table
 * 
 * And after this image header is the image data.
 */
@FileType(ImagesFile.class)
public class ShpFileDune2 extends ShpFile<ShpFileHeaderDune2> implements WritableFile {

	// Read-from information
	private ShpImageHeaderDune2[] shpimageheaders;
	private int maxwidth;
	private int maxheight;
	private boolean variedwidth;
	private boolean variedheight;

	// Save-to information
	private static final String PARAM_FACTION = "-faction";

	private static final int MAX_HEIGHT = 255;

	private boolean srcfactioncols;

	/**
	 * Constructor, creates a new Dune 2 shp file with the given name and file
	 * data.
	 * 
	 * @param name		  The name of this file.
	 * @param filechannel The data for this file.
	 */
	public ShpFileDune2(String name, FileChannel filechannel) {

		super(name);

		// Read file header (read ahead header for offset check)
		ByteBuffer headerbytes = ByteBuffer.allocate(ShpFileHeaderDune2.size() + 3);
		try {
			filechannel.read(headerbytes);

			headerbytes.rewind();
			shpfileheader = new ShpFileHeaderDune2(headerbytes);
	
			filechannel.position(filechannel.position() - 3);
			shpimageheaders = new ShpImageHeaderDune2[numImages()];
			shpimages       = new ByteBuffer[numImages()];
	
			// Read offset table
			int[] offsets = new int[numImages() + 1];
			int offsetsize = shpfileheader.offsetsize;
			ByteBuffer offsetbytes = ByteBuffer.allocate(offsets.length * offsetsize);
			filechannel.read(offsetbytes);
			offsetbytes.rewind();
			for (int i = 0; i < offsets.length; i++) {
				offsets[i] = offsetsize == IMAGE_OFFSET_2 ?
					offsetbytes.getShort() & 0xffff : offsetbytes.getInt();
			}
	
			// Read image headers and image data
			for (int i = 0; i < numImages(); i++) {
				filechannel.position(offsets[i] + 2);
				ByteBuffer imagedata = ByteBuffer.allocate(Math.abs(offsets[i + 1] - offsets[i]));
				filechannel.read(imagedata);
				imagedata.rewind();
	
				ShpImageHeaderDune2 imageheader = new ShpImageHeaderDune2(imagedata);
				shpimageheaders[i] = imageheader;
	
				// Update maximum width/height variables
				int width = width(i);
				if (maxwidth < width) {
					maxwidth = width;
				}
				int height = height(i);
				if (maxheight < height) {
					maxheight = height;
				}
	
				// Decompress the image data
				ByteBuffer image = ByteBuffer.allocate(width * height);
				if (imageheader.isCompressed()) {
					ByteBuffer uncompressed = ByteBuffer.allocate(imageheader.datasize & 0xffff);
					CodecUtility.decodeFormat80(imagedata, uncompressed);
					CodecUtility.decodeFormat2(uncompressed, image);
				}
				else {
					CodecUtility.decodeFormat2(imagedata, image);
				}
	
				// Replace lookup values with real values from lookup table (if applicable)
				if (imageheader.lookuptable != null) {
					while (image.hasRemaining()) {
						image.put(imageheader.lookuptable[image.get(image.position())]);
					}
					image.rewind();
				}
				shpimages[i] = image;
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructor, builds this file from an existing {@link ImageFile}.
	 * 
	 * @param name		Name of this file.
	 * @param imagefile File to source data from.
	 * @param params	Additonal parameters: width, height, numimgs.
	 */
	public ShpFileDune2(String name, ImageFile imagefile, String... params) {

		super(name);

		int width = -1;
		int height = -1;
		int numimgs = -1;

		// Grab the parameters
		for (String param: params) {

			// Check width, height and frame number parameters 'fit'
			if (param.startsWith(PARAM_WIDTH)) {
				width = Integer.parseInt(param.substring(PARAM_WIDTH.length()));
			}
			else if (param.startsWith(PARAM_HEIGHT)) {
				height = Integer.parseInt(param.substring(PARAM_HEIGHT.length()));
			}
			else if (param.startsWith(PARAM_NUMIMGS)) {
				numimgs = Integer.parseInt(param.substring(PARAM_NUMIMGS.length()));
			}

			// Create internal lookup table?
			else if (param.equals(PARAM_FACTION)) {
				srcfactioncols = true;
			}
		}

		// Ensure each parameter was filled
		if (width == -1 || height == -1 || numimgs == -1) {
			throw new IllegalArgumentException();
		}

		// Check source will 'fit' into Dune 2 SHP file
		String sourcename = imagefile.getFileName();
		String targetclass = getClass().getSimpleName();

		ImageUtility.checkSize(sourcename, targetclass, width, height, MAX_WIDTH, MAX_HEIGHT);
		ImageUtility.checkNumImages(sourcename, targetclass, numimgs, MAX_NUMIMGS);
		ImageUtility.checkPaletted(sourcename, targetclass, imagefile);

		// Build file from ImageFile
		ByteBuffer[] images = imagefile instanceof PalettedInternal ?
				ImageUtility.splitImage(width, height, numimgs,
					imagefile.width(), imagefile.height(), FORMAT_INDEXED,
					((PalettedInternal)imagefile).getRawImageData()):
				ImageUtility.splitImage(width, height, numimgs, imagefile);
		buildFile(width, height, images);
	}

	/**
	 * Constructor, builds this file from a series of <tt>ImageFile</tt>
	 * implementations.
	 * 
	 * @param name		 Name of this file.
	 * @param imagefiles Files to source data from.
	 * @param params	 Additional parameters: create faction map.
	 */
	public ShpFileDune2(String name, ImageFile[] imagefiles, String... params) {

		super(name);

		// Grab the parameters
		for (String param: params) {

			// Create internal lookup table?
			if (param.equals(PARAM_FACTION)) {
				srcfactioncols = true;
			}
		}

		// Check source will 'fit' into Dune 2 SHP file
		String targetclass = getClass().getSimpleName();

		ImageUtility.checkNumImages("(list of images)", targetclass, imagefiles.length, MAX_NUMIMGS);
		for (ImageFile imagefile: imagefiles) {
			String sourcename = imagefile.getFileName();
			ImageUtility.checkSize(sourcename, targetclass, imagefile.width(), imagefile.height(),
					MAX_WIDTH, MAX_HEIGHT);
			ImageUtility.checkPaletted(sourcename, targetclass, imagefile);
		}

		// Build file from ImageFile[]
		ByteBuffer[] images = new ByteBuffer[imagefiles.length];
		int[] widths  = new int[imagefiles.length];
		int[] heights = new int[imagefiles.length];
		for (int i = 0; i < imagefiles.length; i++) {
			try (ImageFile imagefile = imagefiles[i]) {
				ByteBuffer imagedata = ByteBuffer.allocate(imagefile.width() * imagefile.height() *
						imagefile.format().size);
				(imagefile instanceof PalettedInternal ?
						((PalettedInternal)imagefile).getRawImageData() : imagefile.getImageData())
						.read(imagedata);
				images[i] = (ByteBuffer)imagedata.rewind();
				widths[i]  = imagefile.width();
				heights[i] = imagefile.height();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		buildFile(widths, heights, images);
	}

	/**
	 * Constructor, builds this file from another <tt>ImagesFile</tt>
	 * implementation.
	 * 
	 * @param name		 Name of this file.
	 * @param imagesfile File to source data from.
	 * @param params	 Additional parameters.  None used for this conversion.
	 */
	public ShpFileDune2(String name, ImagesFile imagesfile, String... params) {

		super(name);

		// Check source will 'fit' into Dune 2 SHP file
		String sourcename = imagesfile.getFileName();
		String targetclass = getClass().getSimpleName();

		ImageUtility.checkNumImages(sourcename, targetclass, imagesfile.numImages(), MAX_NUMIMGS);
		ImageUtility.checkPaletted(sourcename, targetclass, imagesfile);
		ImageUtility.checkSize(sourcename, targetclass, imagesfile.width(), imagesfile.height(),
				MAX_WIDTH, MAX_HEIGHT);

		// Build file from ImagesFile
		try (ReadableByteChannel srcimages = (imagesfile instanceof PalettedInternal ?
				((PalettedInternal)imagesfile).getRawImageData() : imagesfile.getImagesData())) {
			ByteBuffer[] images = new ByteBuffer[imagesfile.numImages()];
			for (int i = 0; i < images.length; i++) {
				ByteBuffer imagedata = ByteBuffer.allocate(imagesfile.width() * imagesfile.height() *
						imagesfile.format().size);
				srcimages.read(imagedata);
				images[i] = (ByteBuffer)imagedata.rewind();
			}
			buildFile(imagesfile.width(), imagesfile.height(), images);
		}
		// TODO: Should be able to soften the auto-close without needing this
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates this Dune 2 file object from various parts of other objects.
	 * 
	 * @param width	 Width of each image.
	 * @param height Height of each image.
	 * @param images Array of images to use.
	 */
	private void buildFile(int width, int height, ByteBuffer[] images) {

		int[] widths  = new int[images.length];
		int[] heights = new int[images.length];
		Arrays.fill(widths, width);
		Arrays.fill(heights, height);
		buildFile(widths, heights, images);
	}

	/**
	 * Creates this Dune 2 file object from various parts of other objects.
	 * 
	 * @param widths  Width of each image.
	 * @param heights Height of each image.
	 * @param images  Array of images to use.
	 */
	private void buildFile(int[] widths, int[] heights, ByteBuffer[] images) {

		// Create header
		shpfileheader = new ShpFileHeaderDune2((short)images.length);

		// Create image headers
		shpimageheaders = new ShpImageHeaderDune2[images.length];
		maxwidth = widths[0];
		maxheight = heights[0];

		for (int i = 0; i < images.length; i++) {
			int width = widths[i];
			int height = heights[i];

			shpimageheaders[i] = new ShpImageHeaderDune2((short)width, (byte)height, null, 0, 0);

			if (!variedwidth && maxwidth != width) {
				variedwidth = true;
			}
			if (maxwidth < width) {
				maxwidth = width;
			}

			if (!variedheight && maxheight != height) {
				variedheight = true;
			}
			if (maxheight < height) {
				maxheight = height;
			}
		}

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
	 * Returns the height of the largest image within this file.
	 * 
	 * @return Height of the largest image contained within.
	 */
	@Override
	public int height() {

		return maxheight;
	}

	/**
	 * Returns the height of the image at the given index.
	 * 
	 * @param imagenum Index of the internal image.
	 * @return Height of the image at that index.
	 */
	public int height(int imagenum) {

		return shpimageheaders[imagenum].height & 0xff;
	}

	/**
	 * Returns some information on this Dune 2 SHP file.
	 * 
	 * @return Dune 2 SHP file info.
	 */
	@Override
	public String toString() {

		return filename + " (Dune 2 SHP file)" +
			"\n  Number of images: " + numImages() +
			"\n  Image width: " + (variedwidth ? "(varies)" : width()) +
			"\n  Image height: " + (variedheight ? "(varies)" : height()) +
			"\n  Colour depth: 8-bit";
	}

	/**
	 * Returns the width of the largest image within this file.
	 * 
	 * @return Width of the largest image contained within.
	 */
	@Override
	public int width() {

		return maxwidth;
	}

	/**
	 * Returns the width of the image at the given index.
	 * 
	 * @param imagenum Index of the internal image.
	 * @return Width of the image at that index.
	 */
	public int width(int imagenum) {

		return shpimageheaders[imagenum].width & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(GatheringByteChannel outputchannel) {

		int numimgs = numImages();
		int preoffsetsize = (numimgs + 1) * shpfileheader.offsetsize;

		// Encode each image, update image headers, create image offsets
		ByteBuffer imageoffsets = ByteBuffer.allocate(preoffsetsize);
		int offsettotal = preoffsetsize;

		ByteBuffer[] images = new ByteBuffer[numimgs];

		for (int i = 0; i < numimgs; i++) {
			ByteBuffer imagebytes = shpimages[i];
			byte[] colourtable = null;

			// If meant for faction colours, generate a colour table for the frame,
			// while at the same time replacing the image bytes with the index
			if (srcfactioncols) {
				LinkedHashMap<Byte,Byte> colours = new LinkedHashMap<>();

				// Track colour values used, replace colour values with table values
				byte index = 0;
				for (int imagepos = 0; imagepos < imagebytes.limit(); imagepos++) {
					byte colour = imagebytes.get(imagepos);
					if (!colours.containsKey(colour)) {
						colours.put(colour, index++);
					}
					imagebytes.put(imagepos, colours.get(colour));
				}

				// Convert from hashmap -> byte[]
				colourtable = new byte[Math.max(colours.size(), 16)];
				int j = 0;
				for (byte colour: colours.keySet()) {
					colourtable[j++] = colour;
				}
			}

			// Encode image data
			// NOTE: Compression with Format80 is skipped for Dune 2 SHP files due
			//       to my implementation of Format80 compression causing "Memory
			//       Corrupts!" error messages to come from Dune 2 itself.
			ByteBuffer image = ByteBuffer.allocate((int)(imagebytes.capacity() * 1.5));
			CodecUtility.encodeFormat2(imagebytes, image);

			// Build image header
			ShpImageHeaderDune2 imageheader = new ShpImageHeaderDune2(
					(short)width(i), (byte)height(i), colourtable, image.limit(), image.limit());

			shpimageheaders[i] = imageheader;
			images[i] = image;

			// Track offset values
			imageoffsets.putInt(offsettotal);
			offsettotal += imageheader.filesize & 0xffff;
		}

		// Add the special end-of-file offset
		imageoffsets.putInt(offsettotal).rewind();


		// Write file to disk
		try {
			outputchannel.write(shpfileheader.toByteBuffer());

			outputchannel.write(imageoffsets);
			for (int i = 0; i < numimgs; i++) {
				outputchannel.write(shpimageheaders[i].toByteBuffer());
				outputchannel.write(images[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
