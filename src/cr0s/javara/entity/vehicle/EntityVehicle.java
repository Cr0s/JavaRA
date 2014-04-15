package cr0s.javara.entity.vehicle;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.util.RotationUtil;

public abstract class EntityVehicle extends Entity implements IMovable, Mover {
	public int tileX, tileY;
	
	public boolean isRotatingNow = false;
	public int rotation = 0;
	public int newRotation = 0;
	public int maxRotation = 32;
	
	public RotationDirection rotationDirection;
	
	public boolean isPrimary = false, isRepairing = false, isInvuln = false, isDestroyed = false;
		
	public int targetCellX, targetCellY;
	public boolean isMovingToCell;
	
	public int goalX, goalY;
	public int startX, startY;
	public Path currentPath = null;
	public int pathIndex = 0;
	public boolean isMovingByPath;
	
	protected float moveSpeed = 0.1f;
	
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
		
	public void findPathAndMoveTo(int aGoalX, int aGoalY) {
	    Path path = world.getVehiclePathfinder().findPathFromTo(this, aGoalX, aGoalY);
	    this.startX = (int) Math.floor(this.getPosX() / 24);
	    this.startY = (int) Math.floor(this.getPosY() / 24);
	    if (path != null) {
		this.currentPath = path;
		
		this.isMovingByPath = true;
	    	this.pathIndex = 1;
	    	
	    	this.goalX = aGoalX;
	    	this.goalY = aGoalY;
	    	
	    	Step firstStep = this.currentPath.getStep(this.pathIndex);
	    	this.moveToAdjacentTile(firstStep.getX(), firstStep.getY());
	    }
	}
	
	public void moveToAdjacentTile(int tileX, int tileY) {
	    this.isMovingToCell = true;
	    
	    this.targetCellX = tileX;
	    this.targetCellY = tileY;
	    
	    this.moveX = (tileX - (int) Math.floor( this.getPosX() / 24));
	    this.moveY = (tileY - (int) Math.floor( this.getPosY() / 24));
	    
	    int rot = RotationUtil.getRotationFromXY(0, 0, moveX, moveY);
	    this.rotateTo(rot);	    
	    
	    this.doRotationTick();
	}
	
	public void doMoveTick(int delta) {
	    if (this.isMovingToCell && !this.isRotatingNow) {		
		float nextX = this.getPosX() + this.moveX * delta * getMoveSpeed();
		float nextY = this.getPosY() + this.moveY * delta * getMoveSpeed();

		this.boundingBox.setCenterX(nextX);
		this.boundingBox.setCenterY(nextY);
		
		this.posX = this.boundingBox.getMinX();
		this.posY = this.boundingBox.getMinY();
		
		int nextCellX = (int) Math.floor(nextX / 24);
		int nextCellY = (int) Math.floor(nextY / 24);

		// Cell is reached?
		if (nextCellX == this.targetCellX) {
		    this.moveX = 0;
		}

		if (nextCellY == this.targetCellY) {
		    this.moveY = 0;
		}	
		
		if (this.moveX == 0 && this.moveY == 0) {
		    this.pathIndex++;
		    
		    if (this.pathIndex < this.currentPath.getLength()) {
			Step nextStep = this.currentPath.getStep(this.pathIndex);
			
			if (world.isCellPassable(nextStep.getX(), nextStep.getY())) {
			    this.moveToAdjacentTile(nextStep.getX(), nextStep.getY());
			} else {
			    finishMoving();
			}
		    } else {
			finishMoving();
		    }
		}
	    }
	}
	
	protected void drawPath(Graphics g) {
	    if (this.currentPath != null) {
		g.setColor(Color.green);
		g.setLineWidth(1);
		g.drawLine(this.getPosX() + 12, this.getPosY() + 12, this.goalX * 24 + 12, this.goalY * 24 + 12);
		g.fillOval(this.goalX * 24 + 12 - 2, this.goalY * 24 + 12 - 2, 5, 5);
		
		for (int i = this.pathIndex; i < this.currentPath.getLength() - 1; i++) {
		    Step from = this.currentPath.getStep(i);
		    Step to = this.currentPath.getStep(i + 1);
		    
		    g.fillOval(from.getX() * 24 + 12 - 2, from.getY() * 24 + 12 - 2, 5, 5);
		    g.fillOval(to.getX() * 24 + 12 - 2, to.getY() * 24 + 12 - 2, 5, 5);
		    
		    g.drawLine(from.getX() * 24 + 12, from.getY() * 24 + 12, to.getX() * 24 + 12, to.getY() * 24 + 12);
		}
	    }
	}
	
	public void finishMoving() {
	    this.moveX = 0;
	    this.moveY = 0;
	    
	    this.goalX = 0;
	    this.goalY = 0;
	    
	    this.isMovingToCell = false;
	    this.isMovingByPath = false;
	    this.pathIndex = 0;
	    this.currentPath = null;
	}
	
	public float getPosX() {
	    return this.boundingBox.getCenterX();
	}
	
	public float getPosY() {
	    return this.boundingBox.getCenterY();
	}	
	
	@Override
	public abstract void updateEntity(int delta);

	@Override
	public abstract void renderEntity(Graphics g);
	
	public abstract float getMoveSpeed();
	
	public enum RotationDirection { LEFT, RIGHT } // +1 or -1 to facing value
}
