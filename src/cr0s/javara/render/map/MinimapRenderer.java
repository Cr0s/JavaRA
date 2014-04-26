package cr0s.javara.render.map;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;

import cr0s.javara.entity.Entity;
import cr0s.javara.main.Main;
import cr0s.javara.render.World;

public class MinimapRenderer {
    
    private int width, height;
    private ImageBuffer imgbuf;
    private Image minimapImage;
    private World w;
    
    public MinimapRenderer(World aWorld, int aWidth, int aHeight) {
	this.w = aWorld;
	
	this.width = aWidth;
	this.height = aHeight;
	
	this.imgbuf = new ImageBuffer(width, height);
    }
    
    public void updateMinimap() {
	// Render terrain
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		int r, g, b;
		
		Color targetColor = getMapCellColor((int) (w.getMap().getBounds().getMinX() / 24 + x), (int) (w.getMap().getBounds().getMinY() / 24 + y));
		r = targetColor.getRed();
		g = targetColor.getGreen();
		b = targetColor.getBlue();
		
		
		imgbuf.setRGBA(x, y, r, g, b, targetColor.getAlpha());
	    }
	}
	
	for (Entity e : w.getEntitiesList()) {
	    int cellPosX = (int) (e.posX - w.getMap().getBounds().getMinX()) / 24;
	    int cellPosY = (int) (e.posY - w.getMap().getBounds().getMinY()) / 24;
	    
	    if (Main.getInstance().getPlayer().getShroud() != null && !Main.getInstance().getPlayer().getShroud().isExplored(cellPosX, cellPosY)) {
		//continue;
	    }
	    
	    fillRect(imgbuf, cellPosX, cellPosY, (int) e.sizeWidth / 24, (int) e.sizeHeight / 24, e.owner.playerColor);
	}
	
	this.minimapImage = imgbuf.getImage();
    }
    
    public void fillRect(ImageBuffer imgbuf, int x, int y, int width, int height, Color color) {
	int r, g, b;
	r = color.getRed();
	g = color.getGreen();
	b = color.getBlue();
	
	for (int ix = 0; ix < width; ix++) {
	    for (int iy = 0; iy < height; iy++) {
		imgbuf.setRGBA(x + ix, y + iy, r, g, b, color.getAlpha());
	    }
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
