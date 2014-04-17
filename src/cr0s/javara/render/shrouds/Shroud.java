package cr0s.javara.render.shrouds;

import cr0s.javara.gameplay.Player;
import cr0s.javara.render.World;

public class Shroud {
    private boolean explorationMap[][];
    
    private World w;
    private Player p;
    
    public Shroud(World aW, Player aP) {
	this.w = aW;
	this.p = aP;
    }
    
    public boolean isExplored(int tileX, int tileY) {
	return explorationMap[tileX][tileY];
    }
    
    public void exploreRange(int tileX, int tileY, int range) {
	int maxX = Math.min(tileX + range, this.w.getMap().getWidth());
	int maxY = Math.min(tileY + range, this.w.getMap().getHeight());
	
	for (int x = Math.max(0, tileX - range); x < maxX; x++) {
	    for (int y = Math.max(0, tileY - range); y < maxY; y++) {
		this.explorationMap[x][y] = true;
	    }
	}
    }
}
