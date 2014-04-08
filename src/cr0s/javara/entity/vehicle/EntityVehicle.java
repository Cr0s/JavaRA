package cr0s.javara.entity.vehicle;

import org.newdawn.slick.Graphics;

import cr0s.javara.entity.Entity;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;

public abstract class EntityVehicle extends Entity {
	public int tileX, tileY;
	public float x, y; // Actual screen coordinates for smooth movement
	
	public int hp, maxHp;
	
	public int rotation = 0;
	public int newRotation = 0;
	
	public boolean isPrimary = false, isRepairing = false, isInvuln = false, isDestroyed = false;
		
	public EntityVehicle(int tileX, int tileY, Team team, Player player, int sizeWidth, int sizeHeight) {
		super(tileX, tileY, team, player, sizeWidth, sizeHeight);
		
		this.x = this.tileX = tileX;
		this.y = this.tileY = tileY;
	}
	
	public void giveDamage(int amount) {
		if (!isInvuln) {
			hp -= amount;
			
			if (hp <= 0) {
				hp = 0;
				isDestroyed = true;
			}
		}
	}

	/**
	 * Sets rotation to entity immediately
	 * @param rot
	 */
	public void setRotation(int rot) {
		this.rotation = rot;
	}
	
	/**
	 * Sets rotation to entity to rotate to with rotation speed
	 * @param rot
	 */
	public void rotate(int rot) {
		this.newRotation = rot;
	}
	
	@Override
	public abstract void updateEntity(int delta);

	@Override
	public abstract void renderEntity(Graphics g);

	@Override
	public boolean shouldRenderedInPass(int passNum) {
		return true;
	}
}
