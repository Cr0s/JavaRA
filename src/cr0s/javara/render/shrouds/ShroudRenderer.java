package cr0s.javara.render.shrouds;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.main.Main;
import cr0s.javara.render.World;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.resources.ResourceManager;

public class ShroudRenderer {
    private ShroudTile[][] shroudMap;
    private boolean[][] explorationMap;
    private short[] spriteMap;
    
    /** 
     * Bitfield of shroud directions for each frame. 
     * Lower four bits are corners clockwise from TL; upper four are edges clockwise from top.
     */
    private short[] index = new short[] { 255, 16, 32, 48, 64, 80, 96, 112, 128, 144, 160, 176, 192, 208, 224, 240, 20, 40, 56, 65, 97, 130, 148, 194, 24, 33, 66, 132, 28, 41, 67, 134, 1, 2, 4, 8, 3, 6, 12, 9, 7, 14, 13, 11, 5, 10, 15, 255 };
	
    private SpriteSheet shroudsSheet;
    private World w;
    
    public ShroudRenderer(World aW) {
	this.w = aW;
	
	this.shroudMap = new ShroudTile[w.getMap().getWidth()][w.getMap().getHeight()];
	for (int x = 0; x < w.getMap().getWidth(); x++) {
	    for (int y = 0; y < w.getMap().getHeight(); y++) {
		this.shroudMap[x][y] = new ShroudTile(x * 24, y * 24, 0);
	    }
	}	
	
	this.shroudsSheet = new SpriteSheet(ResourceManager.getInstance().getConquerTexture("shadow.shp").getAsCombinedImage(null, true), 24, 24);
	System.out.println("Loaded shroud sheet: " + shroudsSheet.toString());
	
	spriteMap = new short[256];
	for (short i = 0; i < index.length; i++)
	    spriteMap[index[i]] = i;
	
    }
    
    public void renderShrouds(Graphics g) {
	shroudsSheet.startUse();
	
	for (int x = 0; x < w.getMap().getWidth(); x++) {
	    for (int y = 0; y < w.getMap().getHeight(); y++) {
		    if (x < (int) -w.getCamera().offsetX / 24 - 2
			    || x > (int) -w.getCamera().offsetX / 24 + (int) Main.getInstance().getContainer().getWidth() / 24 + 2) {
			continue;
		    }

		    if (y < (int) -w.getCamera().offsetY / 24 - 2
			    || y > (int) -w.getCamera().offsetY / 24 + (int) Main.getInstance().getContainer().getHeight() / 24 + 2) {
			continue;
		    }
		    
		    this.shroudMap[x][y].render(g);
	    }
	}
	
	shroudsSheet.endUse();
    }
    
    public void update(Shroud s) {
	// Observer's shroud
	if (s == null) {
	    for (int x = 0; x < w.getMap().getWidth(); x++) {
		for (int y = 0; y < w.getMap().getHeight(); y++) {
		    this.shroudMap[x][y].type = observerShroudedEdges(x * 24, y * 24, true);
		}
	    }	    
	} else {
	    for (int x = 0; x < w.getMap().getWidth(); x++) {
		for (int y = 0; y < w.getMap().getHeight(); y++) {
		    this.shroudMap[x][y].type = getShroudedEdges(s, x, y, true);
		}
	    }	    
	}
    }
       
    private int getShroudedEdges(Shroud s, int x, int y, boolean useExtendedIndex)
    {
	if (!s.isExplored(x, y))
	    return 15;
	
	//If a side is shrouded then we also count the corners
	int u = 0;
	if (!s.isExplored(x, y - 1)) {
	    u |= 0x13;
	}
	if (!s.isExplored(x + 1, y)) {
	    u |= 0x26;
	}
	if (!s.isExplored(x, y + 1)) {
	    u |= 0x4C;
	}
	if (!s.isExplored(x - 1, y)) {
	    u |= 0x89;
	}

	int uside = u & 0x0F;
	if (!s.isExplored(x - 1, y - 1)) {
	    u |= 0x01;
	}
	if (!s.isExplored(x + 1, y - 1)) {
	    u |= 0x02;
	}
	if (!s.isExplored(x + 1, y + 1)) {
	    u |= 0x04;
	}
	if (!s.isExplored(x - 1, y + 1)) {
	    u |= 0x08;
	}

	return useExtendedIndex ? u ^ uside : u & 0x0F;
    }
	
    private int observerShroudedEdges(int x, int y, boolean useExtendedIndex)
    {
	if (x > w.getMap().getBounds().getMaxX() || x < w.getMap().getBounds().getMinX() || y > w.getMap().getBounds().getMaxY() || y < w.getMap().getBounds().getMinY()) {
	    return 15;
	}
	
	// Set side bit
	int u = 0;
	if (y == w.getMap().getBounds().getMinY()) {
	    u |= 0x13;
	} 
	if (x == w.getMap().getBounds().getMaxX()) {
	    u |= 0x26; 
	} 
	if (y == w.getMap().getBounds().getMaxY()) {
	    u |= 0x4C;
	} 
	if (x == w.getMap().getBounds().getMinX()) {
	    u |= 0x89;
	}

	// Set angle bit
	int uside = u & 0x0F;
	if (x == w.getMap().getBounds().getMinX() && y == w.getMap().getBounds().getMinY() - 24) {
	    u |= 0x01;
	}
	if (x == w.getMap().getBounds().getMaxX() - 24 && y == w.getMap().getBounds().getMinY()) {
	    u |= 0x02;
	}
	if (x == w.getMap().getBounds().getMaxX() - 24 && y == w.getMap().getBounds().getMaxY()) {
	    u |= 0x04;
	}
	if (x == w.getMap().getBounds().getMinX() && y == w.getMap().getBounds().getMaxY() - 24) {
	    u |= 0x08;
	}

	return useExtendedIndex ? u ^ uside : u & 0x0F;
    }
	
    private class ShroudTile {    
	public int type = 0;
	
	public int x, y;
	
	public ShroudTile(int aX, int aY, int aType) {
	    this.x = aX;
	    this.y = aY;
	    
	    this.type = aType;
	}
	
	public void render(Graphics g) {
	    if (this.type == 0) {
		return;
	    }
	    
	    if (this.type == 15) {
		ShroudRenderer.this.shroudsSheet.getSubImage(0, 15).drawEmbedded(x - 12, y - 12, 24, 24);
	    } else
		ShroudRenderer.this.shroudsSheet.renderInUse(x - 12, y - 12, 0, ShroudRenderer.this.spriteMap[this.type]);
	}
    }
}
