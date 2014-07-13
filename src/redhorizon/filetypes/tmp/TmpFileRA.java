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

package redhorizon.filetypes.tmp;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.utilities.BufferUtility;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGBA;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

/**
 * Representation of Red Alert's map tiles.  These are the various bits and
 * pieces which comprise the [MapPack] section of the scenario files.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions({"int", "sno", "tmp"})
public class TmpFileRA extends AbstractFile implements ImagesFile, Paletted {

	private TmpFileHeaderRA tmpheader;
	private ByteBuffer[] tmpimages;
	
	/**
	 * Constructor, create a new RA template file from existing data.
	 * 
	 * @param name		  The name of this file.
	 * @param bytechannel Data for this file.
	 */
	public TmpFileRA(String name, ReadableByteChannel bytechannel) {

		super(name);
		
		try {
		    	ByteBuffer tmpBytes = BufferUtility.readRemaining(bytechannel);
		    	tmpBytes.order(ByteOrder.LITTLE_ENDIAN);
		    	tmpBytes.rewind();

		    	short width = tmpBytes.getShort();
		    	short height = tmpBytes.getShort();
		    	this.tmpheader = new TmpFileHeaderRA();
		    	this.tmpheader.width = width;
		    	this.tmpheader.height = height;
		    	
		    	this.tmpheader.numtiles = tmpBytes.getShort();
		    	
		    	tmpBytes.position(tmpBytes.position() + 2); // skip unknown word
		    	
			this.tmpheader.tilewidth = tmpBytes.getShort();
			this.tmpheader.tileheight = tmpBytes.getShort();
		    	
		    	tmpBytes.position(tmpBytes.position() + 4);
		    	
		    	int imgStart = tmpBytes.getInt();
		    	this.tmpheader.imagedata = imgStart;
		    	
		    	tmpBytes.position(tmpBytes.position() + 8);
		    	this.tmpheader.index2 = tmpBytes.getInt();
		    	
		    	tmpBytes.position(tmpBytes.position() + 4);
		    	this.tmpheader.index1 = tmpBytes.getInt();
		    	
		    	//System.out.println(width + " x " + height + " | t: " + this.tmpheader.tilewidth + " x " + this.tmpheader.tileheight + " = " + tmpheader.numtiles + " | " + tmpheader.imagedata + " | " + tmpheader.index2 + " " + tmpheader.index1);
		    	
		    	tmpBytes.position(this.tmpheader.index1);

		    	// Read offsets array
		    	byte[] offsets = new byte[this.tmpheader.numtiles];
		    	tmpBytes.get(offsets);
		    	
		    	//System.out.print("Offsets array: ");
		    	//for (int i = 0; i < tmpheader.numtiles; i++) {
		    	//    System.out.print((offsets[i] & 0xFF) + " ");
		    	//}
		    	//System.out.println("");
		    	
		    	this.tmpimages = new ByteBuffer[this.tmpheader.numtiles];
		    	
		    	tmpBytes.rewind();
			for (int i = 0; i < this.tmpimages.length; i++) {
				int imageoffset = offsets[i] & 0xFF;
				
				// Skip empty tiles
				if (imageoffset == 0xFF) {
				    	tmpimages[i] = null;
					continue;
				}
				
				// Read data at given offset
				ByteBuffer imagedata = ByteBuffer.allocate(width() * height());
				//System.out.println("New position (" + imageoffset + "): " + this.tmpheader.imagedata + imageoffset * width() * height());
				tmpBytes.position(this.tmpheader.imagedata + imageoffset * width() * height());
				tmpBytes.get(imagedata.array());
				
				tmpimages[i] = imagedata;
			}
		    	
		    	
		//} catch (IOException e) {
		//    e.printStackTrace();
		} finally {
		    try {
			bytechannel.close();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		/*
		try {
			// Read header
			ByteBuffer headerbytes = ByteBuffer.allocate(TmpFileHeaderRA.HEADER_SIZE-1);
			bytechannel.read(headerbytes);
			headerbytes.rewind();
			tmpheader = new TmpFileHeaderRA(headerbytes);
			
			// Read image offsets
			//ByteBuffer shift = ByteBuffer.allocate(tmpheader.index2 - tmpheader.index1);
			//bytechannel.read(shift);
			
			ByteBuffer offsetsbytes = ByteBuffer.allocate(numImages());
			bytechannel.read(offsetsbytes);
			
			byte[] imageoffsets = offsetsbytes.array();

			// Read images
			tmpimages = new ByteBuffer[numImages()];
			ByteBuffer imagesdata = BufferUtility.readRemaining(bytechannel);
			int offset = tmpheader.imagedata;
			for (int i = 0; i < tmpimages.length; i++) {
				byte imageoffset = imageoffsets[i];

				//System.out.println("Imageoffset: " + imageoffset);
				
				// Skip empty tiles
				if (imageoffset == 0xff) {
				    	tmpimages[i] = null;
					continue;
				}
				
				// Read data at given offset
				ByteBuffer imagedata = ByteBuffer.allocate(width() * height());
				imagesdata.position(offset*i + imageoffset * width() * height());
				imagesdata.get(imagedata.array());
				
				tmpimages[i] = imagedata;
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
		}*/
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

		return FORMAT_RGBA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImagesData() {

		return new ReadableByteChannelAdapter(tmpimages);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return tmpheader.height & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int numImages() {

		return tmpheader.numtiles & 0xffff;
	}

	public ByteBuffer getImage(int index) {
		if (this.tmpimages[index] != null) { 
		    this.tmpimages[index].rewind();
		}
		
		return this.tmpimages[index];
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return tmpheader.width & 0xffff;
	}
	
	public int getWidthInTiles() {
	    return tmpheader.tilewidth & 0xFFFF;
	}
	
	public int getHeightInTiles() {
	    return tmpheader.tileheight & 0xFFFF;
	}
}
