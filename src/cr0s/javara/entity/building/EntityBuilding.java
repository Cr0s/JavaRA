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
	 * Health of building. Current and maximum HP.
	 */
	private int hp, maxHp;
	
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
	}
	
	/**
	 * Gives damage to entity with specified amount.
	 * @param amount damage amount
	 */
	public final void giveDamage(final int amount) {
		if (!isInvuln) {
			hp -= amount;
			
			if (hp <= 0) {
				hp = 0;
				isDestroyed = true;
			}
		}
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

	/**
	 * Gets building's HP value.
	 * @return HP amount
	 */
	public int getHp() {
	    return hp;
	}

	public void setHp(final int aHp) {
	    this.hp = aHp;
	}

	public int getMaxHp() {
	    return maxHp;
	}

	public void setMaxHp(final int aMaxHp) {
	    this.maxHp = aMaxHp;
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
}
