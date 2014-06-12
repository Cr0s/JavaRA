package cr0s.javara.entity.building.common;

import java.util.ArrayList;
import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.IPips;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.actor.activity.activities.harvester.FindResources;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IOreCapacitor;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.order.ITargetLines;
import cr0s.javara.order.TargetLine;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.Pos;

public class EntityProc extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IPips, IOreCapacitor, ITargetLines, IHaveCost {

    private SpriteSheet sheet;

    private Image normal, corrupted;
    private final String TEXTURE_NAME = "proc.shp";
    private final String MAKE_TEXTURE_NAME = "procmake.shp";

    public static final int WIDTH_TILES = 3;
    public static final int HEIGHT_TILES = 4;

    private static final int POWER_CONSUMPTION_LEVEL = 20;

    private static final int SHROUD_REVEALING_RANGE = 9;

    // Harverster offset position
    private static final int HARV_OFFSET_X = 1;
    private static final int HARV_OFFSET_Y = 2;

    public static final int HARV_FACING = 8;
    
    // Ore capacity
    public static final int MAX_CAPACITY = 2000;
    public static final int PIPS_COUNT = 17;
    
    private LinkedList<TargetLine> targetLines = new LinkedList<>();
    
    private static final int BUILDING_COST = 1400;
    
    public EntityProc(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "_x_ xxx x~~ ~~~");

	setBibType(BibType.MIDDLE);
	setProgressValue(-1);

	setMaxHp(100);
	setHp(getMaxHp());

	this.buildingSpeed = 80;//20;
	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();
	
	this.unitProductionAlingment = Alignment.NEUTRAL;
	this.requiredToBuild.add(EntityPowerPlant.class);
    }

    @Override
    public void onBuildFinished() {
	super.onBuildFinished();
	spawnHarvester();
    }
    
    private void spawnHarvester() {
	if (world == null) {
	    return;
	}
		
	Pos harvCell = getHarvesterCell();
	EntityHarvester harv = new EntityHarvester(harvCell.getX() * 24f, harvCell.getY() * 24f, team, owner);
	
	harv.currentFacing = HARV_FACING;
	harv.isVisible = true;
	harv.setWorld(world);
	
	harv.linkedProc = this;
	harv.queueActivity(new FindResources());
	
	world.spawnEntityInWorld(harv);
    }
    
    public Pos getHarvesterCell() {
	return new Pos((this.getTileX() / 24) + HARV_OFFSET_X, (this.getTileY() / 24) + HARV_OFFSET_Y);	
    }
    
    private void initTextures() {
	ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);
	corrupted = tex.getAsImage(1, owner.playerColor);
	normal = tex.getAsImage(0, owner.playerColor);	
    }

    @Override
    public void renderEntity(Graphics g) {
	float nx = posX;
	float ny = posY;

	if (this.getHp() > this.getMaxHp() / 2) {
	    normal.draw(nx, ny);
	} else {
	    corrupted.draw(nx, ny);
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
	return passnum == 0;
    }

    @Override
    public void updateEntity(int delta) {
	// TODO Auto-generated method stub

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

    @Override
    public int getConsumptionLevel() {
	return this.POWER_CONSUMPTION_LEVEL;
    }

    @Override
    public int getRevealingRange() {
	return this.SHROUD_REVEALING_RANGE;
    }
    
    @Override
    public Image getTexture() {
	return normal;
    }

    public void acceptResources(int aCapacity) {
	owner.getBase().giveOre(aCapacity);
    }
    
    @Override
    public int getPipCount() {
	return this.PIPS_COUNT;
    }

    @Override
    public Color getPipColorAt(int i) {
	return (owner.getBase().ore * this.PIPS_COUNT > i * owner.getBase().oreCapacity) ? Color.yellow : null;
    }

    @Override
    public int getOreCapacityValue() {
	return this.MAX_CAPACITY;
    }

    @Override
    public LinkedList<TargetLine> getTargetLines() {
	return linkedHarvestersLines();
    }

    private LinkedList<TargetLine> linkedHarvestersLines() {
	this.targetLines.clear();
	
	for (Entity e : world.getEntitiesList()) {
	    if (e instanceof EntityHarvester) {
		if (((EntityHarvester)e).linkedProc == this) {
		    this.targetLines.add(new TargetLine(new Pos(this.boundingBox.getCenterX(), this.boundingBox.getCenterY()), ((EntityHarvester) e).getCenterPos(), Color.yellow));
		}
	    }
	}
	
	return this.targetLines;
    }

    @Override
    public int getBuildingCost() {
	return BUILDING_COST;
    }        
}
