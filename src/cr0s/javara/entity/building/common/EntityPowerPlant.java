package cr0s.javara.entity.building.common;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IPowerProducer;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class EntityPowerPlant extends EntityBuilding implements ISelectable, IPowerProducer, IShroudRevealer, IHaveCost {
    private Image normal, corrupted;
    private final String TEXTURE_NAME = "powr.shp";
    private final String MAKE_TEXTURE_NAME = "powrmake.shp";

    public static final int WIDTH_TILES = 2;
    public static final int HEIGHT_TILES = 3;

    private static final int POWER_PRODUCTION_LEVEL = 100;

    private static final int SHROUD_REVEALING_RANGE = 7;
    private static final int BUILDING_COST = 300;

    public EntityPowerPlant(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "xx xx ~~");

	setBibType(BibType.SMALL);
	setProgressValue(-1);

	setMaxHp(50);
	setHp(getMaxHp());

	this.buildingSpeed = 90;//50;
	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();
	
	this.unitProductionAlingment = Alignment.NEUTRAL;
	
	this.setName("powr");
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
    public int getPowerProductionLevel() {
	return POWER_PRODUCTION_LEVEL;
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
    public int getBuildingCost() {
	return BUILDING_COST;
    }      
}
