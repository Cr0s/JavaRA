package cr0s.javara.render.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;

import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
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
    
    private HashMap<String, Point> shpTexturesPoints = new HashMap<>();
    
    /**
     * Textures bounds. Needs to find free place in textures sheet by checking bounds intersections.
     */
    public LinkedList<Rectangle> texturesBounds = new LinkedList<>();
    
    private TileMap map;
    
    /**
     * Creates new theater from specified TileSet.
     * @param tileSet
     */
    public Theater(final TileMap aMap, final TileSet aTileSet) {
	this.tileSet = aTileSet;
	this.map = aMap;
	
	generateSpriteSheet();
    }
    
    private void generateSpriteSheet() {
	ImageBuffer ib = new ImageBuffer(SHEET_SIZE, SHEET_SIZE);
	
	for (String name : tileSet.getTiles().values()) {
	    TmpTexture t = ResourceManager.getInstance().getTemplateTexture(tileSet.getSetName(), name + ".tem");
	    
	    putTextureInSheet(ib, t);
	}
	
	HashSet<String> addedTextures = new HashSet<>();
	
	for (MapEntity e : map.getMapEntities()) {
	    // Add only new textures
	    if (!addedTextures.contains(e.getTexture().getTextureName())) {
		System.out.println("Adding: " + e.getTexture().getTextureName());
		putTextureInSheet(ib, e.getTexture());
		
		addedTextures.add(e.getTexture().getTextureName());
	    }
	}
	
	this.spriteSheet = new SpriteSheet(ib.getImage(), 24, 24);
    }

    private void putTextureInSheet(ImageBuffer sheet, ShpTexture texture) {
	if (texture == null) {
	    return;
	}
	
	if (texture.height > maximumYOffset) {
	    this.maximumYOffset = texture.height;
	}
	
	// Check for overflow
	if (currentSheetX + texture.width > SHEET_SIZE) {
	    currentSheetY += maximumYOffset;
	    currentSheetX = 0;	    
	}
	
	// Combine texture into big image
	Image img = texture.getAsImage(0, null); // Without remapping (team) color
	
	// Determine texture bounds
	Rectangle rect = new Rectangle(0, 0, img.getWidth(), img.getHeight());
	
	// Search free place for texture
	boolean isSuccess = false;
	
	for (int sX = 0; sX + img.getWidth() < SHEET_SIZE; sX++) {
	    if (isSuccess) {
		break;
	    }
	    
	    for (int sY = 0; sY + img.getHeight() < SHEET_SIZE; sY++) {
		rect.setBounds(sX, sY, img.getWidth(), img.getHeight());
		
		// Image is fit in sheet on new position?
		if (rect.getMaxX() <= SHEET_SIZE - img.getWidth() && rect.getMaxY() <= SHEET_SIZE - img.getHeight()) {
		    // Check for intersection with other textures
		    
		    boolean isIntersectsWithOthers = false;
		    for (Rectangle r : this.texturesBounds) {
			if (r.intersects(rect)) {
			    isIntersectsWithOthers = true;
			}
		    }
		    
		    if (isIntersectsWithOthers) {
			continue;
		    }
		    
		    // No intersections, add texture to new place
		    // Copy texture into sheet
		    for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
			    Color c = img.getColor(x, y);

			    int r, g, b, a;
			    r = c.getRed();
			    g = c.getGreen();
			    b = c.getBlue();
			    a = c.getAlpha();

			    sheet.setRGBA(sX + x, sY + y, r, g, b, a);
			}
		    }		    
		    
		    // Save texture bounds for further intersect check
		    this.texturesBounds.add(rect);
		    this.shpTexturesPoints.put(texture.getTextureName(), new Point(sX, sY));
		    
		    // Shift search positions
		    currentSheetX = (int) rect.getMaxX();
		    currentSheetY = (int) rect.getMinY();
		    
		    isSuccess = true;
		    
		    break;
		}
	    }
	}
	
	/*if (!isSuccess) { // debug
	    System.out.println("Unable to add: " + texture.getTextureName());
	}*/
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
	
	Rectangle r = new Rectangle(currentSheetX, currentSheetY, texture.width, texture.height * texture.numImages);
	this.texturesBounds.add(r);
	
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

    public Point getShpTexturePoint(String textureName) {
	return this.shpTexturesPoints.get(textureName);
    }
}
