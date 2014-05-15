package cr0s.javara.entity.actor.activity.activities;

import java.util.ArrayList;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Turn.RotationDirection;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.entity.infantry.EntityInfantry.AnimationState;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.render.World;
import cr0s.javara.util.PointsUtil;
import cr0s.javara.util.RotationUtil;

public class MoveInfantry extends Activity {

    private Point destCell;
    private int destRange;
    private EntityBuilding ignoreBuilding;

    public Path currentPath;
    public int currentPathIndex;

    private boolean hasNotifiedBlocker, hasWaited;
    private int waitTicksRemaining;

    private final static int REPATHING_INTERVAL_TICKS = 35;
    private int ticksBeforeRepath = REPATHING_INTERVAL_TICKS;

    private boolean isNewPath;
    private int randomWaitTicks;

    public MoveInfantry(MobileEntity me, Point destinationCell) {
	this.destCell = destinationCell;

	this.randomWaitTicks = me.world.getRandomInt(1, 3);

	chooseNewPath(me);
    }

    public MoveInfantry(MobileEntity me, Point destinationCell, int enoughRange) {
	this(me, destinationCell);

	this.destRange = enoughRange;
    }

    public MoveInfantry(MobileEntity me, Point destinationCell, int enoughRange, EntityBuilding aIgnoreBuilding) {
	this(me, destinationCell, enoughRange);

	this.ignoreBuilding = aIgnoreBuilding;
    }

    public MoveInfantry(MobileEntity me, Path scriptedPath, Point destinationCell, EntityBuilding aIgnoreBuilding) {
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


	me.desiredSubcell = me.currentSubcell;
	if (!me.world.blockingEntityMap.isSubcellFree(nextCell, me.desiredSubcell)) {
	    me.desiredSubcell = me.world.blockingEntityMap.getFreeSubCell(nextCell, me.currentSubcell);

	    if (me.desiredSubcell == null) {
		me.desiredSubcell = me.currentSubcell;
	    }	    
	}	

	if (!me.canEnterCell(nextCell)) {
	    // This building we ignore
	    if (this.ignoreBuilding != null && me.world.getBuildingInCell(nextCell) == this.ignoreBuilding) {
		this.hasNotifiedBlocker = false;
		this.hasWaited = false;

		return null;		
	    }

	    // See if we close enough
	    float dx = destCell.getX() - nextCell.getX();
	    float dy = destCell.getY() - nextCell.getY();

	    if (dx * dx + dy * dy <= this.destRange * this.destRange) {
		//System.out.println("We're close enough. Range: " + Math.sqrt(dx * dx + dy * dy) + " <= " + this.destRange);
		this.currentPathIndex = this.currentPath.getLength(); // stop and skip all path
		this.currentPath = null;

		return null;
	    }


	    MobileEntity blocker = me.world.getMobileEntityInCell(nextCell);

	    // Notify blocker 
	    if (blocker != null && !hasNotifiedBlocker) {
		blocker.notifyBlocking(me);
		this.hasNotifiedBlocker = true;
	    }
	    
	    // Wait a bit
	    if (!this.hasWaited) {
		this.waitTicksRemaining = me.getWaitAverageTime() + me.world.getRandomInt(-me.getWaitSpreadTime(), me.getWaitSpreadTime());

		//System.out.println("Waiting time: " + this.waitTicksRemaining);
		this.hasWaited = true;
		((EntityInfantry) me).setCurrentAnimationState(AnimationState.WAITING);
	    }

	    if (--this.waitTicksRemaining >= 0) { // We're waiting now
		return null;
	    }

	    // We're totally blocked, try to calculate new path
	    chooseNewPath(me);

	    return null;
	}
	
	((EntityInfantry) me).setCurrentAnimationState(AnimationState.MOVING);

	if (--this.ticksBeforeRepath <= 0) {
	    this.ticksBeforeRepath = this.REPATHING_INTERVAL_TICKS;

	    chooseNewPath(me);
	}

	this.currentPathIndex++;
	this.hasNotifiedBlocker = false;
	this.hasWaited = false;

	return nextCell;
    }

    private void chooseNewPath(MobileEntity me) {
	this.currentPath = me.findPathFromTo(me, (int) (this.destCell.getX()), (int) (destCell.getY()));
	this.currentPathIndex = 1;

	this.isNewPath = true;

	// It seems destination cell are blocked, try to choose nearest free cell as new destination cell
	if (this.currentPath == null) {
	    Point newDestCell = chooseClosestToDestCell(me);

	    // Give up
	    if (newDestCell == null) {
		this.isNewPath = false;
		return;
	    }

	    this.destCell = newDestCell;
	    this.currentPath = me.findPathFromTo(me, (int) (this.destCell.getX()), (int) (destCell.getY()));

	    this.isNewPath = true;
	}
    }

    public Point chooseClosestToDestCell(MobileEntity me) {
	Point res = this.destCell;

	// Find free cells in range, starting from closest range
	for (int range = 1; range <= this.destRange; range++) {
	    ArrayList<Point> cells = me.world.choosePassableCellsInCircle(destCell, range);

	    if (cells.size() != 0) {
		return cells.get(me.world.getRandomInt(0, cells.size()));
	    }
	}

	return res;
    }    

    @Override
    public Activity tick(EntityActor a) {
	Point nextCell = null;

	if (isCancelled || !(a instanceof MobileEntity)) {
	    return nextActivity;
	}

	MobileEntity me = (MobileEntity) a;

	if (currentPath == null) {
	    return nextActivity;
	}

	if (randomWaitTicks > 0) {
	    randomWaitTicks--;
	    return this;
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

	MovePart movePartActivity = new MovePart(this, me, me.getPos(), nextCell);
	if (this.isNewPath) {
	    movePartActivity.setIsNewPath(true);

	    this.isNewPath = false;
	}

	movePartActivity.tick(me); // Tick once to avoid tick skipping without movement when current activiy is Move

	return movePartActivity;
    }

    public void setIgnoreBuilding(EntityBuilding eb) {
	this.ignoreBuilding = eb;
    }

    public class MovePart extends Activity {

	public MoveInfantry parentMove;
	private MobileEntity me;
	private Point start;
	private Point end;

	private int lengthInTicks;
	private int ticks;

	private int desiredFacing, startFacing;
	private RotationDirection rotationDirection;

	private boolean isNewPath;

	public MovePart(MoveInfantry aParentMove, MobileEntity aMe, Point aStart, Point aDestCell) {
	    this.parentMove = aParentMove;

	    this.me = aMe;
	    this.me.targetCellX = (int) aDestCell.getX();
	    this.me.targetCellY = (int) aDestCell.getY();

	    int subOffsetXnext = (int) EntityInfantry.subcellOffsets[this.me.desiredSubcell.ordinal()].getX();
	    int subOffsetYnext = (int) EntityInfantry.subcellOffsets[this.me.desiredSubcell.ordinal()].getY();
	    
	    int subOffsetXcurr = (int) EntityInfantry.subcellOffsets[this.me.currentSubcell.ordinal()].getX();
	    int subOffsetYcurr = (int) EntityInfantry.subcellOffsets[this.me.currentSubcell.ordinal()].getY();
	    
	    this.end = new Point((aDestCell.getX() * 24) + subOffsetXnext, (aDestCell.getY() * 24) + subOffsetYnext);
	    this.start = aStart;

	    this.lengthInTicks = (int) (40 - (10 * me.getMoveSpeed()));

	    int facing = RotationUtil.getRotationFromXY(start.getX(), start.getY(), end.getX(), end.getY()) % Turn.MAX_FACING;
	    this.desiredFacing = RotationUtil.quantizeFacings(facing, EntityInfantry.MAX_FACING);
	}

	@Override
	public Activity tick(EntityActor a) {
	    Point nextPos;

	    if (lengthInTicks > 1) { 
		nextPos = PointsUtil.interpolatePos(start, end, ticks, lengthInTicks - 1);
	    } else {
		nextPos = end;
	    }

	    if (me.currentFacing != this.desiredFacing) {
		me.currentFacing = this.desiredFacing % EntityInfantry.MAX_FACING;

		// Don't move while our turn is not finished for new direction
		if (isNewPath) { 
		    return this;
		}
	    } else {
		this.isNewPath = false;
	    }

	    me.setPos(nextPos);		    

	    ticks++;
	    // If move is finished, return control to parent activity
	    if ((me.getPos().getX() == end.getX() && me.getPos().getY() == end.getY()) || ticks >= lengthInTicks) {
		me.currentFacing = this.desiredFacing % EntityInfantry.MAX_FACING;
		me.currentSubcell = me.desiredSubcell;
		me.finishMoving();

		// Parent Move activity is cancelled, lets switch to next activity (user send move order when Move/PartMove activity is working)
		if (this.nextActivity instanceof MoveInfantry || parentMove.isCancelled()) {
		    return this.nextActivity;
		} else {
		    return parentMove;
		}
	    } else {
		return this;
	    }
	}

	public void setIsNewPath(boolean aIsNewPath) {
	    this.isNewPath = aIsNewPath;
	}
    }
}
