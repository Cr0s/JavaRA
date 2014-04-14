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
    private String footprintString;
    private int[][] footprintCells;
    
    private int width, height;
    
    public MapEntity(int aX, int aY, ShpTexture shpTex, String footprint, String dimensions) {
	this.x = aX;
	this.y = aY;
	
	this.texture = shpTex;
	
	this.footprintString = footprint;
	
	this.width = Integer.valueOf(dimensions.substring(0, dimensions.indexOf(",")));
	this.height = Integer.valueOf(dimensions.substring(dimensions.indexOf(",") + 1));
	
	this.footprintCells = new int[width][height];
	generateCellsFromFootprint(footprint, this.footprintCells);
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
    
    /**
     * Generate 2D cells blocking description from OpenRA footprint format string.
     * @param footprint footprint format string
     * @param fc 2D array of cells
     */
    private void generateCellsFromFootprint(String footprint, int[][] fc) {
	int length = footprint.length();
	int x = 0, y = 0;
	
	for (int i = 0; i < length; i++) {
	    char c = footprint.charAt(i);

	    switch (c) {
	    case '_':
		fc[x][y] = TileSet.SURFACE_CLEAR_ID;
		x++;
		break;

	    case 'x':
		fc[x][y] = -1;
		x++;
		break;

	    case ' ':
		x = 0;
		y++;
		break;

	    default:
		fc[x][y] = 0;
		x++;
		break;
	    }
	}
    }
    
    public int getWidth() {
	return this.width;
    }
    
    public int getHeight() {
	return this.height;
    }
    
    public int[][] getFootprintCells() {
	return this.footprintCells;
    }
}
