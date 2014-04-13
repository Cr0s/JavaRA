package cr0s.javara.render;

import java.util.ArrayList;
import java.util.Iterator;
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
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityBuildingProgress;
import cr0s.javara.main.Main;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.TmpTexture;

public class World {
    private TileMap map;
    private Camera camera;

    private GameContainer container;

    private ArrayList<Entity> entities = new ArrayList<>();
    private LinkedList<Entity> entitiesToAdd = new LinkedList<>();

    private final int PASSES_COUNT = 2;

    private int[][] blockingMap;

    boolean canRender = true;
    
    private int removeDeadTicks = 0;
    private final int REMOVE_DEAD_INTERVAL_TICKS = 1000;
    
    public World(String mapName, GameContainer c, Camera camera) {
	map = new TileMap(mapName);


	this.container = c;

	this.camera = camera;
	camera.map = map;
    }

    public void update(int delta) {
	if (removeDeadTicks++ > REMOVE_DEAD_INTERVAL_TICKS) {
	    ArrayList<Entity> list = new ArrayList<Entity>();
	    for (Entity e : this.entities) {
		if (!e.isDead()) {
		    list.add(e);
		}
	    }
	    this.entities = list;
	    
	    this.removeDeadTicks = 0;
	}


	for (Entity e : entitiesToAdd) {
	    this.entities.add(e);
	}
	entitiesToAdd.clear();          

	// Update all entities
	for (Entity e : this.entities) {
	    if (!e.isDead()) { 
		e.updateEntity(delta);
	    }
	}  	
    }

    /**
     * Renders all world's graphics.
     * @param g graphic output object
     */
    public void render(Graphics g) {
	Rectangle mapBounds = camera.getMapTilesBounds();
	int mapX = (int) mapBounds.getX();
	int mapY = (int) mapBounds.getY();

	try {
	    camera.render(container, g);
	} catch (SlickException e1) {
	    e1.printStackTrace();
	}		

	map.render(container, g, camera);

	// Make rendering passes
	for (int i = 0; i < PASSES_COUNT; i++) {
	    for (Entity e : this.entities) {
		if (!e.isDead() && e.isVisible && e.shouldRenderedInPass(i) && camera.isEntityInsideViewport(e)) { 
		    if (e instanceof EntityBuilding) {
			renderEntityBib((EntityBuilding) e);
		    }
		    
		    e.renderEntity(g);
		}
	    }
	}	

	renderSelectionBoxes(g);
	renderHpBars(g);
    }

    /**
     * Renders building foundation (bib).
     * @param b building
     */
    private void renderEntityBib(final EntityBuilding b) {
	BibType bt = b.getBibType();
	SpriteSheet bibSheet = ResourceManager.getInstance().getBibSheet(bt);
	
	bibSheet.startUse();
	int x = (int) b.posX, y = (int) b.posY;
	int bibCount = 0;
	
	switch (bt) {
	case SMALL:
	    bibCount = 2;
	    break;
	    
	case MIDDLE:
	    bibCount = 3;
	    break;
	    
	case BIG:
	    bibCount = 4;
	    break;
	default:
	    bibSheet.endUse();
	    return;
	}
	
	for (int bibY = 0; bibY < 2; bibY++) {
	    for (int bibX = 0; bibX < bibCount; bibX++) {
		int index = bibCount * bibY + bibX;	
		
		bibSheet.getSubImage(0, index).drawEmbedded(x + 24 * bibX,  y + (b.getHeight() / 2 + 12) + 24 * bibY, 24, 24);
	    }
	}
	    
	bibSheet.endUse();
    }
    
    private void renderSelectionBoxes(Graphics g) {
	for (Entity e : this.entities) {
	    if (!e.isDead() && e.isVisible && (e instanceof ISelectable) && (e.isSelected) && camera.isEntityInsideViewport(e)) { 
		e.drawSelectionBox(g);
	    }
	}		    
    }

    private void renderHpBars(Graphics g) {
	for (Entity e : this.entities) {
	    if (!e.isDead() && e.isVisible && (e instanceof ISelectable) && !(e.isSelected) && (e.isMouseOver) && camera.isEntityInsideViewport(e)) { 
		e.drawHpBar(g);
	    }
	}		    
    }

    public TileMap getMap() {
	return map;
    }

    public void spawnEntityInWorld(Entity e) {
	e.setWorld(this);
	this.entitiesToAdd.add(e);
    }

    public Camera getCamera() {
	return camera;
    }

    /**
     * 
     * @param boundingBox
     * @return list of entities selected
     */
    public LinkedList<Entity> selectEntitiesInsideBox(Rectangle boundingBox) {
	LinkedList<Entity> selectedEntities = new LinkedList<>();

	for (Entity e : this.entities) {
	    if (!e.isDead() && (e instanceof ISelectable)) {
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
	    if (!e.isDead() && e instanceof ISelectable && !(e instanceof EntityBuilding)) {
		if (e.boundingBox.contains(x, y)) {
		    return e;
		}
	    }
	}

	// Check buildings
	for (Entity e : this.entities) {
	    if (!e.isDead() && e instanceof ISelectable && (e instanceof EntityBuilding)) {
		if (e.boundingBox.contains(x, y)) {
		    return e;
		}
	    }
	}

	return null;
    }
    
    public void addBuildingTo(EntityBuilding b) {
	EntityBuildingProgress ebp = new EntityBuildingProgress(b);
	
	ebp.isVisible = true;
	
	this.spawnEntityInWorld(ebp);
    }
}
