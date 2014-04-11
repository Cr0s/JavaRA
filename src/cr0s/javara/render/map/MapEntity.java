package cr0s.javara.render.map;

import cr0s.javara.resources.ShpTexture;
import cr0s.javara.resources.TmpTexture;

/**
 * Describes drawable map entity. For example, trees or mines.
 * @author Cr0s
 */
public class MapEntity {
    private int x;
    private int y;
    
    private ShpTexture texture;
 
    public MapEntity(int aX, int aY, ShpTexture shpTex) {
	this.x = aX;
	this.y = aY;
	
	this.texture = shpTex;
    }
    
    public ShpTexture getTexture() {
	return this.texture;
    }
    
    public int getX() {
	return this.x;
    }
    
    public int getY() {
	return this.y;
    }
}
