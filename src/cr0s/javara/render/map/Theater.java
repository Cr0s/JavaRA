package cr0s.javara.render.map;

import java.util.Random;

import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.resources.ResourceManager;

/**
 * Templates and tiles cached holder.
 * @author Cr0s
 *
 */
public class Theater {
    /**
     * Set of tiles and templates to use in this theater.
     */
    private final TileSet tileSet;
    
    private Random r = new Random();
    
    private SpriteSheet spriteSheet;
    
    /**
     * Creates new theater from specified TileSet.
     * @param tileSet
     */
    public Theater(final TileSet aTileSet) {
	this.tileSet = aTileSet;
	
	// Load all Tmps
	for (String name : tileSet.getTiles().values()) {
	    System.out.println("Loading in " + tileSet.getSetName() + ".mix file: " + name + ".tem");
	    
	    ResourceManager.getInstance().getTemplateTexture(tileSet.getSetName(), name + ".tem");
	}
    }
    
    public Image getTileImage(TileReference<Short, Byte> tile) {
	String tileName = this.tileSet.getTiles().get(Integer.valueOf(tile.getTile()));
	
	if (tileName != null) {
	    int index = (int)(tile.getIndex() & 0xFF);
	    if (index == 0) {
		return ResourceManager.getInstance().getTemplateTexture(this.tileSet.getSetName(), tileName + ".tem").getAsCombinedImage();
	    } else {
		return ResourceManager.getInstance().getTemplateTexture(this.tileSet.getSetName(), tileName + ".tem").getByIndex(index);
	    }
	} else {
	    return ResourceManager.getInstance().getTemplateTexture(this.tileSet.getSetName(), "clear1.tem").getByIndex(0);
	}
    }
}
