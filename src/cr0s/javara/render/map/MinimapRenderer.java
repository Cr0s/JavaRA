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

    private Image minimapImage;

    public MinimapRenderer(World aWorld, int aWidth, int aHeight) {
	this.w = aWorld;

	this.width = aWidth;
	this.height = aHeight;

	try {
	    this.minimapImage = new Image(aWidth, aHeight);

	    this.minimapImage.getGraphics().setColor(Color.black);
	    this.minimapImage.getGraphics().fillRect(0, 0, width, height);
	} catch (SlickException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void renderMinimap(Point minimapPos, Graphics gr, Color filterColor) {
	int miniX = (int) minimapPos.getX();
	int miniY = (int) minimapPos.getY();

	this.minimapImage.draw(miniX, miniY, filterColor);
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

    public void update( Color filterColor) {
	Graphics gr;
	try {
	    gr = this.minimapImage.getGraphics();


	    final int ENTITY_ADDITIONAL_SIZE = 1; // grow entity rectangle point in pixels to see on mini map more clear

	    if (Main.getInstance().getPlayer().getShroud() != null) {
		gr.setColor(Color.black);
		gr.fillRect(0, 0, width, height);
	    }

	    // Render terrain
	    for (int x = 0; x < width; x++) {
		for (int y = 0; y < height; y++) {
		    int r, g, b;

		    Color targetColor = getMapCellColor((int) (w.getMap().getBounds().getMinX() / 24 + x), (int) (w.getMap().getBounds().getMinY() / 24 + y));

		    if (targetColor != null) {
			gr.setColor(targetColor);
			gr.fillRect(x, y, 1, 1);
		    }
		}
	    }

	    for (Entity e : w.getEntitiesList()) {
		int cellPosX = (int) (e.posX - w.getMap().getBounds().getMinX()) / 24;
		int cellPosY = (int) (e.posY - w.getMap().getBounds().getMinY()) / 24;

		gr.setColor(e.owner.playerColor.multiply(filterColor));
		gr.fillRect(cellPosX - ENTITY_ADDITIONAL_SIZE, cellPosY - ENTITY_ADDITIONAL_SIZE, (int) e.sizeWidth / 24 + ENTITY_ADDITIONAL_SIZE, (int) e.sizeHeight / 24 + ENTITY_ADDITIONAL_SIZE);
	    }
	} catch (SlickException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
    }
}
