package cr0s.javara.render.map;

import java.util.LinkedList;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;

import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.TmpTexture;

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
    
    private final int SHEET_SIZE = 4096;
    private int currentSheetX = 0, currentSheetY = 0;
    private int maximumYOffset = 0;
    
    /**
     * Creates new theater from specified TileSet.
     * @param tileSet
     */
    public Theater(final TileSet aTileSet) {
	this.tileSet = aTileSet;
	
	generateSpriteSheet();
    }
    
    private void generateSpriteSheet() {
	ImageBuffer ib = new ImageBuffer(SHEET_SIZE, SHEET_SIZE);
	
	for (String name : tileSet.getTiles().values()) {
	    
	    System.out.println("Loading in " + tileSet.getSetName() + ".mix file: " + name + ".tem");
	    TmpTexture t = ResourceManager.getInstance().getTemplateTexture(tileSet.getSetName(), name + ".tem");
	    
	    putTextureInSheet(ib, t);
	}
	
	this.spriteSheet = new SpriteSheet(ib.getImage(), 24, 24);
    }

    private void putTextureInSheet(ImageBuffer sheet, TmpTexture texture) {
	if (texture == null) {
	    return;
	}
	
	if (texture.height * texture.numImages > maximumYOffset) {
	    this.maximumYOffset = texture.height * texture.numImages;
	}
	
	// Overflowed, lets move down and reset to left side
	if (currentSheetX + texture.width > SHEET_SIZE) {
	    currentSheetY += maximumYOffset;
	    currentSheetX = 0;
	}
	
	texture.setSpriteSheetCoords(new Point(currentSheetX, currentSheetY));

	for (int i = 0; i < texture.numImages; i++) {
	    Image img = texture.getByIndex(i);
	    
	    int deployX = currentSheetX;
	    int deployY = currentSheetY + (i * img.getHeight());
	    
	    // Copy texture into sheet
	    for (int y = 0; y < img.getHeight(); y++) {
		for (int x = 0; x < img.getWidth(); x++) {
		    Color c = img.getColor(x, y);

		    int r, g, b, a;
		    r = c.getRed();
		    g = c.getGreen();
		    b = c.getBlue();
		    a = c.getAlpha();

		    sheet.setRGBA(deployX + x, deployY + y, r, g, b, a);
		}
	    }
	}
	
	currentSheetX += texture.width;
	currentSheetY = 0;
    }
    
    public SpriteSheet getSpriteSheet() {
	return this.spriteSheet;
    }
    
    public Point getTileTextureSheetCoord(TileReference<Short, Byte> tile) {
	String tileName = this.tileSet.getTiles().get(Integer.valueOf(tile.getTile()));
	TmpTexture t = ResourceManager.getInstance().getTemplateTexture(tileSet.getSetName(), tileName + ".tem");

	int index = (short) (tile.getIndex() & 0xFF);

	if (t.isInSpriteSheet()) {
	    return t.getSpriteSheetCoords();
	} else return new Point(-1, -1);
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
    
    public TmpTexture getTileTmp(TileReference<Short, Byte> tile) {
	String tileName = this.tileSet.getTiles().get(Integer.valueOf(tile.getTile()));
	
	if (tileName != null) {
	    return ResourceManager.getInstance().getTemplateTexture(this.tileSet.getSetName(), tileName + ".tem");
	} else {
	    return ResourceManager.getInstance().getTemplateTexture(this.tileSet.getSetName(), "clear1.tem");
	}	
    }
}
