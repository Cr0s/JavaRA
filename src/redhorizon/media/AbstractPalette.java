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

package redhorizon.media;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.PaletteFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Basic palette type.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractPalette implements Palette {

	protected final int size;
	protected final ColourFormat format;
	protected final byte[][] palette;

	/**
	 * Constructor, copy an existing palette into this one.
	 * 
	 * @param palette
	 */
	protected AbstractPalette(Palette palette) {

		this.size    = palette.size();
		this.format  = palette.format();
		this.palette = new byte[size][format.size];
		for (int i = 0; i < size; i++) {
			this.palette[i] = palette.getColour(i);
		}
	}

	/**
	 * Constructor, create a palette from a palette file.
	 * 
	 * @param palettefile
	 */
	protected AbstractPalette(PaletteFile palettefile) {

		this.size = palettefile.size();
		this.format = palettefile.format();
		this.palette = new byte[size][format.size];

		try (ReadableByteChannel palettedata = palettefile.getPaletteData()) {
			for (int i = 0; i < size; i++) {
				ByteBuffer colourbytes = ByteBuffer.allocate(format.size);
				palettedata.read(colourbytes);
				palette[i] = colourbytes.array();
			}
		}
		// TODO: Should be able to soften the auto-close without needing this
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size	 Number of colours in the palette
	 * @param format Colour format of the palette
	 * @param bytes	 Palette data.
	 */
	protected AbstractPalette(int size, ColourFormat format, byte[][] bytes) {

		this.size    = size;
		this.format  = format;
		this.palette = bytes;
	}

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size	 Number of colours in the palette.
	 * @param format Colour format of the palette.
	 * @param bytes	 Palette data.
	 */
	protected AbstractPalette(int size, ColourFormat format, ByteBuffer bytes) {

		this.size    = size;
		this.format  = format;
		this.palette = new byte[size][format.size];
		for (int i = 0; i < palette.length; i++) {
			palette[i] = new byte[format.size];
			bytes.get(palette[i]);
		}
	}

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size		  Number of colours in the palette.
	 * @param format	  Colour format of the palette.
	 * @param bytechannel Palette data.
	 */
	protected AbstractPalette(int size, ColourFormat format, ReadableByteChannel bytechannel) {

		this.size    = size;
		this.format  = format;
		this.palette = new byte[size][format.size];
		for (int i = 0; i < palette.length; i++) {
			ByteBuffer colourbytes = ByteBuffer.allocate(format.size);
			try {
				bytechannel.read(colourbytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			palette[i] = colourbytes.array();
		}
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
	public byte[] getColour(int index) {

		return palette[index];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {

		return size;
	}

	/**
	 * Returns this palette in a {@link ByteBuffer} format.
	 * 
	 * @return Buffer of this palette.
	 */
	public ByteBuffer toByteBuffer() {

		ByteBuffer buffer = ByteBuffer.allocate(format.size);
		for (byte[] colour: palette) {
			buffer.put(colour);
		}
		buffer.rewind();
		return buffer;
	}
}
