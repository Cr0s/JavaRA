package cr0s.javara.resources;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SpriteSheet;

import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.pal.PalFile;
import redhorizon.filetypes.png.PngFile;
import redhorizon.filetypes.shp.ShpFileCnc;

public class ShpTexture {
	private ShpFileCnc shp;
	public int width, height; // size of one single frame
	public int numImages;
	
	private int[] remapIndexes = { 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95 };
	
	private boolean[] isRemap = new boolean[256];
	
	public ShpTexture(ShpFileCnc shp) {
		this.width = shp.width();
		this.height = shp.height();
		this.numImages = shp.numImages();
		
		for (int i = 0; i < remapIndexes.length; i++) {
			isRemap[remapIndexes[i]] = true;
		}
		
		this.shp = shp;
	}
	
	/**
	 * Gets ImageBuffer for single .SHP frame with specified remapping color
	 * @param index index of .SHP frame
	 * @param remapColor team color
	 * @return ImageBuffer for remapped texture
	 */
	public ImageBuffer getAsImage(int index, Color remapColor) {
		// Check image in cache
		ImageBuffer res = RemappedTextureCache.getInstance().checkInCache(shp.getFileName(), remapColor, index);
		if (res != null) {
			return res;
		}
		
		// Image is not cached, remap palette, generate image and cache it
		ImageBuffer imgbuf = remapShpFrame(index, remapColor);
			
		// Cache image and return
		RemappedTextureCache.getInstance().putInCache(imgbuf, shp.getFileName(), remapColor, index);
		return imgbuf;
	}
	
	private ImageBuffer remapShpFrame(int index, Color remapColor) {
		
		PalFile pal = ResourceManager.getInstance().getPaletteByName("temperat.pal");
		Color[] colors = new Color[256];
		remapPallete(colors, pal, remapColor);
		
		ByteBuffer bb = shp.getImage(index);
		ImageBuffer imgbuf = new ImageBuffer(shp.width(), shp.height());
		for (int y = 0; y < shp.height(); y++) {
			for (int x = 0; x < shp.width(); x++) {
				int colorValue = bb.get() & 0xFF;
				
				// Check for shadow color
				if (colorValue != 0x04) {
					imgbuf.setRGBA(x, y, colors[colorValue].getRed(), colors[colorValue].getGreen(), colors[colorValue].getBlue(), (colorValue == 0) ? 0 : 255);
				} else {
					// Shadows
					imgbuf.setRGBA(x, y, 0, 0, 0, 64); // Replace shadow color with black color with 3/4 transparency
				}
			}
		}
		
		bb.rewind();
		
		return imgbuf;
	}
	
	/**
	 * Gets combined image by height of all .SHP frames
	 * @param remapColor
	 * @return
	 */
	public ImageBuffer getAsCombinedImage(Color remapColor) {
		int combinedHeight = this.height * this.numImages;
		int combinedWidth = this.width;
		
		ImageBuffer imgBuf = RemappedTextureCache.getInstance().checkInCache(shp.getFileName(), remapColor, -1);
		if (imgBuf != null) {
			return imgBuf;
		}
		
		// Image is not cached
		// Create big sized common image, which will combine all frames of source .SHP
		imgBuf = new ImageBuffer(combinedWidth, combinedHeight);
		
		for (int i = 0; i < this.numImages; i++) {
			ImageBuffer frameBuf = remapShpFrame(i, remapColor);
			
			
		}
		
		// Cache result and return
		RemappedTextureCache.getInstance().putInCache(imgBuf, shp.getFileName(), remapColor, -1);
		return imgBuf;
	}
	
	/**
	 * Apply remapping rules to source pallete
	 * @param colors new palette with remapped team colors
	 * @param pal source palette with default team colors
	 * @param remapColor remapping (team) color
	 */
	public void remapPallete(Color[] colors, PalFile pal, Color remapColor) {
		int remapR = remapColor.getRed();
		int remapG = remapColor.getGreen();
		int remapB = remapColor.getBlue();		

		ByteBuffer bBuffer = pal.getPaletteDataByteBuffer();

		// Remap palette
		for (int i = 0; i < 256; i++) {
			int r = bBuffer.get() & 0xFF;
			int g = bBuffer.get() & 0xFF;
			int b = bBuffer.get() & 0xFF;
		
			colors[i] = new Color(r, g, b);
			
			// Check in remap table, this color in source palette needs to be changed to team-related color or not
			if (isRemap[i]) {
				// We need sustain source brightness and only change color from default to remapped
				float[] hsbRemap = java.awt.Color.RGBtoHSB(remapR, remapG, remapB, null);
				float[] hsbSource = java.awt.Color.RGBtoHSB(r, g, b, null);
				
				// Applying changes, using remapped H, S values and source B value
				java.awt.Color newColor = java.awt.Color.getHSBColor(hsbRemap[0], hsbRemap[1], hsbSource[2]);
				colors[i] = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
			}
 		}
		
		bBuffer.rewind();
	}
}
