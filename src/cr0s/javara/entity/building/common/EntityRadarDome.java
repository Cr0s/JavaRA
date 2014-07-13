package cr0s.javara.entity.building.common;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.combat.ArmorType;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.resources.SoundManager;

public class EntityRadarDome extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IHaveCost {
    private Image normal, corrupted;
    private final String TEXTURE_NAME = "dome.shp";
    private final String MAKE_TEXTURE_NAME = "domemake.shp";

    public static final int WIDTH_TILES = 2;
    public static final int HEIGHT_TILES = 3;

    private static final int POWER_CONSUMPTION_LEVEL = 40;
    private static final int BUILDING_COST = 1600;
    private static final int SHROUD_REVEALING_RANGE = 15;
    
    public EntityRadarDome(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "xx xx ~~");

	setBibType(BibType.SMALL);
	setProgressValue(-1);

	setMaxHp(1000);
	setHp(getMaxHp());

	this.armorType = ArmorType.WOOD;
	
	this.buildingSpeed = 45;
	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();
	
	this.unitProductionAlingment = Alignment.NEUTRAL;
	
	this.requiredToBuild.add(EntityPowerPlant.class);
	this.requiredToBuild.add(EntityProc.class);
	
	this.setName("dome");
    }

    private void initTextures() {
	ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);
	normal = tex.getAsImage(0, owner.playerColor);	
	corrupted = tex.getAsImage(1, owner.playerColor);
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
    public int getRevealingRange() {
	return this.SHROUD_REVEALING_RANGE;
    }
    
    @Override
    public Image getTexture() {
	return normal;
    }

    @Override
    public int getConsumptionLevel() {
	return this.POWER_CONSUMPTION_LEVEL;
    }      
    
    @Override
    public void onBuildFinished() {
	super.onBuildFinished();
	
	if (this.owner == Main.getInstance().getPlayer() && !Main.getInstance().getPlayer().getBase().isLowPower()) {
	    SoundManager.getInstance().playSfxGlobal("radaron2", 0.9f);
	}
    }

    @Override
    public int getBuildingCost() {
	return BUILDING_COST;
    }
}
