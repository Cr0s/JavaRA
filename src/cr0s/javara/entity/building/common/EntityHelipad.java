package cr0s.javara.entity.building.common;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.combat.ArmorType;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.Pos;

public class EntityHelipad extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IHaveCost {

    private SpriteSheet sheet;

    private int animIndex = 0;
    private int animDelayTicks = 0;
    private static final int ANIM_DELAY_TICKS = 2;

    private final String TEXTURE_NAME = "hpad.shp";
    private final String MAKE_TEXTURE_NAME = "hpadmake.shp";

    public static final int WIDTH_TILES = 2;
    public static final int HEIGHT_TILES = 3;

    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;

    private static final int POWER_CONSUMPTION_LEVEL = 10;
    private static final int SHROUD_REVEALING_RANGE = 5;

    private static final int BUILDING_COST = 500;
    
    private final static Pos spawnOffset = new Pos(0, 12);
    private final static int initialFacing = 12;
    
    private boolean isCharging = false;
    
    public EntityHelipad(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "xx xx ~~");

	setBibType(BibType.SMALL);
	setProgressValue(-1);

	setMaxHp(800);
	setHp(this.getMaxHp());

	this.armorType = ArmorType.WOOD;
	this.makeTextureName = MAKE_TEXTURE_NAME;

	initTextures();
	this.unitProductionAlingment = Alignment.NEUTRAL;
	
	this.requiredToBuild.add(EntityRadarDome.class);
	
	this.setName("hpad");
    }

    private void initTextures() {
	ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);
	sheet = new SpriteSheet(tex.getAsCombinedImage(owner.playerColor), tex.getAsImage(0, owner.playerColor).getWidth(), tex.getAsImage(0, owner.playerColor).getHeight());
    }

    @Override
    public void renderEntity(Graphics g) {
	float nx = posX;
	float ny = posY;

	int corruptionShift = 0;

	if (this.getHp() > this.getMaxHp() / 2) {
	    corruptionShift = 0;
	} else {
	    corruptionShift = 7;
	}

	sheet.startUse();
	sheet.getSubImage(0, corruptionShift + ((this.isCharging) ? animIndex : 0)).drawEmbedded(posX, posY, this.getTextureWidth(), this.getTextureHeight());
	sheet.endUse();

	// Draw bounding box if debug mode is on
	if (Main.DEBUG_MODE) {
	    g.setLineWidth(2);
	    g.setColor(owner.playerColor);
	    g.draw(boundingBox);
	    g.setLineWidth(1);
	}
	
	// Render repairing wrench
	if (this.repairIconBlink) {
	    repairImage.draw(this.boundingBox.getX() + this.boundingBox.getWidth() / 2 - repairImage.getWidth() / 2, this.boundingBox.getY() + this.boundingBox.getHeight() / 2 - repairImage.getHeight() / 2);
	}
    }

    @Override
    public boolean shouldRenderedInPass(int passnum) {
	return passnum == 0;
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	
	if (animDelayTicks++ > ANIM_DELAY_TICKS) {
	    animDelayTicks = 0;

	    this.animIndex = (this.animIndex + 1) % 5;
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
	return sheet.getSubImage(0, 0);
    }

    @Override
    public int getBuildingCost() {
	return BUILDING_COST;
    }

    public void deployEntity(EntityActor newInstance) {
	if (newInstance instanceof MobileEntity) {
	    final MobileEntity me = (MobileEntity) newInstance;
	    
	    me.isVisible = true;	    
	    newInstance.setWorld(this.world);
	  
	    world.spawnEntityInWorld(newInstance);
	    
	    me.setPos(this.getPosition().add(this.spawnOffset));
	    me.currentFacing = this.initialFacing;
	}
    }
    
    @Override
    public void onBuildFinished() {

    }
}
