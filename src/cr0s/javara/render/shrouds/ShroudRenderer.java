package cr0s.javara.render.shrouds;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.render.World;
import cr0s.javara.resources.ResourceManager;

public class ShroudRenderer {
    private ShroudTile[][] shroudMap;
    private boolean[][] explorationMap;
    private int[] spriteMap;
    
    public int[] index = new int[] { 12, 9, 8, 3, 1, 6, 4, 2, 13, 11, 7, 14 };
	
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
	
	this.shroudsSheet = new SpriteSheet(ResourceManager.getInstance().getConquerTexture("shadow.shp").getAsCombinedImage(null), 24, 24);
	System.out.println("Loaded shroud sheet: " + shroudsSheet.toString());
	
	spriteMap = new int[16];
	for (int i = 0; i < index.length; i++)
	    spriteMap[index[i]] = i;
	
    }
    
    public void renderShrouds(Graphics g) {
	shroudsSheet.startUse();
	
	for (int x = 0; x < w.getMap().getWidth(); x++) {
	    for (int y = 0; y < w.getMap().getHeight(); y++) {
		//if (w.getCamera().viewportRect.contains(x * 24, y * 24)) {
		  //  System.out.println("Rendering shroud " + x + "; " + y);
		    this.shroudMap[x][y].render(g);
		//}
	    }
	}
	
	shroudsSheet.endUse();
    }
    
    public void update(Shroud s) {
	// Observer's shroud
	if (s == null) {
	    for (int x = 0; x < w.getMap().getWidth(); x++) {
		for (int y = 0; y < w.getMap().getHeight(); y++) {
		    this.shroudMap[x][y].type = observerShroudedEdges(x * 24, y * 24, false);
		}
	    }	    
	} else {
	    for (int x = 0; x < w.getMap().getWidth(); x++) {
		for (int y = 0; y < w.getMap().getHeight(); y++) {
		    this.shroudMap[x][y].type = getShroudedEdges(s, x, y, false);
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

	//RA provides a set of frames for tiles with shrouded
	//corners but unshrouded edges. We want to detect this
	//situation without breaking the edge -> corner enabling
	//in other combinations. The XOR turns off the corner
	//bits that are enabled twice, which gives the behavior
	//we want here.
	return useExtendedIndex ? u ^ uside : u & 0x0F;
    }
	
    private int observerShroudedEdges(int x, int y, boolean useExtendedIndex)
    {
	if (x < w.getMap().getBounds().getMinX() || y < w.getMap().getBounds().getMinY()) {
	    return 15;
	} else if (x > w.getMap().getBounds().getMaxX() - 24 || y > w.getMap().getBounds().getMaxY() - 24) {
	    return 15;
	}
	
	// Set side bit
	short u = 0;
	if (y == w.getMap().getBounds().getMinY()) {
	    u |= 0x13;
	}
	if (x == w.getMap().getBounds().getMaxX() - 24) {
	    u |= 0x26;
	}
	if (y == w.getMap().getBounds().getMaxY() - 24) {
	    u |= 0x4C;
	}
	if (x == w.getMap().getBounds().getMinX()) {
	    u |= 0x89;
	}

	// Set angle bit
	short uside = (short) (u & 0x0F);
	if (x == w.getMap().getBounds().getMinX() && y == w.getMap().getBounds().getMinY()) {
	    u |= 0x01;
	}
	if (x == w.getMap().getBounds().getMaxX() - 24 && y == w.getMap().getBounds().getMinY()) {
	    u |= 0x02;
	}
	if (x == w.getMap().getBounds().getMaxX() - 24 && y == w.getMap().getBounds().getMaxY() - 24) {
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
	    this.x = aY;
	    this.y = aX;
	    
	    this.type = aType;
	}
	
	public void render(Graphics g) {
	    if (this.type == 0) {
		return;
	    }
	    
	    if (this.type == 15) {
		ShroudRenderer.this.shroudsSheet.getSubImage(0, 15).drawEmbedded(x - 12, y - 12, 24, 24);
	    } else
		ShroudRenderer.this.shroudsSheet.getSubImage(0, ShroudRenderer.this.spriteMap[this.type]).drawEmbedded(x - 12, y - 12, 24, 24);
	}
    }
}
