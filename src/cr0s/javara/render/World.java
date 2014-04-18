package cr0s.javara.render;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityBuildingProgress;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.main.Main;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.render.map.VehiclePathfinder;
import cr0s.javara.render.shrouds.Shroud;
import cr0s.javara.render.shrouds.ShroudRenderer;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.TmpTexture;

public class World implements TileBasedMap {
    private TileMap map;
    private Camera camera;

    private GameContainer container;
    
    private VehiclePathfinder vp;

    private ArrayList<Player> players = new ArrayList<>();
    
    private ArrayList<Entity> entities = new ArrayList<>();
    private LinkedList<Entity> entitiesToAdd = new LinkedList<>();

    private final int PASSES_COUNT = 2;

    public int[][] blockingMap;
    public int[][] blockingEntityMap;
    
    boolean canRender = true;
    
    private int removeDeadTicks = 0;
    private final int REMOVE_DEAD_INTERVAL_TICKS = 1000;
    
    public World(String mapName, GameContainer c, Camera camera) {
	map = new TileMap(this, mapName);

	this.blockingMap = new int[map.getWidth()][map.getHeight()];
	this.blockingEntityMap = new int[map.getWidth()][map.getHeight()];
	
	map.fillBlockingMap(this.blockingMap);
	
	this.vp = new VehiclePathfinder(this);
	
	this.container = c;

	this.camera = camera;
	camera.map = map;

    }

    public void update(int delta) {
	if (Main.getInstance().getPlayer().getShroud() != null) {
	    Main.getInstance().getPlayer().getShroud().getRenderer().update(Main.getInstance().getPlayer().getShroud());
	} else {
	    Main.getInstance().getObserverShroudRenderer().update(null);
	}
	
	for (int i = 0; i < this.map.getHeight(); i++) {
	    Arrays.fill(this.blockingEntityMap[i], 0);
	}
	
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
	    if (e instanceof EntityBuilding) {
		EntityBuilding eb = (EntityBuilding) e;
		
		for (int by = 0; by < eb.getHeightInTiles(); by++) {
		    for (int bx = 0; bx < eb.getWidthInTiles(); bx++) {
			this.blockingMap[(((int) eb.posX + 12) / 24) + bx][(((int) eb.posY + 12) / 24) + by] = eb.getBlockingCells()[bx][by];
		    }
		}
	    }
	    
	    this.entities.add(e);
	}
	entitiesToAdd.clear();          
	
	// Update all entities
	for (Entity e : this.entities) {
	    if (!e.isDead()) { 
		e.updateEntity(delta);
		
		// Set up blocking map parameters
		if (e instanceof EntityVehicle) {
		    this.blockingEntityMap[(int) (((EntityVehicle) e).getCenterPosX()) / 24][(int) (((EntityVehicle) e).getCenterPosY()) / 24] = 1;
		}
		
		// Reveal shroud
		if (e instanceof IShroudRevealer) {
		    if (e.owner.getShroud() != null) {
			e.owner.getShroud().exploreRange((int) e.boundingBox.getCenterX()/ 24, (int) e.boundingBox.getCenterY() / 24, ((IShroudRevealer)e).getRevealingRange());
		    }
		}
	    }
	}  	
	
	updatePlayersBases();
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

	Color blockedColor = new Color(64, 0, 0, 64);
	Color pColor = g.getColor();
	
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

	map.renderMapEntities(container, g, camera);
	
	// Debug: render blocked cells
	if (Main.DEBUG_MODE) {
	    for (int y = 0; y < map.getHeight(); y++) {
		for (int x = 0; x < map.getWidth(); x++) {
		    if (!isCellPassable(x, y)) {
			g.setColor(blockedColor);
			g.fillRect(x * 24, y * 24, 24, 24);
			g.setColor(pColor);
		    }		
		}
	    }
	}	
	
	renderSelectionBoxes(g);
	renderHpBars(g);
	
	if (Main.getInstance().getPlayer().getShroud() != null) {
	    Main.getInstance().getPlayer().getShroud().getRenderer().renderShrouds(g);
	} else {
	    Main.getInstance().getObserverShroudRenderer().renderShrouds(g);
	}
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
    public LinkedList<Entity> selectMovableEntitiesInsideBox(Rectangle boundingBox) {
	LinkedList<Entity> selectedEntities = new LinkedList<>();

	for (Entity e : this.entities) {
	    if (!e.isDead() && (e instanceof ISelectable) && (e instanceof IMovable)) {
		if (boundingBox.intersects(e.boundingBox)) { 
		    ((ISelectable) e).select();

		    selectedEntities.add(e);
		} else
		{
		    ((ISelectable) e).cancelSelect();
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
    
    private void updatePlayersBases() {
	for (Player p : this.players) {
	    p.getBase().update();
	}
    }
    
    public void addBuildingTo(EntityBuilding b) {
	EntityBuildingProgress ebp = new EntityBuildingProgress(b);
	
	ebp.isVisible = true;
	
	this.spawnEntityInWorld(ebp);
    }
    
    public boolean isCellPassable(int x, int y) {
	if (x >= this.map.getWidth() || y >= this.map.getHeight()) {
	    return false;
	}
	
	if (x < 0 || y < 0) {
	    return false;
	}
	
	if (!this.map.isInMap(x * 24, y * 24)) {
	    return false;
	}
	
	return (blockingEntityMap[x][y] == 0) && (blockingMap[x][y] == 0 
		    || this.blockingMap[x][y] == this.map.getTileSet().SURFACE_CLEAR_ID
		    || this.blockingMap[x][y] == this.map.getTileSet().SURFACE_BEACH_ID
		    || this.blockingMap[x][y] == this.map.getTileSet().SURFACE_ROAD_ID
		    || this.blockingMap[x][y] == this.map.getTileSet().SURFACE_ROUGH_ID);
    }
    
    public boolean isCellBuildable(int x, int y) {
	if (x >= this.map.getWidth() || y >= this.map.getHeight()) {
	    return false;
	}
	
	if (x < 0 || y < 0) {
	    return false;
	}
	
	if (!this.map.isInMap(x * 24, y * 24)) {
	    return false;
	}
	
	return (blockingEntityMap[x][y] == 0) && (blockingMap[x][y] == 0 
		    || this.blockingMap[x][y] == this.map.getTileSet().SURFACE_CLEAR_ID
		    || this.blockingMap[x][y] == this.map.getTileSet().SURFACE_BEACH_ID
		    || this.blockingMap[x][y] == this.map.getTileSet().SURFACE_ROAD_ID);	
    }

    @Override
    public boolean blocked(PathFindingContext arg0, int x, int y) {
	return !this.isCellPassable(x, y);
    }

    @Override
    public float getCost(PathFindingContext ctx, int x, int y) {
	return 1;
    }

    @Override
    public int getHeightInTiles() {
	return this.map.getHeight();
    }

    @Override
    public int getWidthInTiles() {
	return this.map.getWidth();
    }

    @Override
    public void pathFinderVisited(int arg0, int arg1) {
    }
    
    public VehiclePathfinder getVehiclePathfinder() {
	return this.vp;
    }
    
    public void addPlayer(Player p) {
	this.players.add(p);
    }
}
