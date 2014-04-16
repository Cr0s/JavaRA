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
	    	
	    	System.out.println("Generating path, moving from " + this.startX + "; " + this.startY + " to " + (int) this.goalX + "; " + (int) this.goalY);
	    	
	    	Step firstStep = this.currentPath.getStep(this.pathIndex);
	    	this.moveToAdjacentTile(firstStep.getX(), firstStep.getY());
	    } else {
		this.isMovingToCell = false;
		this.currentPath = null;
		this.isMovingByPath = false;
		this.pathIndex = 0;
	    }
	}
	
	public void moveToAdjacentTile(int tileX, int tileY) {
	    System.out.println("Moving to adjacent tile from " + (int) this.getPosX() + "; " + (int) this.getPosY() + " to " + tileX + "; " + tileY);
	    this.isMovingToCell = true;
	    
	    this.targetCellX = tileX;
	    this.targetCellY = tileY;
	    
	    this.moveX = (tileX - (int) Math.floor( this.getPosX() / 24));
	    this.moveY = (tileY - (int) Math.floor( this.getPosY() / 24));
	    
	    int rot = RotationUtil.getRotationFromXY(0, 0, moveX, moveY);
	    this.rotateTo(rot);	    
	    
	    this.doRotationTick();
	}
	
	private boolean isPathBlocked() {
	    for (int i = this.pathIndex; i < this.currentPath.getLength(); i++) {
		if (!world.isCellPassable(this.currentPath.getStep(i).getX(), this.currentPath.getStep(i).getY())) {
		    System.out.println("Path is blocked!");
		    return true;
		}
	    }
	    
	    return false;
	}
	
	private boolean isTargetCellReached() {
	   
	    boolean isReached = (Math.floor(this.getPosX() / 24) == this.targetCellX) && (Math.floor(this.getPosY() / 24) == this.targetCellY * 1.0f);
	    
	    if (isReached) {
		System.out.println("* Target reached.");
	    } else {
		System.out.println("Need one more move. " + Math.floor(this.getPosX() / 24) + " != " + this.targetCellX * 1.0f + "; " + Math.floor(this.getPosY() / 24) + " != " + this.targetCellY * 1.0f);
	    }
	    
	    return isReached;
	}
	
	private void switchToNextWaypointOrFinish() {
	    if (this.isMovingByPath && this.currentPath != null && this.pathIndex < this.currentPath.getLength() - 1) {
		this.pathIndex++;
		Step nextStep = this.currentPath.getStep(this.pathIndex);
		
		System.out.println("Switching to next waypoint...");
		
		if (world.isCellPassable(nextStep.getX(), nextStep.getY())) {
		    this.moveToAdjacentTile(nextStep.getX(), nextStep.getY());
		} else {
		    System.out.println("* Is not passable");
		    finishMoving();
		}
	    } else {
		finishMoving();
	    }
	}
	
	private boolean tryRepathIfPathBlocked() {
	    if (this.isPathBlocked()) {
		this.findPathAndMoveTo(this.goalX, this.goalY);
		
		return true;
	    }
	    
	    return false;
	}
	
	public void doMoveTick(int delta) {
	    doRotationTick();
	    
	    if (!this.isMovingToCell || this.isRotatingNow) {
		return;
	    }
	    
	    System.out.println("");
	    
	    // If path is blocked, trying to make a repath
	    if (tryRepathIfPathBlocked()) { // Path is blocked, there is another path or no any path, so we need stop
		return;
	    }
	    
	    float targetCellXCenter = this.targetCellX * 24 + 12;
	    float targetCellYCenter = this.targetCellY * 24 + 12;
	    
	    float nextX = this.getPosX() + this.moveX * delta * getMoveSpeed();
	    float nextY = this.getPosY() + this.moveY * delta * getMoveSpeed();
	    
	    // Check cell boundaries
	    if (-moveX * (targetCellXCenter - nextX) >= 1) {
		System.out.println("NextX: " + nextX + " is > than " + targetCellXCenter + " (" + -moveX * (targetCellXCenter - nextX) + ")");
		nextX = targetCellXCenter;
		//nextX = targetCellX * 24;
		//this.moveX = 0;
	    }
	    
	    if (-moveY * (targetCellYCenter - nextY) >= 1) {
		System.out.println("NextY: " + nextY + " is > than " + targetCellYCenter + " (" + -moveY * (targetCellYCenter - nextY) + ")");
		//nextY = targetCellY * 24;
		//this.moveY = 0;
		nextY = targetCellYCenter;
	    }    
	    
	    System.out.println("Moving from " + (int) this.getPosX() / 24 + "; " + (int) this.getPosY() / 24 + " to " + (int) nextX / 24 + "; " + (int) nextY / 24);
	    
	    if (isTargetCellReached()) {
		switchToNextWaypointOrFinish();
	    } else {
		this.posX = nextX - (this.sizeWidth / 4);
		this.posY = nextY - (this.sizeHeight / 4);
	    }
	}
	
	protected void drawPath(Graphics g) {
	    if (this.currentPath != null) {
		g.setColor(Color.green);
		g.setLineWidth(1);
		g.drawLine(this.getPosX(), this.getPosY(), this.currentPath.getStep(this.pathIndex).getX() * 24 + 12, this.currentPath.getStep(this.pathIndex).getY() * 24 + 12);
		g.fillOval(this.goalX * 24 + 12 - 2, this.goalY * 24 + 12 - 2, 5, 5);
		
		for (int i = this.pathIndex; i < this.currentPath.getLength() - 1; i++) {
		    Step from = this.currentPath.getStep(i);
		    Step to = this.currentPath.getStep(i + 1);
		    
		    g.fillOval(from.getX() * 24 + 12 - 2, from.getY() * 24 + 12 - 2, 5, 5);
		    g.fillOval(to.getX() * 24 + 12 - 2, to.getY() * 24 + 12 - 2, 5, 5);
		    
		    g.drawLine(from.getX() * 24 + 12, from.getY() * 24 + 12, to.getX() * 24 + 12, to.getY() * 24 + 12);
		}
	    }
	    
	    g.setColor(Color.gray); 
		for (int i = (int) (posX / 24 - 5); i < posX / 24 + 5; i++) {
		    for (int j = (int) (posY / 24 - 5); j < posY / 24 + 5; j++) {
			g.drawRect(i * 24, j * 24, 24, 24);
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
	    return this.posX + (this.sizeWidth / 4);
	}
	
	public float getPosY() {
	    return this.posY + (this.sizeHeight / 4);
	}	
	
	@Override
	public abstract void updateEntity(int delta);

	@Override
	public abstract void renderEntity(Graphics g);
	
	public abstract float getMoveSpeed();
	
	public enum RotationDirection { LEFT, RIGHT } // +1 or -1 to facing value
}
