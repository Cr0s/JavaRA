package cr0s.javara.entity.building.soviet;

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
import cr0s.javara.entity.building.common.EntityPowerPlant;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.Pos;

public class EntityBarracks extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IHaveCost {

    private SpriteSheet sheet;

    private int animIndex = 0;
    private int animDelayTicks = 0;
    private static final int ANIM_DELAY_TICKS = 2;

    private final String TEXTURE_NAME = "barr.shp";
    private final String MAKE_TEXTURE_NAME = "barrmake.shp";

    public static final int WIDTH_TILES = 2;
    public static final int HEIGHT_TILES = 3;

    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;

    private static final int POWER_CONSUMPTION_LEVEL = 10;
    private static final int SHROUD_REVEALING_RANGE = 10;

    private static final int BUILDING_COST = 400;
    
    private Pos rallyPos;
    private Pos exitPos;
    
    public EntityBarracks(Float tileX, Float tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "xx xx ~~");

	setBibType(BibType.SMALL);
	setProgressValue(-1);

	setMaxHp(800);
	setHp(getMaxHp());

	this.armorType = ArmorType.WOOD;
	this.makeTextureName = MAKE_TEXTURE_NAME;

	initTextures();
	this.unitProductionAlingment = Alignment.SOVIET;
	
	this.requiredToBuild.add(EntityPowerPlant.class);
	
	this.setName("barr");
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
	    corruptionShift = 10;
	}

	sheet.startUse();
	sheet.getSubImage(0, corruptionShift + animIndex).drawEmbedded(posX, posY, this.getTextureWidth(), this.getTextureHeight());
	sheet.endUse();

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
	if (animDelayTicks++ > ANIM_DELAY_TICKS) {
	    animDelayTicks = 0;

	    this.animIndex = (this.animIndex + 1) % 10;
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
	    
	    Path p = new Path();
	    p.appendStep((int) exitPos.getX(), (int) exitPos.getY());
	    p.appendStep((int) rallyPos.getX(), (int) rallyPos.getY());
	    
	    SubCell freeSubCell = world.blockingEntityMap.getFreeSubCell(rallyPos, SubCell.CENTER);
	    if (freeSubCell != null) {
		me.currentSubcell = freeSubCell;
		me.desiredSubcell = freeSubCell;
		me.setCellPos(exitPos);
		
		me.startMovingByPath(p, this);
	    } else {
		SubCell sc = SubCell.CENTER;

		MobileEntity blocker = world.getMobileEntityInCell(exitPos);
		if (blocker != null) {
		    blocker.nudge(me, true);
		}
		
		blocker = world.getMobileEntityInCell(rallyPos);
		if (blocker != null) {
		    blocker.nudge(me, true);
		}
		
		me.setCellPos(exitPos);
		me.currentSubcell = sc;
		me.desiredSubcell = sc;
		me.startMovingByPath(p, this);
	    }
	}
    }
    
    @Override
    public void onBuildFinished() {
	this.exitPos = new Pos((posX) / 24, (posY + 1 * 24) / 24);
	this.rallyPos = new Pos((posX) / 24, (posY + 2 * 24) / 24);	
    }
}
