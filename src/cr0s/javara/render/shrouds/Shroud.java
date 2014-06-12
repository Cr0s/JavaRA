package cr0s.javara.render.shrouds;

import cr0s.javara.gameplay.Player;
import cr0s.javara.render.World;
import cr0s.javara.util.Pos;

public class Shroud {
    private boolean explorationMap[][];
    
    private World w;
    private Player p;
    
    private ShroudRenderer sr;
    
    public Shroud(World aW, Player aP) {
	this.w = aW;
	this.p = aP;
	
	this.sr = new ShroudRenderer(aW);
	
	this.explorationMap = new boolean[w.getMap().getWidth()][w.getMap().getHeight()];
    }
    
    public boolean isExplored(int tileX, int tileY) {
	if (tileX < 0 || tileY < 0 || tileX >= w.getMap().getWidth() || tileY >= w.getMap().getHeight()) {
	    return false;
	}
	
	return explorationMap[tileX][tileY];
    }
    
    public void exploreRange(int tileX, int tileY, int range) {
	int maxX = Math.min(tileX + range, (int) (this.w.getMap().getBounds().getMaxX() / 24) + 1);
	int maxY = Math.min(tileY + range, (int) (this.w.getMap().getBounds().getMaxY() / 24) + 1);
	
	for (int x = Math.max((int) this.w.getMap().getBounds().getMinX() / 24, tileX - range); x < maxX; x++) {
	    for (int y = Math.max((int) this.w.getMap().getBounds().getMinY() / 24, tileY - range); y < maxY; y++) {
		int dx = tileX - x;
		int dy = tileY - y;
		
		if (dx * dx + dy * dy <= range * range) {
		    this.explorationMap[x][y] = true;
		}
	    }
	}
    }
    

    public boolean isAreaShrouded(int centerX, int centerY, int width, int height) {
	for (int x = centerX - (width / 2); x <= centerX + (width / 2); x++) {
	    for (int y = centerY - (height / 2); y <= centerY + (height / 2); y++) {
		if (this.explorationMap[x][y]) {
		    return false;
		}
	    }
	}
	
	return true;
    }    
    
    public ShroudRenderer getRenderer() {
	return this.sr;
    }

    public boolean isExplored(Pos cellPos) {
	return this.isExplored((int) cellPos.getX(), (int) cellPos.getY());
    }
}
