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

package redhorizon.utilities;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.File;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.filetypes.png.PngFile;
import redhorizon.media.Palette;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

/**
 * Utility class containing methods to aid image files/classes with tasks such
 * as the transfer of image data from a file to an OpenGL-useable texture, to
 * file conversion checks.
 * 
 * @author Emanuel Rabina
 */
public class ImageUtility {

	/**
	 * Private default constructor as this class is only ever meant to be used
	 * statically.
	 */
	private ImageUtility() {
	}

	/**
	 * Applies a palette to some image data.
	 * 
	 * @param source  Original colour index frame/image.
	 * @param dest	  Buffer to store the colourized data.
	 * @param palette Palette data to use.
	 */
	public static void applyPalette(ByteBuffer source, ByteBuffer dest, Palette palette) {

		for (int i = 0; i < source.limit(); i++) {
			dest.put(palette.getColour(source.get() & 0xff));
		}
		source.rewind();
		dest.rewind();
	}

	/**
	 * Applies a palette to an array of paletted image data.
	 * 
	 * @param source  Original colour index frames/images.
	 * @param dest	  Buffers to store the colourized data.
	 * @param palette Palette data to use.
	 */
	public static void applyPalette(ByteBuffer[] source, ByteBuffer[] dest, Palette palette) {

		for (int i = 0; i < source.length; i++) {
			applyPalette(source[i], dest[i], palette);
		}
	}

	/**
	 * Check that all images in the array are of the same dimensions and format
	 * as the first image in the array.
	 * 
	 * @param imagefiles Array of images to check.
	 * @throws UnsupportedOperationException If the images differ slightly.
	 */
	public static void checkConsistentImages(ImageFile[] imagefiles) throws UnsupportedOperationException {

		int width  = imagefiles[0].width();
		int height = imagefiles[0].height();
		ColourFormat format = imagefiles[0].format();

		for (int i = 1; i < imagefiles.length; i++) {
			ImageFile imagefile = imagefiles[i];
			if (imagefile.width() != width || imagefile.height() != height || imagefile.format() != format) {
				throw new IllegalArgumentException("Unable to combine the images into a single image. " +
						"The widths/heights are not all the same.");
			}
		}
	}

	/**
	 * Check number of images.  Used for when there is no single source file.
	 * 
	 * @param targetclass Name of the target class.
	 * @param numimgs	  Number of source images.
	 * @param maxnumimgs  Maximum number of target images.
	 * @throws UnsupportedOperationException If <tt>numimages</tt> is greater
	 * 		   than <tt>maxnumimgs</tt>.
	 */
	public static void checkNumImages(String targetclass, int numimgs, int maxnumimgs)
		throws UnsupportedOperationException {

		if (numimgs > maxnumimgs) {
			throw new UnsupportedOperationException("Too many images for type " + targetclass + ". " +
					"Maximum number of images for this filetype is " + maxnumimgs + ".");
		}
	}

	/**
	 * Check number of images.  Used for when there is a single named source
	 * file.
	 * 
	 * @param sourcename  Name of the source file.
	 * @param targetclass Name of the target class.
	 * @param numimgs	  Number of source images.
	 * @param maxnumimgs  Maximum number of target images.
	 * @throws UnsupportedOperationException If <tt>numimages</tt> is
	 * 		   greater than <tt>maxnumimgs</tt>.
	 */
	public static void checkNumImages(String sourcename, String targetclass, int numimgs, int maxnumimgs)
		throws UnsupportedOperationException {

		if (numimgs > maxnumimgs) {
			throw new UnsupportedOperationException("Too many images in " + sourcename + " for type " + targetclass + ". " +
					"Maximum number of images for this filetype is " + maxnumimgs + ".");
		}
	}

	/**
	 * Check source is paletted.
	 * 
	 * @param sourcename  Name of the source file.
	 * @param targetclass Name of the target class.
	 * @param sourcefile  Source file type.
	 * @throws UnsupportedOperationException If <tt>sourcefile</tt> is not a
	 * 		   paletted file type.
	 */
	public static void checkPaletted(String sourcename, String targetclass, File sourcefile)
		throws UnsupportedOperationException {

		// Special case for PNG files
		if (sourcefile instanceof PngFile) {
			if (!((PngFile)sourcefile).isIndexed()) {
				throw new UnsupportedOperationException(sourcename + " is not an indexed/paletted image. " +
						"Conversions to " + targetclass + " require indexed/paletted source images.");
			}
		}
		else if (!(sourcefile instanceof Paletted)) {
			throw new UnsupportedOperationException(sourcename + " is not an indexed/paletted image. " +
					"Conversions to " + targetclass + " require indexed/paletted source images.");
		}
	}

	/**
	 * Checks width, height, and uncompressed image size.
	 * 
	 * @param sourcename  Name of the source file.
	 * @param targetclass Name of the target class.
	 * @param width		  Source image width.
	 * @param height	  Source image height.
	 * @param maxwidth	  Maximum width of the target image.
	 * @param maxheight	  Maximum height of the target image.
	 * @throws UnsupportedOperationException If the source image size exceeds
	 * 		   the given dimensions.
	 */
	public static void checkSize(String sourcename, String targetclass, int width, int height,
		int maxwidth, int maxheight) throws UnsupportedOperationException {

		if (width > maxwidth) {
			throw new UnsupportedOperationException(
					"Image width in " + sourcename + " is too wide for type " + targetclass + ". " +
					"Maximum width of images for this filetype is " + maxwidth + ".");
		}

		if (height > maxheight) {
			throw new UnsupportedOperationException(
					"Image height in " + sourcename + " is too tall for type " + targetclass + ". " +
					"Maximum height of images for this filetype is " + maxheight + ".");
		}
	}

	/**
	 * Creates a single overall image from all the images in the given file.  If
	 * the image is of the paletted sort, a palette must be supplied.
	 * 
	 * @param combinewidth	The number of images to combine horizontally.
	 * @param combineheight The number of images to combine vertically.
	 * @param imagesfile	File containing a series of images.
	 * @param palette		Palette data to use for the merging (if paletted).
	 * @return Single combined image data.
	 */
	public static ByteBuffer combineImages(int combinewidth, int combineheight,
		ImagesFile imagesfile, Palette palette) {

		int width  = imagesfile.width();
		int height = imagesfile.height();
		ColourFormat format = imagesfile.format();
		ByteBuffer[] images = new ByteBuffer[imagesfile.numImages()];

		ReadableByteChannel imagesdata = imagesfile.getImagesData();
		for (int i = 0; i < images.length; i++) {
			ByteBuffer imagedata = ByteBuffer.allocate(imagesfile.width() * imagesfile.height());
			if (imagesfile instanceof Paletted) {
				ByteBuffer rawimage = ByteBuffer.allocate(width * height);
				try {
					imagesdata.read(rawimage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				rawimage.rewind();
				applyPalette(rawimage, imagedata, palette);
			}
			else {
				try {
					imagesdata.read(imagedata);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				imagedata.rewind();
			}
			images[i] = imagedata;
		}

		int[] widths = new int[imagesfile.numImages()];
		Arrays.fill(widths, width);
		int[] heights = new int[imagesfile.numImages()];
		Arrays.fill(heights, height);

		return combineImages(combinewidth, combineheight, widths, heights, format, images);
	}

	/**
	 * Creates a single overall image buffer from a series of smaller images.
	 * 
	 * @param numimgshor The number of images to combine horizontally.
	 * @param numimgsver The number of images to combine vertically.
	 * @param widths	 Width of each image.
	 * @param heights	 Height of each image
	 * @param format	 Number of colour channels in each image.
	 * @param images	 The images to combine.
	 * @return Single combined image data.
	 */
	public static ByteBuffer combineImages(int numimgshor, int numimgsver,
		int[] widths, int[] heights, ColourFormat format, ByteBuffer[] images) {

		int maxwidth = 0;
		for (int i = 0; i < widths.length; i++) {
			if (maxwidth < widths[i]) {
				maxwidth = widths[i];
			}
		}
		int maxheight = 0;
		for (int i = 0; i < heights.length; i++) {
			if (maxheight < heights[i]) {
				maxheight = heights[i];
			}
		}

		int compilewidth = maxwidth * numimgshor;
		int compileheight = maxheight * numimgsver;
		ByteBuffer compilation = ByteBuffer.allocate(format.size * compilewidth * compileheight);

		// For each image vertically
		for (int i = 0, y = 0; y < numimgsver; y++) {
			int ypos = y * maxheight * compilewidth;

			// For each image horizontally
			for (int x = 0; i < images.length && x < numimgshor; i++, x++) {
				int xpos = (i % numimgshor) * maxwidth;

				// For each vertical line of pixels in the current image
				for (int yi = 0; yi < heights[i]; yi++) {
					int npos = yi * compilewidth;

					compilation.position(ypos + xpos + npos);
					compilation.put(images[i].array(), yi * widths[i], widths[i]);
					Thread.yield();
				}
			}
		}
		compilation.rewind();
		return compilation;
	}

	/**
	 * Given a multi-image file, find out how many images will fit across so
	 * that the resulting combined width is less-than or equal-to
	 * <tt>across</tt> pixels.
	 * 
	 * @param width		Width of each image.
	 * @param numimages Number of images to draw.
	 * @param across	Desired combined image width to stay below.
	 * @return Number of images that will fit within <tt>across</tt> pixels.
	 */
	public static int fitImagesAcross(int width, int numimages, int across) {

		return width < across ?
				Math.min((across - (across % width)) / width, numimages) :
				1;
	}

	/**
	 * Creates a series of images built from a single image, split using the
	 * given dimensions for the resulting images.  The source can be either
	 * indexed or non-indexed data; the result will be of the same type.
	 * <p>
	 * Only works if the dimensions to split the image by cut the source image
	 * exactly, without remaining edges.  An exception is thrown if this is not
	 * the case.
	 * 
	 * @param splitwidth  Width of resulting images.
	 * @param splitheight Height of resulting images.
	 * @param numimages	  Number of images to return.  Can be used to ensure
	 * 					  blank images aren't in the result.
	 * @param imagefile	  File to source images from.
	 * @return <tt>ByteBuffer[]</tt> for each new image.
	 * @throws IllegalArgumentException If the source image cannot be split
	 * 		   evenly as requested.
	 */
	public static ByteBuffer[] splitImage(int splitwidth, int splitheight, int numimages,
		ImageFile imagefile) {

		return splitImage(splitwidth, splitheight, numimages,
				imagefile.width(), imagefile.height(), imagefile.format(), imagefile.getImageData());
	}

	/**
	 * Creates a series of images built from a single image, split using the
	 * given dimensions for the resulting images.  The source can be either
	 * indexed or non-indexed data; the result will be of the same type.
	 * <p>
	 * Only works if the dimensions to split the image by cut the source image
	 * exactly, without remaining edges.  An exception is thrown if this is not
	 * the case.
	 * 
	 * @param splitwidth  Width of resulting images.
	 * @param splitheight Height of resulting images.
	 * @param numimages	  Number of images to return.  Can be used to ensure
	 * 					  blank images aren't in the result.
	 * @param srcwidth	  Width of the source image.
	 * @param srcheight	  Height of the source image.
	 * @param format	  Number of colour components in the source image.
	 * @param srcimage	  File to source images from.
	 * @return <tt>ByteBuffer[]</tt> for each new image.
	 * @throws IllegalArgumentException If the source image cannot be split
	 * 		   evenly as requested.
	 */
	public static ByteBuffer[] splitImage(int splitwidth, int splitheight, int numimages,
		int srcwidth, int srcheight, ColourFormat format, ReadableByteChannel srcimage) {

		ByteBuffer srcbytes = BufferUtility.readRemaining(srcimage);

		// Check result dimensions cut source perfectly
		if ((srcwidth % splitwidth != 0) || (srcheight % splitheight != 0) ||
			((srcwidth / splitwidth) * (srcheight / splitheight) < numimages)) {
			throw new IllegalArgumentException("Unable to split the source image. " +
					"The requested dimensions do not split the image into exact pieces.");
		}

		// Create split images
		ByteBuffer[] compilation = new ByteBuffer[numimages];
		for (int i = 0, row = 0, col = 0, numimghor = srcwidth / splitwidth; i < numimages; ) {
			ByteBuffer splitimage = ByteBuffer.allocate(splitwidth * splitheight * format.size);

			for (int y = 0; y < splitheight; y++) {
				srcbytes.position((row + col) + (y * srcwidth));
				srcbytes.get(splitimage.array(), y * splitwidth, splitwidth);
			}
			splitimage.rewind();
			compilation[i] = splitimage;

			// Adjust current data co-ordinates in source image
			i++;
			if (i % numimghor == 0) {
				row += srcwidth * splitheight * format.size;
			}
			col = (i % numimghor) * splitwidth;
		}
		srcbytes.rewind();

		return compilation;
	}
}
