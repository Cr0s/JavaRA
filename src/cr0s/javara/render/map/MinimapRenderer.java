package cr0s.javara.render.map;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;

import cr0s.javara.entity.Entity;
import cr0s.javara.main.Main;
import cr0s.javara.render.World;

public class MinimapRenderer {
    
    private int width, height;
    private Image minimapImage;
    private World w;
    
    public MinimapRenderer(World aWorld, int aWidth, int aHeight) {
	this.w = aWorld;
	
	this.width = aWidth;
	this.height = aHeight;
	
	try {
	    this.minimapImage = new Image(width, height);
	} catch (SlickException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void updateMinimap(Color filterColor) {
	try {
	    Graphics gr = this.minimapImage.getGraphics();


	    // Render terrain
	    for (int x = 0; x < width; x++) {
		for (int y = 0; y < height; y++) {
		    int r, g, b;

		    Color targetColor = getMapCellColor((int) (w.getMap().getBounds().getMinX() / 24 + x), (int) (w.getMap().getBounds().getMinY() / 24 + y));


		    gr.setColor(targetColor.multiply(filterColor));
		    gr.fillRect(x, y, 1, 1);
		}
	    }

	    for (Entity e : w.getEntitiesList()) {
		int cellPosX = (int) (e.posX - w.getMap().getBounds().getMinX()) / 24;
		int cellPosY = (int) (e.posY - w.getMap().getBounds().getMinY()) / 24;

		if (Main.getInstance().getPlayer().getShroud() != null && !Main.getInstance().getPlayer().getShroud().isExplored(cellPosX, cellPosY)) {
		    //continue;
		}

		gr.setColor(e.owner.playerColor.multiply(filterColor));
		gr.fillRect(cellPosX, cellPosY, (int) e.sizeWidth / 24, (int) e.sizeHeight / 24);
	    }
	    
	    gr.flush();
	} catch (SlickException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}	
    }
    
    public Image getImage() {
	return this.minimapImage;
    }
    
    private Color getMapCellColor(int cellX, int cellY) {
	if (Main.getInstance().getPlayer().getShroud() != null && !Main.getInstance().getPlayer().getShroud().isExplored(cellX, cellY)) {
	    return Color.black;
	}
	
	int surfaceId = w.getMap().getSurfaceIdAt(cellX, cellY);
	Color c = w.getMap().getTileSet().terrainColors.get(surfaceId);
	
	if (c != null) {
	    return c;
	} else {
	    return w.getMap().getTileSet().terrainColors.get(TileSet.SURFACE_CLEAR_ID);
	}
    }
}
