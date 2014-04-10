package cr0s.javara.resources;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;

import redhorizon.filetypes.pal.PalFile;
import redhorizon.filetypes.tmp.TmpFileRA;

public class TmpTexture {
	private TmpFileRA tmp;
	public int width, height; // size of one single frame
	public int numImages;
	
	private final static int TILE_WIDTH= 24, TILE_HEIGHT = 24;
	
	private final int PALETTE_SIZE = 256;
	private final int BYTE_MASK = 0xFF;
	private final int SHADOW_COLOR = 0x04; 
	private final int SHADOW_ALPHA_LEVEL = 64;
	private final int NON_TRANSPARENT_ALPHA = 255;
	
	private final String type;
	private Image combinedImage = null;
	
	private HashMap<Integer, Image> frameCache = new HashMap<>();
	
	public TmpTexture(TmpFileRA aTmp, String aType) {
		this.width = aTmp.width();
		this.height = aTmp.height();
		this.numImages = aTmp.numImages();
		
		this.tmp = aTmp;
		this.type = aType;
	}
	
	
	private ImageBuffer applyPalette(int index) {
		PalFile pal = ResourceManager.getInstance().getPaletteByName(type + ".pal");
		Color[] colors = new Color[PALETTE_SIZE];
		
		ByteBuffer bBuffer = pal.getPaletteDataByteBuffer();

		// Use palette data
		for (int i = 0; i < PALETTE_SIZE; i++) {
			int r = bBuffer.get() & BYTE_MASK;
			int g = bBuffer.get() & BYTE_MASK;
			int b = bBuffer.get() & BYTE_MASK;
		
			colors[i] = new Color(r, g, b);
 		}
		
		bBuffer.rewind();		
		
		ByteBuffer bb = tmp.getImage(index);
		ImageBuffer imgbuf = new ImageBuffer(width, height);
		for (int y = 0; y < width; y++) {
			for (int x = 0; x < height; x++) {
				int colorValue = bb.get() & BYTE_MASK;
				
				// Check for shadow color
				if (colorValue != SHADOW_COLOR) {
					imgbuf.setRGBA(x, y, colors[colorValue].getRed(), colors[colorValue].getGreen(), colors[colorValue].getBlue(), (colorValue == 0) ? 0 : NON_TRANSPARENT_ALPHA);
				} else {
					// Replace shadow color with black color with transparency
					imgbuf.setRGBA(x, y, 0, 0, 0, SHADOW_ALPHA_LEVEL); 
				}
			}
		}
		
		bb.rewind();
		
		return imgbuf;
	}
	
	/**
	 * Gets combined image by height of all .TMP tiles.
	 * @param remapColor
	 * @return big combined image
	 */
	public Image getAsCombinedImage() {
	    if (this.combinedImage != null) {
		return this.combinedImage;
	    }
	    
	    //System.out.println("Tmp width: " + this.width + " | height: " + this.height);
		int combinedHeight = this.height * numImages;
		int combinedWidth = this.width * numImages;
		
		// Image is not cached
		// Create big sized common image, which will combine all frames of source .TMP
		ImageBuffer imgBuf = new ImageBuffer(combinedWidth, combinedHeight);
		
		System.out.println(tmp.getWidthInTiles() + " x " + tmp.getHeightInTiles());
		
		for (int tileY = 0; tileY < tmp.getHeightInTiles(); tileY++) {
		    for (int tileX = 0; tileX < tmp.getWidthInTiles(); tileX++) {
			int imgIndex = tmp.getWidthInTiles() * tileY + tileX;

			ImageBuffer frameBuf = null;
			if (tmp.getImage(imgIndex) == null) {
			    // Create empty image
			    frameBuf = new ImageBuffer(width, height);
			} else {
			    frameBuf = applyPalette(imgIndex);    
			}

						
			
			Image frame = frameBuf.getImage();
			
			byte[] rgba = frameBuf.getRGBA();
			
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int r, g, b, a;
					Color c = frame.getColor(x, y);
					r = c.getRed();
					g = c.getGreen();
					b = c.getBlue();
					a = c.getAlpha();
					
					imgBuf.setRGBA(tileX * width + x, tileY * height + y, r, g, b, a);
				}
			}			
		    }
		}
			
		// Cache result and return
		this.combinedImage = imgBuf.getImage();
		return this.combinedImage;
	}
	
	public Image getByIndex(int index) {
	    if (this.frameCache.containsKey(index)) {
		return frameCache.get(index);
	    } else {
		Image img = applyPalette(index).getImage();
		frameCache.put(index, img);
		
		return img;
	    }
	}
}
