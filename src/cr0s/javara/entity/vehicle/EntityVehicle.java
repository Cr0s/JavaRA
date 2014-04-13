package cr0s.javara.entity.vehicle;

import org.newdawn.slick.Graphics;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;

public abstract class EntityVehicle extends Entity implements IMovable {
	public int tileX, tileY;
	
	public boolean isRotatingNow = false;
	public int rotation = 0;
	public int newRotation = 0;
	public int maxRotation = 32;
	
	public RotationDirection rotationDirection;
	
	public boolean isPrimary = false, isRepairing = false, isInvuln = false, isDestroyed = false;
		
	public EntityVehicle(float posX, float posY, Team team, Player player, int sizeWidth, int sizeHeight) {
		super(posX, posY, team, player, sizeWidth, sizeHeight);
	}

	/**
	 * Do a rotation tick.
	 * @return result of rotation. True - rotaton is finished. False - rotation in process.
	 */
	public boolean doRotationTick() {
	    if (this.isRotatingNow) {
		if (this.getRotation() == this.newRotation) {
		    this.isRotatingNow = false;
		    return true;
		}
		
		if (this.rotationDirection == RotationDirection.LEFT) {
		    this.setRotation((this.getRotation() + 1) % maxRotation);
		} else if (this.rotationDirection == RotationDirection.RIGHT) {
		    this.setRotation((this.getRotation() - 1) % maxRotation);
		}
		
		return false;
	    }
	
	    return true;
	}
	
	/**
	 * Sets rotation to entity immediately.
	 * @param rot
	 */
	public void setRotation(int rot) {
	    if (rot < 0) { rot = 31; } 
		this.rotation = rot;
	}
	
	/**
	 * Sets desired rotation and let entity rotate with some rotation speed to desired rotation;
	 * @param rot desired rotation value
	 */
	public void rotateTo(int rot) {
	    rot = rot % 32;

	    this.newRotation = rot;
	    
	    // Select nearest rotation direction
	    if (getRotation() >= 24 && rot <= 8) {
		this.rotationDirection = RotationDirection.LEFT;
	    } else if (getRotation() <= 8 && rot >= 24) {
		this.rotationDirection = RotationDirection.RIGHT;
	    } else
	    if (getRotation() < rot) {
		this.rotationDirection = RotationDirection.LEFT;
	    } else if (getRotation() > rot){
		this.rotationDirection = RotationDirection.RIGHT;
	    } else {
		this.isRotatingNow = false;
		return;
	    }

	    this.isRotatingNow = true;
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
	
	public enum RotationDirection { LEFT, RIGHT } // +1 or -1 to facing value
}
