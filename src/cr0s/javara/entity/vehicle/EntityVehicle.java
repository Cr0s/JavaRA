package cr0s.javara.entity.vehicle;

import org.newdawn.slick.Graphics;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;

public abstract class EntityVehicle extends Entity implements IMovable {
	public int tileX, tileY;
	
	public int rotation = 0;
	public int newRotation = 0;
	
	public boolean isPrimary = false, isRepairing = false, isInvuln = false, isDestroyed = false;
		
	public EntityVehicle(float posX, float posY, Team team, Player player, int sizeWidth, int sizeHeight) {
		super(posX, posY, team, player, sizeWidth, sizeHeight);
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
	
	/**
	 * Returns rotation value
	 * @return rotation value
	 */
	public int getRotation() {
		return this.rotation;
	}
	
	@Override
	public abstract void updateEntity(int delta);

	@Override
	public abstract void renderEntity(Graphics g);
}
