package cr0s.javara.render.map;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;

public class ResourcesLayer {
    ResourceCell[][] resources;
    private TileMap map;
    
    public ResourcesLayer(TileMap aMap) {
	this.map = aMap;
	this.resources = new ResourceCell[this.map.getWidth()][this.map.getHeight()];
    }
    
    int getAdjacentCellsWith(byte t, int i, int j)
    {
	int sum = 0;
	for (int u = -1; u < 2; u++) {
	    for (int v = -1; v < 2; v++) {
		if (resources[i + u][j + v] != null && resources[i + u][j + v].type == t) {
		    ++sum;
		}
	    }
	}
	
	return sum;
    }
    
    public void setInitialDensity() {
	for (int x = 0; x < this.map.getWidth(); x++) {
	    for (int y = 0; y < this.map.getHeight(); y++) {
		ResourceCell cell = resources[x][y];
		
		if (cell != null) {
		    int adjacent = getAdjacentCellsWith(cell.type, x, y);
		    int density = lerp(0, cell.maxDensity, adjacent, 9);
		    
		    cell.density = (byte) (density & 0xFF);
		}
	    }
	}
    }
    
    public static int lerp(int a, int b, int mul, int div )
    {
	return a + (b - a) * mul / div;
    }
    
    public class ResourceCell {
	public ResourceCell (byte aType, byte aVariant) {
	    this.type = aType;
	    this.variant = aVariant;
	    
	    this.density = 0;
	    this.maxDensity = (byte) ((aType == 1) ? 12 : 3);
	}
	
	public byte maxDensity;
	public byte density;
	public byte type;
	public byte variant; 
	
	public String getSpriteName() {
	    if (type == 1) {
		return "gold0" + (this.variant + 1) + ".tem";
	    } else if (type == 2) {
		return "gem0" + (this.variant + 1) + ".tem";
	    }
	    
	    
	    return null;
	}
	
	public int getFrameIndex() {
	    return lerp(0, maxDensity - 1, density - 1, maxDensity);
	}
    }

    public void renderAll(Graphics g) {
	// Draw tiles layer
	for (int y = 0; y < this.map.getHeight(); y++) {
	    for (int x = 0; x < this.map.getWidth(); x++) {
		if (x < (int) -Main.getInstance().getCamera().offsetX / 24 - 1
			|| x > (int) -Main.getInstance().getCamera().offsetX / 24 + (int) Main.getInstance().getContainer().getWidth()
			/ 24 + 1) {
		    continue;
		}

		if (y < (int) -Main.getInstance().getCamera().offsetY / 24 - 1
			|| y > (int) -Main.getInstance().getCamera().offsetY / 24 + (int) Main.getInstance().getContainer().getHeight()
			/ 24 + 1) {
		    continue;
		}

		if (Main.getInstance().getPlayer().getShroud() != null && Main.getInstance().getPlayer().getShroud().isAreaShrouded(x, y, 2, 2)) {
		    continue;
		}

		if (this.resources[x][y] != null) {
		    byte index = (byte) (this.resources[x][y].getFrameIndex() & 0xFF);
		    
		    Point sheetPoint = map.getTheater().getShpTexturePoint(this.resources[x][y].getSpriteName());
		    
		    int sX = (int) sheetPoint.getX();
		    int sY = (int) sheetPoint.getY();

		    if (sX != -1 && sY != -1) {
			this.map.getTheater().getSpriteSheet().renderInUse(x * 24, y * 24, sX / 24, (sY / 24) + index);
		    }		    
		}
	    }
	}	
    }
    
    public void renderCell(int x, int y) {
	if (this.resources[x][y] != null) {
	    byte index = (byte) (this.resources[x][y].getFrameIndex() & 0xFF);
	    
	    Point sheetPoint = map.getTheater().getShpTexturePoint(this.resources[x][y].getSpriteName());
	    
	    int sX = (int) sheetPoint.getX();
	    int sY = (int) sheetPoint.getY();

	    if (sX != -1 && sY != -1) {
		this.map.getTheater().getSpriteSheet().renderInUse(x * 24, y * 24, sX / 24, (sY / 24) + index);
	    }		    
	}	
    }

    public boolean isCellEmpty(int x, int y) {
	return this.resources[x][y] == null;
    }
    
    public int harvestCell(int x, int y) {
	ResourceCell resource = this.resources[x][y];
	
	if (resource != null) {
	    if (resource.density >= 0) {
		resource.density--;
		
		if (resource.density <= 0) {
		    this.resources[x][y] = null;
		}
		
		return resource.type;
	    }
	}
	
	return -1;
    }

    public boolean isCellEmpty(Point targetCell) {
	return isCellEmpty((int) targetCell.getX(), (int) targetCell.getY());
    }

    public int harvestCell(Point currentCell) {
	return harvestCell((int) currentCell.getX(), (int) currentCell.getY());
    }
}
