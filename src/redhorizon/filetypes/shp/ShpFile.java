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

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_INDEXED;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Abstract SHP file class containing only the parts similar between both C&C
 * and Dune 2 SHP files.
 * 
 * @author Emanuel Rabina
 * @param <H> SHP file header implementation type.
 */
public abstract class ShpFile<H extends ShpFileHeader> extends AbstractFile
	implements ImagesFile, Paletted {

	// Read-from information
	H shpfileheader;
	ByteBuffer[] shpimages;

	// Save-to information
	static final String PARAM_WIDTH   = "-w:";
	static final String PARAM_HEIGHT  = "-h:";
	static final String PARAM_NUMIMGS = "-n:";

	static final int MAX_WIDTH   = 65535;
	static final int MAX_NUMIMGS = 65535;

	/**
	 * Constructor, creates a SHP file with the given name.
	 * 
	 * @param name The name of this file.
	 */
	ShpFile(String name) {

		super(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ColourFormat format() {
		return FORMAT_INDEXED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImagesData() {

		return new ReadableByteChannelAdapter(shpimages);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int numImages() {

		return shpfileheader.numimages & 0xffff;
	}
	
	public ByteBuffer getImage(int index) {
		this.shpimages[index].rewind();
		return this.shpimages[index];
	}
}
