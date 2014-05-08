package cr0s.javara.entity.actor.activity.activities;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Turn.RotationDirection;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.util.InterpolatePos;
import cr0s.javara.util.RotationUtil;

public class Move extends Activity {

    private Point destCell;
    private int destRange;
    private EntityBuilding ignoreBuilding;
    
    public Path currentPath;
    public int currentPathIndex;
    
    private boolean hasNotifiedBlocker, hasWaited;
    private int waitTicksRemaining;
    
    public Move(MobileEntity me, Point destinationCell) {
	this.destCell = destinationCell;
	
	this.currentPath = me.findPathFromTo(me, (int) (destinationCell.getX()), (int) (destinationCell.getY()));
	this.currentPathIndex = 1;
    }
    
    public Move(MobileEntity me, Point destinationCell, int enoughRange) {
	this(me, destinationCell);
	
	this.destRange = enoughRange;
    }
    
    public Move(MobileEntity me, Point destinationCell, int enoughRange, EntityBuilding aIgnoreBuilding) {
	this(me, destinationCell, enoughRange);
	
	this.ignoreBuilding = aIgnoreBuilding;
    }
    
    public Move(MobileEntity me, Path scriptedPath, Point destinationCell, EntityBuilding aIgnoreBuilding) {
	this(me, destinationCell, 0);
	
	this.currentPath = scriptedPath;
	this.ignoreBuilding = aIgnoreBuilding;
    }
    
    private Point popPath(MobileEntity me) {
	int px = 0, py = 0;
	
	if (this.currentPath == null || currentPathIndex >= this.currentPath.getLength() || this.currentPath.getLength() < 1) {
	    this.currentPath = null;
	    return null;
	}
	
	Step s = currentPath.getStep(currentPathIndex);
	px = s.getX();
	py = s.getY();
	
	Point nextCell = new Point(px, py);
	
	/*if (me instanceof EntityMcv) {
        	System.out.println("Current cell: " + px + "; " + py + " | pathIndex: " + (currentPathIndex - 1));
        	System.out.println("\tcanEnterCell: " + me.canEnterCell(nextCell));
        	System.out.println("\tblockedByEntitiy: " +  me.world.isCellBlockedByEntity(nextCell));
        	System.out.println("\tblockedByEntitiy: " +  me.world.getEntityInPoint(nextCell.getX() * 24, nextCell.getY() * 24));
	}*/
	
	if (!me.canEnterCell(nextCell) && me.world.isCellBlockedByEntity(nextCell)) {
	    if (this.ignoreBuilding != null && me.world.getBuildingInCell(nextCell) != this.ignoreBuilding) {
		this.hasNotifiedBlocker = false;
		this.hasWaited = false;
		
		return nextCell;		
	    }
	    
	    // See if we close enough
	    /*float dx = destCell.getX() - nextCell.getX();
	    float dy = destCell.getY() - nextCell.getY();
	    System.out.println("Next cell in path is blocked by: " + me.world.getMobileEntityInCell(nextCell) + " | Range: " + Math.sqrt(dx * dx + dy * dy));
	    
	    if (dx * dx + dy * dy <= this.destRange * this.destRange) {
		this.currentPathIndex = this.currentPath.getLength(); // stop and skip all path
		this.currentPath = null;
		
		return null;
	    }*/
	    

	    MobileEntity blocker = me.world.getMobileEntityInCell(nextCell);
	    
	    // Notify blocker 
	    if (blocker != null && !hasNotifiedBlocker) {
		blocker.notifyBlocking(me);
		this.hasNotifiedBlocker = true;
	    }
	    
	    // Wait a bit
	    if (!this.hasWaited) {
		this.waitTicksRemaining = me.getWaitAverageTime() + me.world.getRandomInt(-me.getWaitSpreadTime(), me.getWaitSpreadTime());
		
		System.out.println("Waiting time: " + this.waitTicksRemaining);
		this.hasWaited = true;
	    }
	    
	    if (--this.waitTicksRemaining >= 0) { // We're waiting now
		System.out.println("\tWaiting ticks: " + this.waitTicksRemaining);
		return null;
	    }
	    
	    // We're totally blocked, try to calculate new path
	    System.out.println("Recalculating path: " + (me == null) + "; " + (this.destCell == null));
	    this.currentPath = me.findPathFromTo(me, (int) (this.destCell.getX()), (int) (destCell.getY()));
	    this.currentPathIndex = 1;
	    
	    return null;
	}
	
	this.currentPathIndex++;
	this.hasNotifiedBlocker = false;
	this.hasWaited = false;
	
	return nextCell;
    }
    
    @Override
    public Activity tick(EntityActor a) {
	Point nextCell = null;
	
	if (!(a instanceof MobileEntity)) {
	    return nextActivity;
	}
	
	MobileEntity me = (MobileEntity) a;
	
	if (currentPath == null) {
	    return nextActivity;
	}
	
	nextCell = popPath(me);
	
	if (nextCell == null) {
	    return this;
	}
	
	me.isMovingToCell = true;
	me.targetCellX = (int) nextCell.getX();
	me.targetCellY = (int) nextCell.getY();
	
	/*
	 * 1. Turn to required facing
	 * 2. Drag to required cell
	 * 3. Continue moving
	 * 
	 *         Movement system:
	 *         
	 *                     +-----<------+
	 *                     |            |
	 *                     v            ^
	 * Activity queue: MovePart --+     | (change facing and drag to target cell)
	 *                            |     ^
	 *                            v     |
	 *                           Move ->+ (if we have next cell to move)
	 */
	
	return new MovePart(this, me, me.getPos(), nextCell);
    }
      
    public void setIgnoreBuilding(EntityBuilding eb) {
	this.ignoreBuilding = eb;
    }
    
    public class MovePart extends Activity {

	public Move parentMove;
	private MobileEntity me;
	private Point start;
	private Point end;
	
	private int lengthInTicks;
	private int ticks;
	
	private int desiredFacing, startFacing;
	private RotationDirection rotationDirection;
	
	public MovePart(Move aParentMove, MobileEntity aMe, Point aStart, Point aDestCell) {
	    this.parentMove = aParentMove;
	    
	    this.me = aMe;
	    this.me.targetCellX = (int) aDestCell.getX();
	    this.me.targetCellY = (int) aDestCell.getY();
	    
	    this.end = new Point(aDestCell.getX() * 24, aDestCell.getY() * 24);
	    this.start = aStart;
	    
	    this.lengthInTicks = (int) (10 - (10 * me.getMoveSpeed()));
	    
	    this.desiredFacing = RotationUtil.getRotationFromXY(start.getX(), start.getY(), end.getX(), end.getY()) % Turn.MAX_FACING;
	    this.startFacing = me.currentFacing;
	    
	    if (me.currentFacing >= 24 && desiredFacing <= 8) {
		this.rotationDirection = RotationDirection.LEFT;
	    } else if (me.currentFacing <= 8 && desiredFacing >= 24) {
		this.rotationDirection = RotationDirection.RIGHT;
	    } else {
		if (me.currentFacing < desiredFacing) {
		    this.rotationDirection = RotationDirection.LEFT;
		} else if (me.currentFacing > desiredFacing){
		    this.rotationDirection = RotationDirection.RIGHT;
		}
	    }	    
	}
	
	@Override
	public Activity tick(EntityActor a) {
	    Point nextPos;
	    
	    if (lengthInTicks > 1) { 
		nextPos = InterpolatePos.interpolatePos(start, end, ticks, lengthInTicks - 1);
	    } else {
		nextPos = end;
	    }

	    me.setPos(nextPos);	

	    if (me.currentFacing != this.desiredFacing) {
		turnFacing();
	    
		return this;
	    } else {

		ticks++;
		// If move is finished, return control to parent activity
		if ((me.getPos().getX() == end.getX() && me.getPos().getY() == end.getY()) || ticks >= lengthInTicks) {
		    me.currentFacing = this.desiredFacing % Turn.MAX_FACING; // how rough!
		    me.isMovingToCell = false;

		    // Parent Move activity is cancelled, lets switch to next activity (user send move order when Move/PartMove activity is working)
		    if (this.nextActivity instanceof Move || parentMove.isCancelled()) {
			return this.nextActivity;
		    } else {
			return parentMove;
		    }
		} else {
		    return this;
		}
	    }
	}
	
	private void turnFacing() {
	    int newFacing = me.currentFacing;
	    if (this.rotationDirection == RotationDirection.LEFT) {
		newFacing = (me.currentFacing + 1) % Turn.MAX_FACING;
	    } else if (this.rotationDirection == RotationDirection.RIGHT) {
		newFacing = (me.currentFacing - 1) % Turn.MAX_FACING;
	    }
	    
	    // Turn by circle
	    if (newFacing < 0) {
		newFacing = Turn.MAX_FACING - 1;
	    }
	    
	    me.currentFacing = newFacing % Turn.MAX_FACING;
	}
    }
}
