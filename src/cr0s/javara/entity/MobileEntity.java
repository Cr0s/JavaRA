package cr0s.javara.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.activities.Drag;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.order.IOrderIssuer;
import cr0s.javara.order.IOrderResolver;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.MoveOrderTargeter;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.util.RotationUtil;

public abstract class MobileEntity extends EntityActor implements Mover, IMovable, INotifyBlockingMove {
    protected float moveSpeed = 0.1f;
   
    public int targetCellX, targetCellY;
    public boolean isMovingToCell;   
    
    public int goalX, goalY;
    
    public MobileEntity(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
	
	ordersList = new ArrayList<>();
	ordersList.add(new MoveOrderTargeter(this));
    }


    public abstract Path findPathFromTo(MobileEntity e, int aGoalX, int aGoalY);

    public Point getCenterPos() {
	return new Point(this.getCenterPosX(), this.getCenterPosY());
    }
    
    public Point getPos() {
	return new Point(this.posX, this.posY);
    }
    
    public Point getCellPos() {
	return new Point((int) getCenterPosX() / 24, (int) getCenterPosY() / 24);
    }    
    
    public Point getTexturePos() {
	return new Point(this.getTextureX(), this.getTextureY());
    }

    public void setPos(Point pos) {
	this.posX = pos.getX();
	this.posY = pos.getY();
    }
    
    public void setCenterPos(Point pos) {
	this.setCenterX(pos.getX());
	this.setCenterY(pos.getY());
    }
    
    protected void drawPath(Graphics g) {
	if (!Main.DEBUG_MODE) {
	    return;
	}

	if (this.currentActivity != null) {
	    Path currentPath = null;
	    int pathIndex = 0;
	    
	    if ((this.currentActivity instanceof Move) && ((Move) this.currentActivity).currentPath != null) {
		currentPath = ((Move) this.currentActivity).currentPath;
		pathIndex = ((Move) currentActivity).currentPathIndex;
	    } else if ((this.currentActivity instanceof Move.MovePart) && ((Move.MovePart) this.currentActivity).parentMove.currentPath != null) {
		currentPath = ((Move.MovePart) this.currentActivity).parentMove.currentPath;
		pathIndex = ((Move.MovePart) this.currentActivity).parentMove.currentPathIndex;	
	    }
	    
	    if (currentPath == null) {
		return;
	    }
	    
	    g.setColor(Color.green);
	    g.setLineWidth(1);
	    
	    if (pathIndex == currentPath.getLength()) {
		return;
	    }
	    
	    g.drawLine(this.getCenterPosX(), this.getCenterPosY(), currentPath.getStep(pathIndex - 1).getX() * 24 + 12, currentPath.getStep(pathIndex - 1).getY() * 24 + 12);
	    
	    g.fillOval(this.goalX * 24 + 12 - 2, this.goalY * 24 + 12 - 2, 5, 5);

	    for (int i = pathIndex - 1; i < currentPath.getLength() - 1; i++) {
		Step from = currentPath.getStep(i);
		Step to = currentPath.getStep(i + 1);

		g.fillOval(from.getX() * 24 + 12 - 2, from.getY() * 24 + 12 - 2, 5, 5);
		g.fillOval(to.getX() * 24 + 12 - 2, to.getY() * 24 + 12 - 2, 5, 5);

		g.drawLine(from.getX() * 24 + 12, from.getY() * 24 + 12, to.getX() * 24 + 12, to.getY() * 24 + 12);
	    }

	    //g.setColor(Color.orange);
	    //g.fillOval(this.targetCellX * 24 + 12, this.targetCellY * 24 + 12, 5, 5);		
	}

	// Draw grid
	/*final int GRID_SIZE = 3;
	g.setColor(Color.gray); 
	for (int i = (int) (posX / 24 - GRID_SIZE); i < posX / 24 + GRID_SIZE; i++) {
	    for (int j = (int) (posY / 24 - GRID_SIZE); j < posY / 24 + GRID_SIZE; j++) {
		g.drawRect(i * 24, j * 24, 24, 24);
	    }
	}*/
    }

    public void finishMoving() {
	this.moveX = 0;
	this.moveY = 0;

	this.isMovingToCell = false;
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
    
    public void nudge(MobileEntity nudger, boolean force) {
	nudge(nudger, force, 1);
    }
    
    public void nudge(MobileEntity nudger, boolean force, int nudgingDepth) {
	// Don't allow non-forced nudges if we doing something
	if (!force && !this.isIdle()) {
	    return;
	}
	
	// All possible adjacent cells directions
	int dx[] = { +1, -1,  0,  0, +1, -1, +1, -1 };
	int dy[] = {  0,  0, +1, -1, +1, +1, -1, -1 };
	
	Point nudgerPos = null;
	if (nudger != null) {
	    nudgerPos = nudger.getCellPos();
	}
	
	ArrayList<Point> availCells = new ArrayList<>();
	ArrayList<Point> smartCells = new ArrayList<>(); // smart cells is cells which blocked, but we can try to nudge actor inside it
	
	for (int i = 0; i < 8; i++) {
	    int newCellX = (int) this.getCellPos().getX() + dx[i];
	    int newCellY = (int) this.getCellPos().getY() + dy[i];
	    
	    // Skip cell with nudger position
	    if (nudger != null && (nudgerPos.getX() == newCellX && nudgerPos.getY() == newCellY)) {
		continue;
	    }
	    
	    if (world.isCellPassable(newCellX, newCellY)) {
		availCells.add(new Point(newCellX, newCellY));
	    } else {
		smartCells.add(new Point(newCellX, newCellY));
	    }
	}
	
	Point pointToGetOut = null; // target point to stand down
	
	// Choose random cell, first from of available, if not, then, select from smart cells
	if (availCells.size() != 0) {
	    pointToGetOut = availCells.get(world.getRandomInt(0, availCells.size()));
	} else {
	    if (smartCells.size() != 0) {
		pointToGetOut = smartCells.get(world.getRandomInt(0, smartCells.size()));
	    }
	}
	
	if (pointToGetOut != null && availCells.size() != 0) {
	    this.cancelActivity();
	    
	    this.queueActivity(new Move(this, pointToGetOut));
	} else { 
	    // All cells seems to be blocked, lets try to nudge someone around
	    // Check depth to avoid stack overflow if we fall into recursion
	    if (nudgingDepth > 3) {
		return;
	    }

	    Collections.shuffle(smartCells);
	    
	    for (Point cell : smartCells) {
		MobileEntity blocker = world.getMobileEntityInCell(cell);
		
		if (blocker != this && blocker != null && blocker.isFrendlyTo(this)) {
		    blocker.nudge(this, force, nudgingDepth + 1);
		}
	    }
	}
    }
    
    public void moveTo(Point destCell) {
	this.moveTo(destCell, null);
    }
    
    public void moveTo(Point destCell, EntityBuilding ignoreBuilding) {
	this.goalX = (int) destCell.getX();
	this.goalY = (int) destCell.getY();

	Move move = new Move(this, destCell, getMinimumEnoughRange(), ignoreBuilding);
	
	// If we already moving
	if (this.currentActivity instanceof Move) {
	    this.currentActivity.cancel();
	} else if (this.currentActivity instanceof Move.MovePart) {
	    this.currentActivity.queueActivity(move);
	    return;
	}
	
	queueActivity(move);
    }
    
    public void startMovingByPath(Path p, EntityBuilding ignoreBuilding) {
	this.goalX = (int) p.getX(p.getLength() - 1);
	this.goalY = (int) p.getY(p.getLength() - 1);
	
	queueActivity(new Move(this, p, new Point(goalX, goalY), ignoreBuilding));
    }    
    
    @Override
    public void notifyBlocking(MobileEntity from) {
	if (this.isIdle() && from.isFrendlyTo(this)) {
	    this.nudge(from, true); // we being nudged by from 
	}
    }    
    
    public abstract float getMoveSpeed();    
    public abstract float getTextureX();
    public abstract float getTextureY();    
    public abstract int getMinimumEnoughRange();
    public abstract boolean canEnterCell(Point cellPos);
    
    public abstract int getWaitAverageTime();
    public abstract int getWaitSpreadTime();
    
    // Orders section
    @Override
    public ArrayList<OrderTargeter> getOrders() {
	return this.ordersList;
    }
    
    @Override
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia) {
	if (targeter.orderString.equals("Move") && ia.mouseButton == 1) {
	    if (!target.isCellTarget()) {
		return null;
	    }
	    
	    return new Order("Move", null, target.getTargetCell());
	}
	
	return null;
    }
    
    @Override
    public void resolveOrder(Order order) {
	if (order.orderString.equals("Move") && order.targetPosition != null) {
	    this.moveTo(order.targetPosition);
	}
    }
}
