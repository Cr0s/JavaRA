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

public class EntityProc extends EntityBuilding implements ISelectable, IPowerConsumer, IShroudRevealer, IPips {

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
    
    public EntityProc(Integer tileX, Integer tileY, Team team, Player player) {
	super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24, "_x_ xxx x~~ ~~~");

	setBibType(BibType.MIDDLE);
	setProgressValue(-1);

	setMaxHp(100);
	setHp(getMaxHp());

	this.buildingSpeed = 80;//20;
	this.makeTextureName = MAKE_TEXTURE_NAME;
	initTextures();
    }

    @Override
    public void onBuildFinished() {
	spawnHarvester();
    }
    
    private void spawnHarvester() {
	if (world == null) {
	    return;
	}
	
	System.out.println("Spawning harvester");
	
	Point harvCell = getHarvesterCell();
	EntityHarvester harv = new EntityHarvester(harvCell.getX() * 24f, harvCell.getY() * 24f, team, owner);
	
	harv.currentFacing = HARV_FACING;
	harv.isVisible = true;
	harv.setWorld(world);
	
	harv.linkedProc = this;
	harv.queueActivity(new FindResources());
	
	world.spawnEntityInWorld(harv);
    }
    
    public Point getHarvesterCell() {
	return new Point((this.getTileX() / 24) + HARV_OFFSET_X, (this.getTileY() / 24) + HARV_OFFSET_Y);	
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
	return (owner.getBase().oreValue * this.PIPS_COUNT > i * owner.getBase().oreCapacity) ? Color.yellow : null;
    }        
}
