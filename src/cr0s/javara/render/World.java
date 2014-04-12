package cr0s.javara.render;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Animation;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.AppletGameContainer.Container;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.Log;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.main.Main;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.render.map.tiled.TiledMap;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.TmpTexture;

public class World {
	private TileMap map;
	private Camera camera;

	private GameContainer container;

	private ArrayList<Entity> entities = new ArrayList<>();

	private final int PASSES_COUNT = 1;
	
	private int[][] blockingMap;
	
	boolean canRender = true;
	public World(String mapName, GameContainer c, Camera camera) {
		map = new TileMap(mapName);


		this.container = c;

		this.camera = camera;
		camera.map = map;
	}

	public void update(int delta) {
		// Update all entities
		for (Entity e : this.entities) {
			e.updateEntity(delta);
		}
	}

	public void render(Graphics g) {
		Rectangle mapBounds = camera.getMapTilesBounds();
		int mapX = (int)mapBounds.getX();
		int mapY = (int)mapBounds.getY();
	
		try {
			camera.render(container, g);
		} catch (SlickException e1) {
			e1.printStackTrace();
		}		
		
		map.render(container, g, camera);//, y, sx, sy, width, height, vpX, vpY, sw, sh);//
		
		// Make rendering passes
		for (int i = 0; i < PASSES_COUNT; i++) {
			for (Entity e : this.entities) {
				if (e.isVisible && e.shouldRenderedInPass(i) && camera.isEntityInsideViewport(e)) { 
					e.renderEntity(g);
				}
			}
		}	
		
		renderSelectionBoxes(g);
		renderHpBars(g);
	}

	private void renderSelectionBoxes(Graphics g) {
	    for (Entity e : this.entities) {
		if (e.isVisible && (e instanceof ISelectable) && (e.isSelected) && camera.isEntityInsideViewport(e)) { 
		    e.drawSelectionBox(g);
		}
	    }		    
	}
	
	private void renderHpBars(Graphics g) {
	    for (Entity e : this.entities) {
		if (e.isVisible && (e instanceof ISelectable) && !(e.isSelected) && (e.isMouseOver) && camera.isEntityInsideViewport(e)) { 
		    e.drawHpBar(g);
		}
	    }		    
	}
	
	public TileMap getMap() {
		return map;
	}

	public void spawnEntityInWorld(Entity e) {
		e.setWorld(this);
		this.entities.add(e);
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	/**
	 * 
	 * @param boundingBox
	 * @return
	 */
	public LinkedList<Entity> selectEntitiesInsideBox(Rectangle boundingBox) {
	    LinkedList<Entity> selectedEntities = new LinkedList<>();
	    
	    for (Entity e : this.entities) {
		if (e instanceof ISelectable) {
		    if (boundingBox.intersects(e.boundingBox)) { 
			((ISelectable)e).select();
			
			selectedEntities.add(e);
		    } else
		    {
			((ISelectable)e).cancelSelect();
		    }
		}
	    }
	    
	    return selectedEntities;
	}
	
	public void cancelAllSelection() {
	    for (Entity e : this.entities) {
		if (e instanceof ISelectable) {
		    ((ISelectable) e).cancelSelect();
		}
	    }	    
	}

	public Entity getEntityInPoint(float x, float y) {
	    // First check non-buildings entities
	    for (Entity e : this.entities) {
		if (e instanceof ISelectable && !(e instanceof EntityBuilding)) {
		    if (e.boundingBox.contains(x, y)) {
			return e;
		    }
		}
	    }
	    
	    // Check buildings
	    for (Entity e : this.entities) {
		if (e instanceof ISelectable && (e instanceof EntityBuilding)) {
		    if (e.boundingBox.contains(x, y)) {
			return e;
		    }
		}
	    }
	    
	    return null;
	}
}
