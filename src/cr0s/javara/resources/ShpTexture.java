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
	private SpriteSheet sh;
	private ShpFileCnc shp;
	public int width, height; // size of one single frame
	
	private int[] remapIndexes = { 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95 };
	private boolean[] isRemap = new boolean[256];
	
	public ShpTexture(ShpFileCnc shp) {
		for (int i = 0; i < remapIndexes.length; i++) {
			isRemap[remapIndexes[i]] = true;
		}
		
		this.shp = shp;
	}
	
	public ImageBuffer getAsImage(int index, Color remapColor) {
		int remapR = remapColor.getRed();
		int remapG = remapColor.getGreen();
		int remapB = remapColor.getBlue();
	
		PalFile pal = ResourceManager.getInstance().getPaletteByName("temperat.pal");
		
		ByteBuffer bBuffer = pal.getPaletteDataByteBuffer();
		
		// Remap palette
		try {
			Color[] colors = new Color[256];
			for (int i = 0; i < 256; i++) {
				int r = bBuffer.get() & 0xFF;
				int g = bBuffer.get() & 0xFF;
				int b = bBuffer.get() & 0xFF;
				
				colors[i] = new Color(r, g, b);
				
				// This color needs to be remapped to team-related color
				if (isRemap[i]) {
					float[] hsbRemap = java.awt.Color.RGBtoHSB(remapR, remapG, remapB, null);
					float[] hsbSource = java.awt.Color.RGBtoHSB(r, g, b, null);
					
					java.awt.Color newColor = java.awt.Color.getHSBColor(hsbRemap[0], hsbRemap[1], hsbSource[2]);
					colors[i] = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
				}
	 		}
			
			bBuffer.rewind();
			
			ByteBuffer bb = shp.getImage(index);
			ImageBuffer imgbuf = new ImageBuffer(shp.width(), shp.height());
			for (int y = 0; y < shp.height(); y++) {
				for (int x = 0; x < shp.height(); x++) {
					int colorValue = bb.get() & 0xFF;
					
					if (colorValue != 0x04) {
						imgbuf.setRGBA(x, y, colors[colorValue].getRed(), colors[colorValue].getGreen(), colors[colorValue].getBlue(), (colorValue == 0) ? 0 : 255);
					} else {
						// Shadows
						imgbuf.setRGBA(x, y, 0, 0, 0, 64);
					}
				}
			}
			
			bb.rewind();
			return imgbuf;
		} finally {}
	}
}
