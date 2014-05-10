package cr0s.javara.render.map;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.main.Main;
import cr0s.javara.render.World;

public class MinimapRenderer {

    private int width, height;
    private World w;

    public MinimapRenderer(World aWorld, int aWidth, int aHeight) {
	this.w = aWorld;

	this.width = aWidth;
	this.height = aHeight;
    }

    public void renderMinimap(Point minimapPos, Graphics gr, Color filterColor) {
	int miniX = (int) minimapPos.getX();
	int miniY = (int) minimapPos.getY();

	final int ENTITY_ADDITIONAL_SIZE = 1; // grow entity rectangle point in pixels to see on mini map more clear
	
	if (Main.getInstance().getPlayer().getShroud() != null) {
	    gr.setColor(Color.black.multiply(filterColor));
	    gr.fillRect(miniX, miniY, width, height);
	}
	
	// Render terrain
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		int r, g, b;

		Color targetColor = getMapCellColor((int) (w.getMap().getBounds().getMinX() / 24 + x), (int) (w.getMap().getBounds().getMinY() / 24 + y));

		if (targetColor != null) {
		    gr.setColor(targetColor.multiply(filterColor));
		    gr.fillRect(miniX + x, miniY + y, 1, 1);
		}
	    }
	}

	for (Entity e : w.getEntitiesList()) {
	    int cellPosX = (int) (e.posX - w.getMap().getBounds().getMinX()) / 24;
	    int cellPosY = (int) (e.posY - w.getMap().getBounds().getMinY()) / 24;

	    gr.setColor(e.owner.playerColor.multiply(filterColor));
	    gr.fillRect(miniX + cellPosX - ENTITY_ADDITIONAL_SIZE, miniY + cellPosY - ENTITY_ADDITIONAL_SIZE, (int) e.sizeWidth / 24 + ENTITY_ADDITIONAL_SIZE, (int) e.sizeHeight / 24 + ENTITY_ADDITIONAL_SIZE);
	}

    }

    private Color getMapCellColor(int cellX, int cellY) {
	if (Main.getInstance().getPlayer().getShroud() != null && !Main.getInstance().getPlayer().getShroud().isExplored(cellX, cellY)) {
	    return null;
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
