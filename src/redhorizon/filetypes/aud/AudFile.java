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

package redhorizon.filetypes.aud;

import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.SoundBitrate;
import redhorizon.filetypes.SoundChannels;
import redhorizon.filetypes.SoundFile;
import redhorizon.filetypes.StreamingDataDecoder;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.channels.DuplicateReadOnlyByteChannel;
import static redhorizon.filetypes.SoundBitrate.*;
import static redhorizon.filetypes.SoundChannels.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Implementation of the AUD files used in Red Alert and Tiberium Dawn.  An AUD
 * file is the sound format of choice for these games, compressed using one of 2
 * schemes: IMA-ADPCM and WS-ADPCM; the latter being a Westwood proprietary
 * format.
 * <p>
 * This is a streaming file type.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("aud")
public class AudFile extends AbstractFile implements SoundFile {

	private static final byte TYPE_IMA_ADPCM = 99;
	private static final byte TYPE_WS_ADPCM  = 1;
	private static final byte FLAG_16BIT  = 0x02;
	private static final byte FLAG_STEREO = 0x01;

	private final AudFileHeader audheader;
	private SeekableByteChannel bytechannel;;
	private final ExecutorService decoderthreadpool = Executors.newCachedThreadPool();

	/**
	 * Constructor, creates a new aud file with the given name and data.
	 * 
	 * @param name		  The name of this file.
	 * @param bytechannel Data of this aud file.
	 */
	public AudFile(String name, ReadableByteChannel bytechannel) {

		super(name);

		// AUD file header
		ByteBuffer headerbytes = ByteBuffer.allocate(AudFileHeader.HEADER_SIZE);
		headerbytes.order(ByteOrder.LITTLE_ENDIAN);
		
		try {
			bytechannel.read(headerbytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		headerbytes.rewind();
		audheader = new AudFileHeader(headerbytes);

		// Store seekable channel types
		if (bytechannel instanceof SeekableByteChannel) {
			this.bytechannel = (SeekableByteChannel)bytechannel;
		}

		// If the input channel isn't seekable, create a temp file that is seekable
		else {
			File tempsounddatafile;
			try {
				tempsounddatafile = File.createTempFile(name, null);
				tempsounddatafile.deleteOnExit();
				FileChannel filechannel = FileChannel.open(Paths.get(tempsounddatafile.getAbsolutePath()), WRITE);
				filechannel.transferFrom(bytechannel, 0, audheader.filesize);
				this.bytechannel = filechannel;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SoundBitrate bitrate() {

		return (audheader.flags & FLAG_16BIT) != 0 ? BITRATE_16 : BITRATE_8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SoundChannels channels() {

		return (audheader.flags & FLAG_STEREO) != 0 ? CHANNELS_STEREO : CHANNELS_MONO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {

		try {
			bytechannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		decoderthreadpool.shutdownNow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int frequency() {

		return audheader.frequency & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getSoundData() {

		Pipe pipe = null;
		try {
			pipe = Pipe.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		decoderthreadpool.execute(new SoundDataDecoder(
				new DuplicateReadOnlyByteChannel(bytechannel), pipe.sink()));
		return pipe.source();
	}

	/**
	 * Decoder task.
	 */
	private class SoundDataDecoder extends StreamingDataDecoder {

		/**
		 * Constructor, sets the input and output for the decoding.
		 * 
		 * @param inputchannel	Channel to read the encoded input from.
		 * @param outputchannel Channel to write the decoded output to.
		 */
		private SoundDataDecoder(ReadableByteChannel inputchannel, WritableByteChannel outputchannel) {

			super(inputchannel, outputchannel);
		}

		/**
		 * Decodes the next chunk of audio data.  Assumes that the byte channel
		 * is positioned immediately after the chunk that is being passed-in.
		 * 
		 * @param chunkheader Header of the chunk to decode.
		 * @param update 2-<tt>int</tt> array, containing the latest index and
		 * 				 sample values respectively.
		 * @return Decoded sound data.
		 */
		private ByteBuffer decodeChunk(AudChunkHeader chunkheader, int[] update) {

			// Build buffers from chunk header
			ByteBuffer source = ByteBuffer.allocate(chunkheader.filesize & 0xffff);
			source.order(ByteOrder.LITTLE_ENDIAN);
			try {
				inputchannel.read(source);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			source.rewind();
			ByteBuffer dest = ByteBuffer.allocate(chunkheader.datasize & 0xffff);
			dest.order(ByteOrder.LITTLE_ENDIAN);
			
			// Decode
			switch (audheader.type) {
			case TYPE_WS_ADPCM:
				CodecUtility.decode8bitWSADPCM(source, dest);
				break;
			case TYPE_IMA_ADPCM:
				CodecUtility.decode16bitIMAADPCM(source, dest, update);
				break;
			}
			return dest;
		}

		/**
		 * Perform decoding of the sound data.
		 */
		@Override
		protected void decode() {

			ByteBuffer chunkheaderbytes = ByteBuffer.allocate(AudChunkHeader.CHUNK_HEADER_SIZE);
			chunkheaderbytes.order(ByteOrder.LITTLE_ENDIAN);
			int[] update = new int[2];

			// Decompress the aud file data by chunks
			while (true) {
				chunkheaderbytes.clear();
				int read = -1;
				try {
					read = inputchannel.read(chunkheaderbytes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (read == -1) {
					break;
				}
				chunkheaderbytes.rewind();
				ByteBuffer chunkbytes = decodeChunk(new AudChunkHeader(chunkheaderbytes), update);
				chunkbytes.order(ByteOrder.LITTLE_ENDIAN);
				try {
					outputchannel.write(chunkbytes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String threadName() {

			return "AudFile :: " + filename + " :: Sound data decoding thread";
		}
	}
}
