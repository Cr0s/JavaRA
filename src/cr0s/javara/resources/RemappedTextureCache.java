package cr0s.javara.resources;

import java.util.WeakHashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;

/**
 * Contains textures from .shp source with remapped team colors
 * 
 * @author Cr0s
 *
 */
public class RemappedTextureCache {
	private static RemappedTextureCache instance = null;
	private WeakHashMap<String, Image> remappedTextures;
	
	private RemappedTextureCache() {
		this.remappedTextures = new WeakHashMap<>();
	}
	
	public static RemappedTextureCache getInstance() {
		if (instance == null) {
			instance = new RemappedTextureCache();
		}
		
		return instance;
	}
		
	/**
	 * Checks cache for specified texture and color
	 * @param textureName name of texture, for example "mcv.shp"
	 * @param remapColor remapping color (team color)
	 * @param textureIndex index in texture sheet of .shp (number of frame) or -1 if we need all frames as one image
	 * @return null if image is not cached
	 */
	public Image checkInCache(String textureName, Color remapColor, int textureIndex) {
		// Generate key to search
		// searchKey = textureName + r + g + b + textureIndex
		String cacheKey = textureName + new Integer(remapColor.getRed()).toString() + new Integer(remapColor.getGreen()).toString() + new Integer(remapColor.getBlue()).toString() + textureIndex;
		
		return remappedTextures.get(cacheKey);
	}
	
	/**
	 * Puts specified image as ImageBuffer in cache
	 * @param image Image of remapped image
	 * @param textureName texture name
	 * @param remapColor remapping color (team color)
	 * @param textureIndex index of texture, or -1 if we need all .SHP frames as one whole image
	 */
	public void putInCache(Image image, String textureName, Color remapColor, int textureIndex) {
		String cacheKey = textureName + new Integer(remapColor.getRed()).toString() + new Integer(remapColor.getGreen()).toString() + new Integer(remapColor.getBlue()).toString() + textureIndex;	
		
		if (!this.remappedTextures.containsKey(cacheKey)) {
			this.remappedTextures.put(cacheKey, image);
		} else {
			System.err.println("[Warning] Remapped textures cache warning: trying to put in cache image that already cached.");
		}
	}
}
