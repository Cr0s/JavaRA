package cr0s.javara.entity.building.common;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.Pos;

public class EntityWarFactory extends EntityBuilding implements ISelectable, IShroudRevealer, IDeployable, IPowerConsumer, IHaveCost {

    private SpriteSheet sheetTop;
    private Image weapDownNormal, weapDownCorrupted;
    
    private final String TEXTURE_NAME_DOWN = "weap.shp";
    private final String TEXTURE_NAME_TOP = "weap2.shp";
    private final String MAKE_TEXTURE_NAME = "weapmake.shp";
    private final int CORRUPTION_INDEX = 3;

    public static final int WIDTH_TILES = 3;
    public static final int HEIGHT_TILES = 4;
    private static final int SHROUD_REVEALING_RANGE = 10;
    private static final int ANIMATION_FRAME_DELAY = 2; // in ticks
    private static final int ANIMATION_LENGTH = 3;
    private static final int DEPLOY_TRY_INTERVAL = 5;
    
    private static final String FOOTPRINT = "xxx xxx ~~~ ~~~";

    private Alignment weapAlignment = Alignment.SOVIET;
    private boolean isCorrupted = false;
    
    private int currentPass;
    private int animationFrame;
    private int animationFrameTicks;
    private int deployTryTicks;
    
    private int tx, ty;
    
    private boolean isDoorsOpenAnimation;
    private boolean isDoorsCloseAnimation;
    
    private boolean isDoorsOpen;
    
    private EntityVehicle targetEntity;
    
    // 6 directions to exit from war factory
    private final int[] exitDirectionsX = { 0, -1, -1, 1, 1, 0 };
    private final int[] exitDirectionsY = { 1,  0,  1, 1, 0, 0 };
    
    private final Rectangle exitBoundingBox;
    
    private static final int CONSUME_POWER_VALUE = 30;
    private static final int BUILDING_COST = 2000;
    
    public EntityWarFactory(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, FOOTPRINT);

	this.weapAlignment = player.getAlignment();

	setBibType(BibType.MIDDLE);
	setProgressValue(-1);

	setMaxHp(100);
	setHp(getMaxHp());

	this.isDoorsOpen = false;
	
	this.buildingSpeed = 20;
	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();
	
	this.exitBoundingBox = new Rectangle(posX, posY, sizeWidth, sizeHeight - 24);
	this.unitProductionAlingment = Alignment.NEUTRAL;
	
	this.requiredToBuild.add(EntityPowerPlant.class);
	this.requiredToBuild.add(EntityProc.class);
    }

    private void initTextures() {
	ShpTexture texTop = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME_TOP);
	this.sheetTop = new SpriteSheet(texTop.getAsCombinedImage(owner.playerColor, false, 0, 0), 72, 48);
	
	ShpTexture texDown = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME_DOWN);
	weapDownNormal = texDown.getAsImage(0, owner.playerColor);
	weapDownCorrupted = texDown.getAsImage(1, owner.playerColor);	
    }

    @Override
    public void renderEntity(Graphics g) {
	float nx = posX;
	float ny = posY;

	// Draw downTexture
	if (this.currentPass == 0) {
	    if (!this.isCorrupted) {
		weapDownNormal.draw(nx, ny);
	    } else {
		weapDownCorrupted.draw(nx, ny);
	    }
	} else {
	    if (isDoorsOpenAnimation || isDoorsCloseAnimation) { 
		sheetTop.getSubImage(0, ((this.isCorrupted) ? this.CORRUPTION_INDEX : 0) + this.animationFrame).draw(nx, ny);;
	    } else {
		sheetTop.getSubImage(0, ((this.isCorrupted) ? this.CORRUPTION_INDEX : 0) + ((isDoorsOpen) ? 3 : 0)).draw(nx, ny);;
	    }
	}
	
	// Draw bounding box if debug mode is on
	if (Main.DEBUG_MODE) {
	    g.setLineWidth(2);
	    g.setColor(owner.playerColor);
	    g.draw(boundingBox);
	    g.setLineWidth(1);
	}
    }

    @Override
    public boolean shouldRenderedInPass(int passnum) {
	this.currentPass = passnum;
	
	return (passnum == 0) || (passnum == 2);
    }

    @Override
    public void updateEntity(int delta) {
	this.isCorrupted = this.getHp() <= this.getMaxHp() / 2;
	
	if (this.animationFrameTicks++ > this.ANIMATION_FRAME_DELAY) {
	    this.animationFrameTicks = 0;
	    
	    if (this.isDoorsOpenAnimation && !this.isDoorsCloseAnimation) {
		this.animationFrame++;
		
		if (this.animationFrame == this.ANIMATION_LENGTH) {
		    this.isDoorsOpenAnimation = false;
		    this.animationFrame = 0;
		    this.isDoorsOpen = true;
		}
	    } else if (!this.isDoorsOpenAnimation && this.isDoorsCloseAnimation) {
		this.animationFrame--;
		
		if (this.animationFrame == 0) {
		    this.isDoorsCloseAnimation = false;
		    this.isDoorsOpen = false;
		}
	    }
	}
	
	if (isDoorsOpen && this.targetEntity != null) {
	    if (this.deployTryTicks++ > DEPLOY_TRY_INTERVAL) {
		this.deployTryTicks = 0;
		
		tryToDeployEntity();
	    }
	}
    }

    private void tryToDeployEntity() {
	if (tryToMoveOutEntityToUnlockedCells(this.targetEntity)) {
	    targetEntity.isVisible = true;
	    
	    if (targetEntity == null || !targetEntity.boundingBox.intersects(this.exitBoundingBox)) { // if entity is still inside factory, don't close the doors
		animateCloseDoors();
		this.targetEntity = null;
	    }	    
	}
    }
    
    private boolean tryToMoveOutEntityToUnlockedCells(EntityVehicle v) {
	boolean isSuccess = false;

	int exitX = getTileX() / 24 + 1;
	int exitY = getTileY() / 24 + HEIGHT_TILES - 2;
	
	boolean isExitBlocked = isCellBlocked(exitX, exitY);
	
	if (isExitBlocked) {
	    // Try to nudge blocker
	    MobileEntity blocker = world.getMobileEntityInCell(new Pos(exitX, exitY));
	    if (blocker != null) {
		blocker.nudge(null, true);
	    }
	    
	    return false;
	}
	
	Path p = new Path();
	p.appendStep((int) targetEntity.getCenterPosX(), (int) targetEntity.getCenterPosY());
	p.appendStep(exitX, exitY);	
	
	for (int i = 0; i < 6; i++) {
	    int cellX = exitX + this.exitDirectionsX[i];
	    int cellY = exitY + this.exitDirectionsY[i];

	    if (!isCellBlocked(cellX, cellY)) {
		isSuccess = true;

		tx = cellX;
		ty = cellY;

		p.appendStep(cellX, cellY);

		v.startMovingByPath(p, this);		

		return true;
	    }	    
	}

	return false;
    }
    
    public boolean isCellBlocked(int cellX, int cellY) {
	return !world.isCellPassable(cellX, cellY) || (world.getEntityNonBuildingInPoint((cellX * 24) + 12, (cellY * 24) + 12) != null);
    }
    
    public boolean deployEntity(EntityVehicle target) {
	if (this.targetEntity != null) {
	    return false;
	}
	
	world.spawnEntityInWorld(target);
	target.setWorld(world);
	this.targetEntity = target;
	targetEntity.setPositionByCenter(this.getTileX() + 24 + 12, getTileY() + 24 * 1);
	targetEntity.currentFacing = 16;
	targetEntity.isVisible = false;
	
	animateOpenDoors();
	
	return false;
    }
    
    private void animateOpenDoors() {
	this.isDoorsOpenAnimation = true;
	this.animationFrameTicks = 0;
	this.animationFrame = 0;
	
	this.isDoorsCloseAnimation = false;
    }

    private void animateCloseDoors() {
	this.isDoorsCloseAnimation = true;
	this.animationFrameTicks = 0;
	this.animationFrame = this.ANIMATION_LENGTH;
	
	this.isDoorsOpenAnimation = false;
    }    
    
    @Override
    public void select() {
	this.isSelected = true;
    }

    @Override
    public void cancelSelect() {
	this.isSelected = false;
    }

    @Override
    public boolean isSelected() {
	return this.isSelected;
    }

    @Override
    public float getHeightInTiles() {
	return this.tileHeight;
    }

    @Override
    public float getWidthInTiles() {
	return this.tileWidth;
    }

    public Alignment getAlignment() {
	return this.weapAlignment;
    }

    @Override
    public int getRevealingRange() {
	return this.SHROUD_REVEALING_RANGE;
    }
    
    @Override
    public Image getTexture() {
	if (this.sheetTop == null) {
	    return null;
	}
	
	return sheetTop.getSubImage(0, 0);
    }
    
    public Image getBottomTexture() {
	return this.weapDownNormal;
    }

    @Override
    public boolean canDeploy() {
	return true;
    }

    @Override
    public void deploy() {
	executeDeployment();
    }

    @Override
    public void executeDeployment() {
	this.owner.getBase().setPrimaryWarFactory(this);
    }

    @Override
    public int getBuildingCost() {
	return this.BUILDING_COST;
    }

    @Override
    public int getConsumptionLevel() {
	return this.CONSUME_POWER_VALUE;
    }    
}
