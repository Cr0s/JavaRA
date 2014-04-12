package cr0s.javara.entity.building;

import org.newdawn.slick.Graphics;

import cr0s.javara.entity.Entity;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;

/**
 * Abstract class for any buildings in game.
 * @author Cr0s
 *
 */
public abstract class EntityBuilding extends Entity {
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
	 * Creates new building.
	 * @param aTileX tiled map grid-aligned location of building by X-axis 
	 * @param aTileY tiled map grid-aligned location of building by Y-axis 
	 * @param aTeam building team alignment.
	 * @param aPlayer building owner player object.
	 * @param aSizeWidth width of building boundaries in pixels.
	 * @param aSizeHeight height of building boundaries in pixels.
	 */
	public EntityBuilding(final int aTileX, final int aTileY, 
                		final Team aTeam, 
                		final Player aPlayer, 
                		final float aSizeWidth, 
                		final float aSizeHeight) {
	    
		super(aTileX, aTileY, aTeam, aPlayer, aSizeWidth, aSizeHeight);
		
		this.tileX = aTileX;
		this.tileY = aTileY;
		
		this.width = aSizeWidth;
		this.height = aSizeHeight;
		
		this.tileWidth = (int) aSizeWidth / 24;
		this.tileHeight = (int) aSizeHeight / 24;
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
	
	public void drawBib() {
	    
	}
	
	public abstract float getHeightInTiles();
	public abstract float getWidthInTiles();	
	
	public void setBibType(BibType bt) {
	    this.bibType = bt;
	}
	
	public BibType getBibType() {
	    return this.bibType;
	}
}
