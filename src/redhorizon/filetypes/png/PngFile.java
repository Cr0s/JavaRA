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

package redhorizon.filetypes.png;

import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.AnimationFile;
import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.filetypes.PalettedInternal;
import redhorizon.filetypes.UnsupportedFileException;
import redhorizon.filetypes.WritableFile;
import redhorizon.filetypes.pal.PalFile;
import redhorizon.filetypes.shp.ShpFileDune2;
import redhorizon.filetypes.wsa.WsaFile;
import redhorizon.media.Palette;
import redhorizon.utilities.ImageUtility;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

/**
 * Wrapper of the Java implementation of Portable Network Graphics (PNG) images.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("png")
@FileType(ImageFile.class)
public class PngFile extends AbstractFile implements ImageFile, PalettedInternal, WritableFile {

	// Read-from information
	private ColourFormat format;
	private int width;
	private int height;
	private PngPalette pngpalette;
	private ByteBuffer pngimage;

	// Save-to information
	private static final int COMBINED_WIDTH_MAX = 1000;

	/**
	 * Constructor, creates a new png file with the given name and data.
	 * 
	 * @param name		  Name of the png file.
	 * @param bytechannel Data of the png file.
	 */
	public PngFile(String name, ReadableByteChannel bytechannel) {

		super(name);

		// Read the file data
		BufferedImage image = null;
		try {
			image = ImageIO.read(Channels.newInputStream(bytechannel));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// Fill image attributes and image data
		width    = image.getWidth();
		height   = image.getHeight();
		format   = image.getColorModel().getNumComponents() == 4 ? FORMAT_RGBA : FORMAT_RGB;
		pngimage = ByteBuffer.wrap(((DataBufferByte)image.getData().getDataBuffer()).getData());

		// Complete RGB palfile if paletted
		if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
			int[] rgb = new int[256];
			((IndexColorModel)image.getColorModel()).getRGBs(rgb);

			byte[][] colours = new byte[256][];
			for (int i = 0; i < 256; i++) {
				byte red   = (byte)((rgb[i] >> 16) & 0xff);
				byte green = (byte)((rgb[i] >> 8) & 0xff);
				byte blue  = (byte) (rgb[i] & 0xff);
				if (format == FORMAT_RGBA) {
					byte alpha = (byte)((rgb[i] >> 24) & 0xff);
					colours[i] = new byte[]{red, green, blue, alpha};
				}
				else {
					colours[i] = new byte[]{red, green, blue};
				}
			}
			pngpalette = new PngPalette(format, colours);
		}
		else {
			throw new UnsupportedFileException("Loading of non-paletted PNG files not supported");
		}
	}

	/**
	 * Constructor, creates a PNG file from an animation.  This just redirects
	 * to the {@link #PngFile(String, ImagesFile, String...)} constructor since
	 * the animation-specific parts don't affect the conversion.
	 * 
	 * @param name			The name of this file.
	 * @param animationfile File to source data from.
	 * @param params		Additional parameters: external palette (opt).
	 */
	public PngFile(String name, AnimationFile animationfile, String... params) {

		this(name, (ImagesFile)animationfile, params);
	}

	/**
	 * Constructor, allows the construction of a new PNG file from the given
	 * {@link ImagesFile} implementation, and the accompanying parameters.
	 * 
	 * @param name		 The name of this file.
	 * @param imagesfile File to source data from.
	 * @param params	 Additional parameters: external palette (opt).
	 */
	public PngFile(String name, ImagesFile imagesfile, String... params) {

		super(name);

		// Load palette from 0th parameter
		if (imagesfile instanceof Paletted && params.length > 0) {
			try (PalFile palfile = new PalFile("PNG palette", FileChannel.open(Paths.get(params[0])))) {
				pngpalette = new PngPalette(palfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Load palette from internal palette
		else if (imagesfile instanceof PalettedInternal) {
			pngpalette = new PngPalette(((PalettedInternal)imagesfile).getPalette());
		}

		// Unsupported conversion
		else {
			throw new IllegalArgumentException();
		}

		// Construct single image from the multiple images inside the file
		int maximagewidth  = imagesfile.width();
		int maximageheight = imagesfile.height();
		int numimages = imagesfile instanceof WsaFile && ((WsaFile<?>)imagesfile).isLooping() ?
				imagesfile.numImages() + 1 : imagesfile.numImages();
		int numimageshor = ImageUtility.fitImagesAcross(imagesfile.width(), numimages, COMBINED_WIDTH_MAX);
		int numimagesver = (int)Math.ceil(numimages / (double)numimageshor);

		width  = maximagewidth * numimageshor;
		height = maximageheight * numimagesver;
		format = imagesfile.format();

		int[] srcwidths  = new int[numimages];
		int[] srcheights = new int[numimages];

		ByteBuffer[] allimages = new ByteBuffer[imagesfile.numImages()];
		try (ReadableByteChannel srcimagesbytes = imagesfile instanceof PalettedInternal ?
				((PalettedInternal)imagesfile).getRawImageData() : imagesfile.getImagesData()) {
			for (int i = 0; i < allimages.length; i++) {

				int imagewidth = imagesfile instanceof ShpFileDune2 ?
						((ShpFileDune2)imagesfile).width(i) : maximagewidth;
				int imageheight = imagesfile instanceof ShpFileDune2 ?
						((ShpFileDune2)imagesfile).height(i) : maximageheight;

				srcwidths[i]  = imagewidth;
				srcheights[i] = imageheight;

				// Create an image the same size as the source frame
				ByteBuffer image = ByteBuffer.allocate(imagewidth * imageheight);
				srcimagesbytes.read(image);
				image.rewind();
				allimages[i] = image;
			}
		}
		// TODO: Should be able to soften the auto-close without needing this
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		pngimage = ImageUtility.combineImages(numimageshor, numimagesver,
				srcwidths, srcheights, FORMAT_INDEXED, allimages);
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
	public ColourFormat format() {

		return format;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImageData() {

		// Apply RGB(A) palette if paletted
		if (isIndexed()) {
			ByteBuffer image = ByteBuffer.allocate(width() * height() * format().size);
			ImageUtility.applyPalette(pngimage, image, pngpalette);
			return new ReadableByteChannelAdapter(image);
		}

		return new ReadableByteChannelAdapter(pngimage);
	}

	/**
	 * Retrieves the internal palette as an array of colours.
	 * 
	 * @return Array of colours acting as this image's internal palette, or
	 * 		   <tt>null</tt> if not indexed.
	 */
	@Override
	public Palette getPalette() {

		return pngpalette;
	}

	/**
	 * Returns the raw indexed data which constructed this image.
	 * 
	 * @return The raw image data, or <tt>null</tt> if not indexed.
	 */
	@Override
	public ReadableByteChannel getRawImageData() {

		if (isIndexed()) {
			return new ReadableByteChannelAdapter(pngimage);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return height;
	}

	/**
	 * Return whether or not this png file is a paletted one.
	 * 
	 * @return <tt>true</tt> if this png file uses an internal palette.
	 */
	public boolean isIndexed() {

		return pngpalette != null;
	}

	/**
	 * Returns some information on this PNG file.
	 * 
	 * @return PNG file info.
	 */
	@Override
	public String toString() {

		return filename + " (PNG file)" +
			"\n  Image width: " + width() +
			"\n  Image height: " + height() +
			"\n  Colour depth: " + (isIndexed() ?
					"8-bit (using internal palette)" : (format().size * 8) + "-bit");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(GatheringByteChannel outputchannel) {

		// Create paletted PNG
		if (isIndexed()) {

			// Build the IndexColorModel (Java palette), use alpha
			byte[] reds   = new byte[256];
			byte[] greens = new byte[256];
			byte[] blues  = new byte[256];
			byte[] alphas = new byte[256];

			for (int i = 0; i < 256; i++) {
				byte[] colour = pngpalette.getColour(i);
				reds[i]   = colour[0];
				greens[i] = colour[1];
				blues[i]  = colour[2];
				alphas[i] = format == FORMAT_RGBA && pngpalette.format() == FORMAT_RGBA ? colour[3] : 0;
			}
			IndexColorModel colormodel = new IndexColorModel(8, 256, reds, greens, blues, alphas);

			// Create new BufferedImage using the IndexColorModel, width, height
			BufferedImage outputimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, colormodel);
			byte[] outputimgdata = ((DataBufferByte)outputimg.getRaster().getDataBuffer()).getData();

			// Write PNG image data into the BufferedImage
			pngimage.get(outputimgdata).rewind();

			// Write to file
			try {
				ImageIO.write(outputimg, "PNG", Channels.newOutputStream(outputchannel));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Create 32-bit PNG
		else {
			throw new UnsupportedFileException("Saving of non-paletted PNG files not supported");
		}
	}
}
