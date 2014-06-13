package cr0s.javara.entity.building.soviet;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.IDefense;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.IPips;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.actor.activity.activities.harvester.FindResources;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IOreCapacitor;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.building.common.EntityWarFactory;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class EntityTeslaCoil extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IHaveCost, IDefense {

    private SpriteSheet sheet;
    
    private final String TEXTURE_NAME = "tsla.shp";
    private final String MAKE_TEXTURE_NAME = "tslamake.shp";

    public static final int WIDTH_TILES = 1;
    public static final int HEIGHT_TILES = 2;

    private static final int POWER_CONSUMPTION_LEVEL = 150;

    private static final int SHROUD_REVEALING_RANGE = 3;
    
    private final int CORRUPTED_OFFSET = 10;

    private int currentFrame = 0;
    private static final int BUILDING_COST = 1200;
    
    public EntityTeslaCoil(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "_ x");

	setBibType(BibType.NONE);
	setProgressValue(-1);

	setMaxHp(400);
	setHp(getMaxHp());

	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();
	
	this.unitProductionAlingment = Alignment.SOVIET;

	this.requiredToBuild.add(EntityWarFactory.class);
    }
    
    private void initTextures() {
	ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);
	
	this.sheet = new SpriteSheet(tex.getAsCombinedImage(owner.playerColor), 24, 48);
    }

    @Override
    public void renderEntity(Graphics g) {
	float nx = posX;
	float ny = posY;

	int textureIndex = (this.getHp() < this.getMaxHp() / 2) ? this.CORRUPTED_OFFSET + this.currentFrame  : this.currentFrame;

	this.sheet.getSubImage(0, textureIndex).draw(nx, ny);
	
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
	return passnum == -1;
    }

    @Override
    public void updateEntity(int delta) {
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
	return this.sheet.getSubImage(0, 0);
    }

    @Override
    public int getBuildingCost() {
	return BUILDING_COST;
    }        
}
