package cr0s.javara.render;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import cr0s.javara.combat.TargetType;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityBuildingProgress;
import cr0s.javara.entity.effect.Explosion;
import cr0s.javara.entity.effect.SequencePlayer;
import cr0s.javara.entity.effect.Smoke;
import cr0s.javara.entity.turreted.IHaveTurret;
import cr0s.javara.gameplay.Player;
import cr0s.javara.main.Main;
import cr0s.javara.order.ITargetLines;
import cr0s.javara.order.TargetLine;
import cr0s.javara.perfomance.Profiler;
import cr0s.javara.render.EntityBlockingMap.Influence;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.render.map.InfantryPathfinder;
import cr0s.javara.render.map.SmudgeLayer;
import cr0s.javara.render.map.TileMap;
import cr0s.javara.render.map.TileSet;
import cr0s.javara.render.map.VehiclePathfinder;
import cr0s.javara.render.viewport.Camera;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.CellChooser;
import cr0s.javara.util.Pos;


public class World implements TileBasedMap {
    private TileMap map;
    private Camera camera;

    private GameContainer container;

    private VehiclePathfinder vp;
    private InfantryPathfinder ip;

    private ArrayList<Player> players = new ArrayList<>();

    private LinkedList<Entity> entities = new LinkedList<>();
    private LinkedList<Entity> entitiesToAdd = new LinkedList<>();

    private final int PASSES_COUNT = 3;

    public int blockingMap[][];
    public EntityBlockingMap blockingEntityMap;

    boolean canRender = true;

    private int removeDeadTicks = 0;
    private final int REMOVE_DEAD_INTERVAL_TICKS = 20;

    private Random random;

    public final int MAX_RANGE = 50;
    private ArrayList<Pos> pointsInRange[] = new ArrayList[MAX_RANGE + 1];

    public World(String mapName, GameContainer c, Camera camera) {
	map = new TileMap(this, mapName);

	map.smudges = new SmudgeLayer(map);
	this.blockingMap = new int[map.getWidth()][map.getHeight()];
	this.blockingEntityMap = new EntityBlockingMap(this);

	map.fillBlockingMap(this.blockingMap);

	this.vp = new VehiclePathfinder(this);
	this.ip = new InfantryPathfinder(this);

	this.container = c;

	this.camera = camera;
	camera.map = map;

	this.random = new Random(System.currentTimeMillis());
	generateRangePoints();
    }

    private void generateRangePoints() {
	for (int i = 0; i < MAX_RANGE + 1; i++) {
	    pointsInRange[i] = new ArrayList<>();
	}

	for (int j = -MAX_RANGE; j <= MAX_RANGE; j++) {
	    for (int i = -MAX_RANGE; i <= MAX_RANGE; i++) {
		if (MAX_RANGE * MAX_RANGE >= i * i + j * j) {
		    pointsInRange[(int)Math.ceil(Math.sqrt(i * i + j * j))].add(new Pos(i, j));
		}
	    }
	}
    }

    public void update(int delta) {
	Profiler.getInstance().startForSection("World tick");
	
	Profiler.getInstance().startForSection("World: shroud tick");
	if (Main.getInstance().getPlayer().getShroud() != null) {
	    Main.getInstance().getPlayer().getShroud().getRenderer().update(Main.getInstance().getPlayer().getShroud());
	} else {
	    Main.getInstance().getObserverShroudRenderer().update(null);
	}
	Profiler.getInstance().stopForSection("World: shroud tick");

	if (removeDeadTicks++ > REMOVE_DEAD_INTERVAL_TICKS) {
	    LinkedList<Entity> list = new LinkedList<Entity>();
	    for (Entity e : this.entities) {
		if (!e.isDead()) {
		    list.add(e);
		}
	    }
	    this.entities = list;

	    this.removeDeadTicks = 0;
	}


	for (Entity e : this.entitiesToAdd) {
	    this.entities.add(e);
	}
	
	this.entitiesToAdd.clear();          

	this.blockingEntityMap.update();
	
	// Update all entities
	Profiler.getInstance().startForSection("World: entity tick");
	for (Entity e : this.entities) {
	    if (!e.isDead()) { 
		// Set up blocking map parameters
		if (e instanceof MobileEntity) {
		    this.blockingEntityMap.occupyForMobileEntity((MobileEntity) e);
		} else if (e instanceof EntityBuilding) {
		    this.blockingEntityMap.occupyForBuilding((EntityBuilding) e);
		}
	    }
	}

	for (Entity e : this.entities) {
	    if (!e.isDead()) { 
		// Reveal shroud
		if (e instanceof IShroudRevealer) {
		    if (e.owner.getShroud() != null) {
			e.owner.getShroud().exploreRange((int) e.boundingBox.getCenterX() / 24, (int) e.boundingBox.getCenterY() / 24, ((IShroudRevealer) e).getRevealingRange());
		    }
		}

		if (e instanceof IHaveTurret) {
		    ((IHaveTurret) e).updateTurrets(delta);
		}
		
		// For mobile entities, after entity updated, update it's blocking map state to avoid entity movement collisions
		if (e instanceof MobileEntity) {
		    this.blockingEntityMap.freeForMobileEntity((MobileEntity) e);

		    e.updateEntity(delta);	

		    // Lock next entity position. Or re-lock current, if position is not changed
		    this.blockingEntityMap.occupyForMobileEntity((MobileEntity) e);
		} else {
		    e.updateEntity(delta);		    
		}

		if (e instanceof ITargetLines && e.isSelected) {
		    for (TargetLine tl : ((ITargetLines) e).getTargetLines()) {
			tl.update(delta);
		    }
		}
	    }
	}  	
	Profiler.getInstance().stopForSection("World: entity tick");
	
	updatePlayers();
	
	Profiler.getInstance().stopForSection("World tick");
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

	//Profiler.getInstance().startForSection("r: Map");
	map.render(container, g, camera);
	//Profiler.getInstance().stopForSection("r: Map");
	
	Color pColor = g.getColor();

	//Profiler.getInstance().startForSection("r: Entity");
	// Render bibs
	for (Entity e : this.entities) {		    
	    if (!e.isDead() && e.isVisible && camera.isEntityInsideViewport(e)) { 
		if (e instanceof EntityBuilding) {
		    renderEntityBib((EntityBuilding) e);
		}
	    }
	}

	// Make rendering passes
	for (int i = -2; i < PASSES_COUNT; i++) {
	    for (Entity e : this.entities) {		    
		if (!e.isDead() && e.isVisible && e.shouldRenderedInPass(i) && camera.isEntityInsideViewport(e)) { 
		    e.renderEntity(g);

		    if (e instanceof ITargetLines && e.isSelected) {
			for (TargetLine tl : ((ITargetLines) e).getTargetLines()) {
			    tl.render(g);
			}
		    }
		}
	    }
	}	

	map.renderMapEntities(container, g, camera);

	// Debug: render blocked cells
	if (Main.DEBUG_MODE) {
	    Color blockedColor = new Color(64, 0, 0, 64);
		
	    for (int y = (int) (-Main.getInstance().getCamera().getOffsetY()) / 24; y < map.getHeight(); y++) {
		for (int x = (int) (-Main.getInstance().getCamera().getOffsetX()) / 24; x < map.getWidth(); x++) {
		    if (!this.isCellPassable(new Pos(x, y))) {
			g.setColor(blockedColor);
			g.fillRect(x * 24, y * 24, 24, 24);
			g.setColor(pColor);
		    }		
		}
	    }
	}	

	renderSelectionBoxes(g);
	
	//Profiler.getInstance().stopForSection("r: Entity");

	//Profiler.getInstance().startForSection("r: Shroud");
	if (Main.getInstance().getPlayer().getShroud() != null) {
	    Main.getInstance().getPlayer().getShroud().getRenderer().renderShrouds(g);
	} else {
	    Main.getInstance().getObserverShroudRenderer().renderShrouds(g);
	}
	//Profiler.getInstance().stopForSection("r: Shroud");
	
	Main.getInstance().getBuildingOverlay().render(g);
    }

    /**
     * Renders building foundation (bib).
     * @param b building
     */
    private void renderEntityBib(final EntityBuilding b) {
	BibType bt = b.getBibType();

	SpriteSheet bibSheet = ResourceManager.getInstance().getBibSheet(bt);
	if (bt == BibType.NONE || bibSheet == null) {
	    return;
	}

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

	if (bibCount > 1) {
	    for (int bibY = 0; bibY < 2; bibY++) {
		for (int bibX = 0; bibX < bibCount; bibX++) {
		    int index = bibCount * bibY + bibX;	

		    bibSheet.getSubImage(0, index).drawEmbedded(x + 24 * bibX,  y + 24 * (b.getHeightInTiles() / 2) + 24 * bibY - 12, 24, 24);
		}
	    }
	}

	bibSheet.endUse();
    }

    private void renderSelectionBoxes(Graphics g) {
	for (Entity e : this.entities) {
	    if (!e.isDead() && e.isVisible && camera.isEntityInsideViewport(e)) {
		if ((e instanceof ISelectable) && (e.isSelected)) { 
		    e.drawSelectionBox(g);
		} else if (e.isMouseOver) {
		    e.drawHpBar(g);
		}
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
	    if (!e.isDead() && (e instanceof ISelectable) && (e instanceof MobileEntity)) {
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
	Main.getInstance().getPlayer().selectedEntities.clear();

	for (Entity e : this.entities) {
	    if (e instanceof ISelectable) {
		((ISelectable) e).cancelSelect();
	    }
	}	    
    }

    /**
     * Returns entity in point. This can building entity or vehicle, infantry.
     * @param x point X
     * @param y point Y
     * @return Entity, if found or null, if not
     */
    public Entity getEntityInPoint(float x, float y) {
	return getEntityInPoint(x, y, false);
    }


    /**
     * Returns only non-building entity, located in point.
     * @param x point X
     * @param y point Y
     * @return Entity or null
     */
    public Entity getEntityNonBuildingInPoint(float x, float y) {
	return getEntityInPoint(x, y, true);
    }

    private Entity getEntityInPoint(float x, float y, boolean onlyNonBuildings) {
	// First check non-buildings entities
	for (Entity e : this.entities) {
	    if (!e.isDead() && e instanceof ISelectable && !(e instanceof EntityBuilding)) {
		if (e.boundingBox.contains(x, y)) {
		    return e;
		}
	    }
	}

	// Check buildings
	if (!onlyNonBuildings) {
	    for (Entity e : this.entities) {
		if (!e.isDead() && e instanceof ISelectable && (e instanceof EntityBuilding)) {
		    if (e.boundingBox.contains(x, y)) {
			return e;
		    }
		}
	    }
	}

	return null;
    }

    private void updatePlayers() {
	for (Player p : this.players) {
	    p.update(0);
	}
    }

    public void addBuildingTo(EntityBuilding b) {
	EntityBuildingProgress ebp = new EntityBuildingProgress(b);

	ebp.isVisible = true;

	this.spawnEntityInWorld(ebp);
    }

    public boolean isCellPassable(int x, int y) {
	return isCellPassable(x, y, SubCell.FULL_CELL);
    }
    
    public boolean isCellPassable(int x, int y, SubCell sub) {
	if (x >= this.map.getWidth() || y >= this.map.getHeight()) {
	    return false;
	}

	if (x < 0 || y < 0) {
	    return false;
	}

	if (!this.map.isInMap(x * 24, y * 24)) {
	    return false;
	}

	return (this.blockingEntityMap.isSubcellFree(new Pos(x, y), sub)) && (blockingMap[x][y] == 0 
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_CLEAR_ID
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_BUILDING_CLEAR_ID
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_BEACH_ID
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_ROAD_ID
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_ROUGH_ID);
    }

    public boolean isCellBuildable(int x, int y) {
	return isCellBuildable(x, y, false);
    }

    public boolean isCellBuildable(int x, int y, boolean isMcvDeploy) {
	if (x >= this.map.getWidth() || y >= this.map.getHeight()) {
	    return false;
	}

	if (x < 0 || y < 0) {
	    return false;
	}

	if (!this.map.isInMap(x * 24, y * 24)) {
	    return false;
	}

	return (isMcvDeploy || !this.blockingEntityMap.isAnyInfluenceInCell(new Pos(x, y))) && (this.map.getResourcesLayer().isCellEmpty(x, y)) && (blockingMap[x][y] == 0 
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_CLEAR_ID
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_BEACH_ID
		|| this.blockingMap[x][y] == this.map.getTileSet().SURFACE_ROAD_ID);	
    }

    @Override
    public boolean blocked(PathFindingContext arg0, int x, int y) {
	return !((MobileEntity) arg0.getMover()).canEnterCell(new Pos(x, y));
    }

    // TODO: add lower costs to roads and higher to beaches, roughs
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
    
    public InfantryPathfinder getInfantryPathfinder() {
	return this.ip;
    }

    public void occupyRandomSpawnForPlayer(Player p) {
	ArrayList<Pos> spawns = this.map.getSpawnPoints();
	Random r = new Random(System.currentTimeMillis());

	if (spawns.size() > 0) {
	    int randomSpawnIndex = r.nextInt(spawns.size());
	    Pos spawnPoint = spawns.get(randomSpawnIndex);

	    spawns.remove(randomSpawnIndex);

	    p.setSpawn((int) spawnPoint.getX(), (int) spawnPoint.getY());
	}
    }

    public void addPlayer(Player p) {
	this.players.add(p);

	occupyRandomSpawnForPlayer(p);
	p.spawn();
    }

    public boolean isPossibleToBuildHere(int x, int y, EntityBuilding eb) {
	int[][] blockingCells = eb.getBlockingCells();

	for (int bX = 0; bX < eb.getWidthInTiles(); bX++) {
	    for (int bY = 0; bY < eb.getHeight(); bY++) {
		if (blockingCells[bX][bY] != TileSet.SURFACE_BUILDING_CLEAR_ID && !isCellBuildable(x + bX, y + bY)) {
		    return false;
		}
	    }
	}

	return true;
    }

    public LinkedList<Entity> getEntitiesList() {
	return this.entities;
    }

    public EntityBuilding getBuildingInCell(Pos cellPos) {
	LinkedList<Influence> cellInf = this.blockingEntityMap.getCellInfluences(cellPos);

	if (cellInf == null) {
	    return null;
	}
	
	for (Influence i : cellInf) {
	    if (i.entity instanceof EntityBuilding) {
		return ((EntityBuilding) i.entity);
	    }
	}

	return null;
    }

    public MobileEntity getMobileEntityInCell(Pos cellPos) {
	LinkedList<Influence> cellInf = this.blockingEntityMap.getCellInfluences(cellPos);
	
	if (cellInf == null) {
	    return null;
	}
	
	for (Influence i : cellInf) {
	    if (i.entity instanceof MobileEntity) {
		return ((MobileEntity) i.entity);
	    }
	}

	return null;
    }

    public boolean isCellBlockedByEntity(Pos cellPos) {
	int x = (int) cellPos.getX();
	int y = (int) cellPos.getY();

	return !this.blockingEntityMap.isSubcellFree(new Pos(x, y), SubCell.FULL_CELL);
    }
    
    public boolean isCellBlockedByEntity(Pos cellPos, SubCell sub) {
	int x = (int) cellPos.getX();
	int y = (int) cellPos.getY();

	return !this.blockingEntityMap.isSubcellFree(new Pos(x, y), sub);
    }    

    public int getRandomInt(int from, int to) {
	return from + random.nextInt(to - from);
    }

    public ArrayList<Pos> chooseTilesInAnnulus(Pos centerPos, int minRange, int maxRange, CellChooser chooser) {
	ArrayList<Pos> res = new ArrayList<Pos>();

	if (minRange > maxRange) {
	    throw new IllegalArgumentException("minRange > maxRange");
	}
	
	if (maxRange > this.pointsInRange.length) {
	    throw new IllegalArgumentException("maxRange exceeds possible value: " + this.pointsInRange.length);
	}
	
	for (int i = minRange; i <= maxRange; i++) {
	    for (Pos p : this.pointsInRange[i]) {
		int cellPosX = (int) (centerPos.getX() + p.getX());
		int cellPosY = (int) (centerPos.getY() + p.getY());

		Pos cellPoint = new Pos(cellPosX, cellPosY);

		if (map.isInMap(cellPosX * 24, cellPosY * 24) && chooser.isCellChoosable(cellPoint)) {
		    res.add(cellPoint);
		}
	    }
	}

	return res;
    }    
    
    public ArrayList<Pos> chooseTilesInCircle(Pos centerPos, int range, CellChooser chooser) {
	ArrayList<Pos> res = new ArrayList<Pos>();

	for (int i = 0; i <= range; i++) {
	    for (Pos p : this.pointsInRange[i]) {
		int cellPosX = (int) (centerPos.getX() + p.getX());
		int cellPosY = (int) (centerPos.getY() + p.getY());

		Pos cellPoint = new Pos(cellPosX, cellPosY);

		if (map.isInMap(cellPosX * 24, cellPosY * 24) && chooser.isCellChoosable(cellPoint)) {
		    res.add(cellPoint);
		}
	    }
	}

	return res;
    }

    public ArrayList<Pos> choosePassableCellsInCircle(Pos centerPos, int range) {
	return this.chooseTilesInCircle(centerPos, range, new CellChooser() {

	    @Override
	    public boolean isCellChoosable(Pos cellPos) {
		return isCellPassable((int) cellPos.getX(), (int) cellPos.getY());
	    }

	});
    }

    public boolean isCellPassable(Pos cellPos) {
	return isCellPassable((int) cellPos.getX(), (int) cellPos.getY());
    }

    public boolean isCellPassable(Pos cellPos, SubCell subCell) {
	return isCellPassable((int) cellPos.getX(), (int) cellPos.getY(), subCell);
    }

    public TargetType getCellTargetType(Pos cellPos) {
	int x = (int) cellPos.getX();
	int y = (int) cellPos.getY();
	
	if (!this.map.isInMap(x, y)) {
	    return null;
	}
	
	if (this.blockingMap[x][y] == TileSet.SURFACE_BEACH_ID 
		|| this.blockingMap[x][y] == TileSet.SURFACE_BUILDING 
		|| this.blockingMap[x][y] == TileSet.SURFACE_BUILDING_CLEAR_ID 
		|| this.blockingMap[x][y] == TileSet.SURFACE_CLEAR_ID
		|| this.blockingMap[x][y] == TileSet.SURFACE_ORE_GEM
		|| this.blockingMap[x][y] == TileSet.SURFACE_ORE_GOLD
		|| this.blockingMap[x][y] == TileSet.SURFACE_ROAD_ID
		|| this.blockingMap[x][y] == TileSet.SURFACE_ROCK_ID
		|| this.blockingMap[x][y] == TileSet.SURFACE_ROUGH_ID) {
	    return TargetType.GROUND;
	} else {
	    if (this.blockingMap[x][y] == TileSet.SURFACE_RIVER_ID
		    || this.blockingMap[x][y] == TileSet.SURFACE_WATER_ID) {
		return TargetType.WATER;
	    }
	}
	
	return TargetType.AIR;
    }

    public Random getRandom() {
	return this.random;
    }

    public void spawnExplosionAt(Pos pos, String explosionType) {
	Explosion e = new Explosion(pos, explosionType);
	e.setWorld(this);
	e.isVisible = true;
	
	this.spawnEntityInWorld(e);
    }

    public void spawnSmokeAt(Pos pos, String smokeSprite) {
	Smoke s = new Smoke(pos, smokeSprite);
	s.setWorld(this);
	s.isVisible = true;
	
	this.spawnEntityInWorld(s);	
    }

    public ArrayList<Pos> chooseTilesInCircle(Pos targetTile, int i) {
	return this.chooseTilesInCircle(targetTile, i, new CellChooser() {

	    @Override
	    public boolean isCellChoosable(Pos cellPos) {
		return true;
	    }    
	});
    }

    public ArrayList<EntityActor> getActorsInCircle(final Pos pos, float range) {
	ArrayList<EntityActor> result = new ArrayList<EntityActor>();
	ArrayList<Pos> tiles = this.chooseTilesInCircle(pos.getCellPos(), (int) Math.floor(range / 24.0f));
	
	for (Pos tile : tiles) {
	    LinkedList<Influence> influences = this.blockingEntityMap.getCellInfluences(tile);
	    
	    if (influences == null) {
		continue;
	    }
	    
	    for (Influence cellInf : influences) {
		if (cellInf.entity != null && (cellInf.entity instanceof EntityActor)) {
		    // XXX: additional check for duplicates increases algorithm complexity
		    // TODO: look for another ways to avoid duplicates for actors, that occupies more than one cell 
		    if (!result.contains((EntityActor) cellInf.entity)) {
			result.add((EntityActor) cellInf.entity);
		    }
		}
	    }
	}
	
	return result;
    }

    public float getRandomFloat(float from, float to) {
	return from + (random.nextFloat() % (to - from));
    }
    
    public Pos chooseClosestPassableCellInRangeAroundOtherCell(Pos myCellPos, Pos destCell, float destRange) {
	// Find free cells in range, starting from closest range
	ArrayList<Pos> cells = new ArrayList<Pos>();
	for (int range = 1; range <= destRange; range++) {
	    cells.addAll(choosePassableCellsInCircle(destCell, range));
	}

	// Select closest to us and within range
	Pos closest = (cells.size() != 0) ? cells.get(0) : null;
	float minDistance = 0;
	for (Pos pos : cells) {
	    float distance = (float) pos.distanceTo(myCellPos);
	    if (pos.distanceTo(destCell) >= destRange) { // not within range, ignore
		continue;
	    }
	    
	    if (minDistance == 0 || distance < minDistance) {
		closest = pos;
		minDistance = distance;
	    }
	}

	return closest;	
    }

    public void playSequenceAt(Sequence deathSequence, Pos position) {
	SequencePlayer sp = new SequencePlayer(position, deathSequence);
	sp.isVisible = true;
	
	this.spawnEntityInWorld(sp);
    }

    public ArrayList<Pos> chooseTilesInAnnulus(Pos center, int minRange,
	    int maxRange) {
	// Choose any tiles in annulus
	return this.chooseTilesInAnnulus(center, minRange, maxRange, new CellChooser() {

	    @Override
	    public boolean isCellChoosable(Pos cellPos) {
		return true;
	    }    
	});
    }

    public ArrayList<Player> getPlayers() {
	return this.players;
    }
}
