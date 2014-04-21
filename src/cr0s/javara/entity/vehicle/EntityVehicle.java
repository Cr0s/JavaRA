package cr0s.javara.entity.vehicle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.util.RotationUtil;

public abstract class EntityVehicle extends Entity implements IMovable, Mover, IShroudRevealer {
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
	
	private static final int REPATH_RANGE = 3;
	
	private int moveWaitTicks = 0;

	protected int buildingSpeed;
	
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
		
	public boolean findPathAndMoveTo(int aGoalX, int aGoalY) {
	    Path path = world.getVehiclePathfinder().findPathFromTo(this, aGoalX, aGoalY);
	    this.startX = (int) this.getCenterPosX() / 24;
	    this.startY = (int) this.getCenterPosY() / 24;
	    if (path != null) {		
		startMovingByPath(path);
	    	
	    	return true;
	    } else {
		this.isMovingToCell = false;
		this.currentPath = null;
		this.isMovingByPath = false;
		this.pathIndex = 0;
	    }
	    
	    return false;
	}
	
	public void startMovingByPath(Path p) {
	    this.currentPath = p;
	    
	    this.isMovingByPath = true;
	    this.pathIndex = 1;

	    this.goalX = p.getX(p.getLength() - 1);
	    this.goalY = p.getY(p.getLength() - 1);

	    //System.out.println("Generating path, moving from " + this.startX * 24 + "; " + this.startY * 24 + " to " + (int) this.goalX * 24 + "; " + (int) this.goalY * 24);

	    Step firstStep = this.currentPath.getStep(this.pathIndex);
	    if (!this.isMovingToCell) {
		this.moveToAdjacentTile(firstStep.getX(), firstStep.getY());
	    }	    
	}
	
	public void moveToAdjacentTile(int tileX, int tileY) {
	    // Center unit by current cell
	    setPositionByCenter(((int)Math.floor(this.getCenterPosX() / 24) * 24) + 12, ((int)Math.floor(this.getCenterPosY() / 24) * 24) + 12);
	    
	    //System.out.println("Moving to adjacent tile from " + (int) this.getCenterPosX() + "; " + (int) this.getCenterPosY() + " to " + tileX * 24 + "; " + tileY * 24);
	    this.isMovingToCell = true;
	    
	    this.targetCellX = tileX;
	    this.targetCellY = tileY;
	    
	    this.moveX = (tileX - (int) this.getCenterPosX() / 24);
	    this.moveY = (tileY - (int) this.getCenterPosY() / 24);
	    
	    //System.out.println("Move: " + moveX + " " + moveY);
	    
	    int rot = RotationUtil.getRotationFromXY(0, 0, moveX, moveY);
	    this.rotateTo(rot);	    
	    
	    this.doRotationTick();
	}
	
	private boolean isPathBlocked() {
	    for (int i = this.pathIndex; i < this.currentPath.getLength(); i++) {
		if (!world.isCellPassable(this.currentPath.getStep(i).getX(), this.currentPath.getStep(i).getY())) {
		    //System.out.println("Path is blocked!");
		    return true;
		}
	    }
	    
	    return false;
	}
	
	private boolean isTargetCellReached() {
	    return (Math.abs(this.getCenterPosX() - (this.targetCellX * 24 + 12)) <= 3) && (Math.abs(this.getCenterPosY() - (this.targetCellY * 24 + 12)) <= 3);
	}
	
	private void switchToNextWaypointOrFinish() {
	    if (this.isMovingByPath && this.currentPath != null && this.pathIndex < this.currentPath.getLength() - 1) {
		this.pathIndex++;
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
	
	private boolean tryRepathIfPathBlocked() {
	    // Is destination cell blocked?
	    if (!world.isCellPassable(goalX, goalY)) {
		setGoalXYToFreeCellInRange(goalX, goalY, REPATH_RANGE);
		return true;
	    } else // Or some cell in path is blocked?	    
	    if (this.isPathBlocked()) {
		this.findPathAndMoveTo(this.goalX, this.goalY);
		
		return true;
	    }
	    
	    return false;
	}
	
	public void setGoalXYToFreeCellInRange(int gX, int gY, int range) {
	    int numAttempts = range * range;
	    Random r = new Random();
	    
	    for (int i = 0; i < numAttempts; i++) {
		int newX = gX - (range / 2) + r.nextInt(range / 2);
		int newY = gY - (range / 2) + r.nextInt(range / 2);
		
		if (world.isCellPassable(newX, newY)) {
		    if (findPathAndMoveTo(newX, newY)) {
			return;
		    }
		}
	    }
	}
	
	public void doMoveTick(int delta) {
	    doRotationTick();
	    
	    if (!this.isMovingToCell || this.isRotatingNow || this.moveWaitTicks-- > 0) {
		return;
	    }
	    
	    float targetCellXCenter = this.targetCellX * 24 + 12;
	    float targetCellYCenter = this.targetCellY * 24 + 12;
	    
	    if (!world.isCellPassable((int) targetCellXCenter / 24, (int) targetCellYCenter / 24)) {
		this.moveWaitTicks = 20;
		return;
	    }
	    
	    float nextX = this.getCenterPosX() + this.moveX * delta * getMoveSpeed();
	    float nextY = this.getCenterPosY() + this.moveY * delta * getMoveSpeed();
	    
	    // If path is blocked, trying to make a repath
	    if (this.isMovingByPath && tryRepathIfPathBlocked()) { // Path is blocked, there is another path or no any path, so we need stop
		
		if (!isMovingByPath) { // New path is not found
		    // Center unit by current cell
		    setPositionByCenter(((int)Math.floor(this.getCenterPosX() / 24) * 24) + 12, ((int)Math.floor(this.getCenterPosY() / 24) * 24) + 12);
		}
		
		return;
	    }
	    
	    // Check cell boundaries
	    if (-moveX * (targetCellXCenter - nextX) >= 1) {
		this.setCenterX(targetCellXCenter);
	    }
	    
	    if (-moveY * (targetCellYCenter - nextY) >= 1) {
		this.setCenterY(targetCellYCenter);
	    }
	    
	    if (isTargetCellReached()) {
		//System.out.println("Reached");
		this.setPositionByCenter(targetCellXCenter, targetCellYCenter); // correct position to center
		
		if (this.isMovingByPath) {
		    switchToNextWaypointOrFinish();
		}
	    } else {
		this.setPositionByCenter(nextX, nextY);
	    }
	}
	
	protected void drawPath(Graphics g) {
	    if (!Main.DEBUG_MODE) {
		return;
	    }
	    
	    if (this.currentPath != null) {
		g.setColor(Color.green);
		g.setLineWidth(1);
		g.drawLine(this.getCenterPosX(), this.getCenterPosY(), this.currentPath.getStep(this.pathIndex).getX() * 24 + 12, this.currentPath.getStep(this.pathIndex).getY() * 24 + 12);
		g.fillOval(this.goalX * 24 + 12 - 2, this.goalY * 24 + 12 - 2, 5, 5);
		
		for (int i = this.pathIndex; i < this.currentPath.getLength() - 1; i++) {
		    Step from = this.currentPath.getStep(i);
		    Step to = this.currentPath.getStep(i + 1);
		    
		    g.fillOval(from.getX() * 24 + 12 - 2, from.getY() * 24 + 12 - 2, 5, 5);
		    g.fillOval(to.getX() * 24 + 12 - 2, to.getY() * 24 + 12 - 2, 5, 5);
		    
		    g.drawLine(from.getX() * 24 + 12, from.getY() * 24 + 12, to.getX() * 24 + 12, to.getY() * 24 + 12);
		}
		
		g.setColor(Color.orange);
		g.fillOval(this.targetCellX * 24 + 12, this.targetCellY * 24 + 12, 5, 5);		
	    }
	    
	    //g.setColor(Color.gray); 
	//	for (int i = (int) (posX / 24 - 5); i < posX / 24 + 5; i++) {
		//    for (int j = (int) (posY / 24 - 5); j < posY / 24 + 5; j++) {
		//	g.drawRect(i * 24, j * 24, 24, 24);
		//    }
		//}
	}
	
	public void finishMoving() {
	    // Set up unit in cell center
	    setPositionByCenter(((int)Math.floor(this.getCenterPosX() / 24) * 24) + 12, ((int)Math.floor(this.getCenterPosY() / 24) * 24) + 12);
	    
	    this.moveX = 0;
	    this.moveY = 0;
	    
	    this.goalX = 0;
	    this.goalY = 0;
	    
	    this.isMovingToCell = false;
	    this.isMovingByPath = false;
	    this.pathIndex = 0;
	    this.currentPath = null;
	}
	
	public float getCenterPosX() {
	    return this.getTextureX() + (this.sizeWidth / 2);
	}
	
	public float getCenterPosY() {
	    return this.getTextureY() + (this.sizeHeight / 2);
	}	
	
	public void setPositionByCenter(float x, float y) {
	    setCenterX(x);
	    setCenterY(y);
	}
	
	private void setCenterX(float x) {
	    this.posX = x - (this.sizeWidth / 2) + 6;
	}
	
	private void setCenterY(float y) {
	    this.posY = y - (this.sizeHeight / 2) + 12;	    
	}
	
	@Override
	public abstract void updateEntity(int delta);

	@Override
	public abstract void renderEntity(Graphics g);
	
	public abstract float getMoveSpeed();
	
	public enum RotationDirection { LEFT, RIGHT } // +1 or -1 to facing value
	
	public abstract float getTextureX();
	public abstract float getTextureY();
	
	public static EntityVehicle newInstance(EntityVehicle b) {
		Constructor ctor;
		try {
		    ctor = (b.getClass()).getDeclaredConstructor(Float.class, Float.class, Team.class, Player.class);
		    ctor.setAccessible(true);
		    EntityVehicle newEntityVehicle = ((EntityVehicle)ctor.newInstance(b.posX, b.posY, b.team, b.owner));
		    
		    return newEntityVehicle;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
			| IllegalArgumentException | InvocationTargetException e) {
		    e.printStackTrace();
		}

		return null;
	}

	public int getBuildingSpeed() {
	    return this.buildingSpeed;
	}	
}
