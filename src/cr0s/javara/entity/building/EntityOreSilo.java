package cr0s.javara.entity.building;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.IPips;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.actor.activity.activities.harvester.FindResources;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class EntityOreSilo extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IPips, IOreCapacitor {

    private SpriteSheet sheet;
    
    private final String TEXTURE_NAME = "silo.shp";
    private final String MAKE_TEXTURE_NAME = "silomake.shp";

    public static final int WIDTH_TILES = 1;
    public static final int HEIGHT_TILES = 1;

    private static final int POWER_CONSUMPTION_LEVEL = 5;

    private static final int SHROUD_REVEALING_RANGE = 3;
    
    // Ore capacity
    public static final int MAX_CAPACITY = 1500;
    public static final int PIPS_COUNT = 5;
    
    private int oreLevel = 0;
    private final int CORRUPTED_OFFSET = 5;
    
    public EntityOreSilo(Integer tileX, Integer tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "x");

	setBibType(BibType.NONE);
	setProgressValue(-1);

	setMaxHp(25);
	setHp(getMaxHp());

	this.buildingSpeed = 80;
	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();
    }

    @Override
    public void onBuildFinished() {
    }
    
    private void initTextures() {
	ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);
	
	this.sheet = new SpriteSheet(tex.getAsCombinedImage(owner.playerColor), 24, 24);
    }

    @Override
    public void renderEntity(Graphics g) {
	float nx = posX;
	float ny = posY;

	int textureIndex = this.oreLevel;
	
	if (this.getHp() < this.getMaxHp() / 2) {
	    textureIndex += CORRUPTED_OFFSET;
	}

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
	// Update ore level
	this.oreLevel = 0;
	
	for (int i = 1; i < this.getPipCount(); i++) {
	    if (this.getPipColorAt(i) != null) {
		this.oreLevel++;
	    }
	}
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
    public int getPipCount() {
	return this.PIPS_COUNT;
    }

    @Override
    public Color getPipColorAt(int i) {
	return (owner.getBase().oreValue * this.PIPS_COUNT > i * owner.getBase().oreCapacity) ? Color.yellow : null;
    }

    @Override
    public int getOreCapacityValue() {
	return this.MAX_CAPACITY;
    }        
}
