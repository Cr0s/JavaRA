package cr0s.javara.entity.building;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;

import cr0s.javara.combat.ArmorType;
import cr0s.javara.combat.TargetType;
import cr0s.javara.combat.Warhead;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.render.EntityBlockingMap.FillsSpace;
import cr0s.javara.render.map.TileSet;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.util.Pos;

/**
 * Abstract class for any buildings in game.
 * @author Cr0s
 *
 */
public abstract class EntityBuilding extends EntityActor {
    /**
     * Building coordinates aligned to tiled map grid.
     */
    private int tileX, tileY;

    /**
     * Size of building in tiles.
     */
    protected int tileWidth, tileHeight;

    /**
     * Size of building in pixels
     */
    protected float width, height;

    /**
     * Speed of building.
     */
    public int buildingSpeed;

    /**
     * Make texture name.
     */
    public String makeTextureName;

    /**
     * Additional properties.
     * 
     * isPrimary - building is set as "primary". 
     * All created units will appear from this building.
     * 
     * isRepairing - building is in repairing state.
     * isInvuln - building is invulnerable for any kind of damage.
     * isDestroyed - building is destroyed.
     */
    private boolean isPrimary = false, isRepairing = false, 
	    isInvuln = false, isDestroyed = false;

    /**
     * Bib type. Big, middle or small.
     */
    private BibType bibType;

    /**
     * Progress current and maximum values.
     * 
     * Shows unit/building creation progress in progress bar.
     */
    private int progressValue, maxProgress;

    /**
     * Building's footprint.
     */
    private String footprint;
    private int[][] blockingCells;
    
    public String explosionSound = "kaboom22";
    
    /**
     * Creates new building.
     * @param aTileX tiled map grid-aligned location of building by X-axis 
     * @param aTileY tiled map grid-aligned location of building by Y-axis 
     * @param aTeam building team alignment.
     * @param aPlayer building owner player object.
     * @param aSizeWidth width of building boundaries in pixels.
     * @param aSizeHeight height of building boundaries in pixels.
     */
    public EntityBuilding(final Float aTileX, final Float aTileY, 
	    final Team aTeam, 
	    final Player aPlayer, 
	    final float aSizeWidth, 
	    final float aSizeHeight, 
	    String aFootprint) {

	super(aTileX, aTileY, aTeam, aPlayer, aSizeWidth, aSizeHeight);

	this.tileX = aTileX.intValue();
	this.tileY = aTileY.intValue();

	this.width = aSizeWidth;
	this.height = aSizeHeight;

	this.tileWidth = (int) aSizeWidth / 24;
	this.tileHeight = (int) aSizeHeight / 24;

	this.footprint = aFootprint;

	this.blockingCells = new int[this.tileWidth][this.tileHeight];
	generateCellsFromFootprint(aFootprint, this.blockingCells);
	
	this.fillsSpace = FillsSpace.ONE_OR_MORE_CELLS;
	
	requiredToBuild.add(EntityConstructionYard.class);
	
	this.armorType = ArmorType.CONCRETE;
	this.targetTypes.add(TargetType.GROUND);
    }

    @Override
    public abstract void updateEntity(int delta);

    @Override
    public abstract void renderEntity(Graphics g);

    @Override
    public abstract boolean shouldRenderedInPass(int passNum);

    /**
     * Gets tile X-axis building coordinate at tiled map grid.
     * @return X coordinate
     */
    public int getTileX() {
	return tileX;
    }

    /**
     * Sets tile X-axis building coordinate at tiled map grid.
     * @param aTileX new X coordinate
     */
    public void setTileX(final int aTileX) {
	this.tileX = aTileX;
    }

    /**
     * Gets tile Y-axis building coordinate at tiled map grid.
     * @return Y coordinate
     */
    public int getTileY() {
	return tileY;
    }

    /**
     * Sets tile Y-axis building coordinate at tiled map grid.
     * @param aTileY new Y coordinate
     */
    public void setTileY(final int aTileY) {
	this.tileY = aTileY;
    }


    public boolean isPrimary() {
	return isPrimary;
    }

    public void setPrimary(final boolean aIsPrimary) {
	this.isPrimary = aIsPrimary;
    }

    public boolean isRepairing() {
	return isRepairing;
    }

    public void setRepairing(final boolean aIsRepairing) {
	this.isRepairing = aIsRepairing;
    }

    public boolean isInvuln() {
	return isInvuln;
    }

    public void setInvuln(final boolean aIsInvuln) {
	this.isInvuln = aIsInvuln;
    }

    public boolean isDestroyed() {
	return isDestroyed;
    }

    public void setDestroyed(final boolean aIsDestroyed) {
	this.isDestroyed = aIsDestroyed;
    }

    public int getProgressValue() {
	return progressValue;
    }

    public void setProgressValue(final int aProgressValue) {
	this.progressValue = aProgressValue;
    }

    public int getMaxProgress() {
	return maxProgress;
    }

    public void setMaxProgress(final int aMaxProgress) {
	this.maxProgress = aMaxProgress;
    }

    public float getWidth() {
	return this.width;
    }

    public float getHeight() {
	return this.height;
    }

    public abstract float getHeightInTiles();
    public abstract float getWidthInTiles();	

    public void setBibType(BibType bt) {
	this.bibType = bt;
    }

    public BibType getBibType() {
	return this.bibType;
    }

    public String getFootprint() {
	return this.footprint;
    }

    /**
     * Generate 2D cells blocking description from OpenRA footprint format string.
     * @param footprint footprint format string
     * @param fc 2D array of cells
     */
    private void generateCellsFromFootprint(String footprint, int[][] fc) {
	int length = footprint.length();
	int x = 0, y = 0;

	for (int i = 0; i < length; i++) {
	    char c = footprint.charAt(i);

	    switch (c) {
	    case '_':
		fc[x][y] = TileSet.SURFACE_CLEAR_ID;
		x++;
		break;

	    case '~':
		fc[x][y] = TileSet.SURFACE_BUILDING_CLEAR_ID;
		x++;
		break;		    

	    case 'x':
		fc[x][y] = TileSet.SURFACE_BUILDING;
		x++;
		break;

	    case ' ':
		x = 0;
		y++;
		break;

	    default:
		fc[x][y] = 0;
		x++;
		break;
	    }
	}
    }

    public int[][] getBlockingCells() {
	return this.blockingCells;
    }

    public abstract Image getTexture();

    public void changeCellPos(int newCellX, int newCellY) {
	this.tileX = newCellX * 24;
	this.tileY = newCellY * 24;

	this.posX = this.tileX;
	this.posY = this.tileY;

	// Refresh bounding box
	this.boundingBox.setBounds(posX, posY, sizeWidth, sizeHeight);
    }

    @Override
    public String toString() {
	return String.format("Building [%s, (%s, %s), (%s, %s), visible: %s, dead: %s]", this.getClass().getName(), this.getTileX(), this.getTileY(), this.posX, this.posY, this.isVisible, this.isDead());
    }

    public int getTextureWidth() {
	return this.getTexture().getWidth();
    }
    public int getTextureHeight() {
	return this.getTexture().getHeight();
    }

    public void onBuildFinished() {

    }

    @Override
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia) {
	return null;
    }

    @Override
    public void resolveOrder(Order order) {
    }
    
    public void explodeBuilding() {
	SoundManager.getInstance().playSfxAt(this.explosionSound, ((EntityActor) this).getPosition());
	
	for (int bX = 0; bX < getWidthInTiles(); bX++) {
	    for (int bY = 0; bY < getHeightInTiles(); bY++) {
		world.spawnExplosionAt(new Pos(this.posX + bX * 24, this.posY + bY * 24), "fball1.shp");
	    }
	}
    }
    
    @Override
    public void giveDamage(EntityActor firedBy, int amount, Warhead warhead) {
	super.giveDamage(firedBy, amount, warhead);
	
	if (getHp() <= 0) {
	    explodeBuilding();
	}
    }
}
