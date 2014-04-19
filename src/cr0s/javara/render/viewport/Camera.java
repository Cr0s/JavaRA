package cr0s.javara.render.viewport;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import cr0s.javara.entity.Entity;
import cr0s.javara.render.map.TileMap;



public class Camera {

	private boolean init = false;
	public float offsetX, offsetY;
	public Rectangle viewportRect;
	private Rectangle worldClip;

	private Rectangle mapTilesBounds;
	
	public TileMap map;

	public Camera() {
	}

	public void init(GameContainer gc) throws SlickException {
		this.viewportRect = new Rectangle(0, 0, gc.getWidth(), gc.getHeight());
		mapTilesBounds = new Rectangle(0, 0, gc.getWidth(), gc.getHeight());
		worldClip = new Rectangle(0, 0, gc.getWidth(), gc.getHeight());
		init = true;
	}

	public void update(GameContainer gc, int delta) throws SlickException {
		setViewPort(-offsetX, -offsetY, gc.getWidth(), gc.getHeight());

		worldClip = new Rectangle(0, 0, gc.getWidth(), gc.getHeight());
	}

	public void render(GameContainer gc, Graphics g) throws SlickException {
		//int mapX = (int)this.mapTilesBounds.getX();
		//int mapY = (int)this.mapTilesBounds.getY();
		
		//float tileShiftX = 0;// this.viewportRect.getX() - mapX;
		//float tileShiftY = 0;//this.viewportRect.getY() - mapY;
		
		//g.translate(-(mapX + tileShiftX), -(mapY + tileShiftY));
		g.translate(offsetX, offsetY);
	}

	public void renderFinish(GameContainer gc, Graphics g) throws SlickException {
		//g.setWorldClip(worldClip);

		g.resetTransform();
		gc.getDefaultFont().drawString(5, 25, "Camera offset: " + offsetX + " x " + offsetY);
		gc.getDefaultFont().drawString(5, 45, "Map: " + (int)this.mapTilesBounds.getX() / 24 + " x " + (int)this.mapTilesBounds.getY() / 24);
	}

	public void setOffset(float x, float y) {
		float oldOffsetX = this.offsetX;
		float oldOffsetY = this.offsetY;
		
		this.offsetX = x;
		this.offsetY = y;
		
		this.mapTilesBounds.setBounds((int)this.viewportRect.getX(), (int)this.viewportRect.getY(), this.viewportRect.getWidth(), this.viewportRect.getHeight());
	}

	public float getOffsetX() {
		return offsetX;
	}
	public float getOffsetY() {
		return offsetY;

	}

	public void setViewPort(float x, float y, float width, float height) {
		viewportRect.setBounds(x, y, width, height);
	}

	public boolean isEntityInsideViewport(Entity e) {
		float posX = e.posX;
		float posY = e.posY;

		return viewportRect.intersects(e.boundingBox);
	}
	
	public Rectangle getMapTilesBounds() {
		return mapTilesBounds;
	}
}