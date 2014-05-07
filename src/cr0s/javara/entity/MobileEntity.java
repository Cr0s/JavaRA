package cr0s.javara.entity;

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
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.util.RotationUtil;

public abstract class MobileEntity extends EntityActor implements Mover, IMovable {

    public int goalX, goalY;
    public int startX, startY;
    public Path currentPath = null;
    public int pathIndex = 0;
    public boolean isMovingByPath;

    protected float moveSpeed = 0.1f;

    private static final int REPATH_RANGE = 3;    
   
    public int targetCellX, targetCellY;
    public boolean isMovingToCell;    
    
    public MobileEntity(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
    }

    public boolean findPathAndMoveTo(int aGoalX, int aGoalY) {
	Path path = findPathFromTo(this, aGoalX, aGoalY);
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

    public abstract Path findPathFromTo(MobileEntity e, int aGoalX, int aGoalY);
    
    public void startMovingByPath(Path p) {
	this.currentPath = p;

	this.isMovingByPath = true;

	this.goalX = p.getX(p.getLength() - 1);
	this.goalY = p.getY(p.getLength() - 1);

	//System.out.println("Generating path, moving from " + this.startX * 24 + "; " + this.startY * 24 + " to " + (int) this.goalX * 24 + "; " + (int) this.goalY * 24);

	queueActivity(new Move(this, new Point(goalX, goalY), getMinimumEnoughRange()));
    }

    public void moveToAdjacentTile(int tileX, int tileY) {
	//System.out.println("Moving to adjacent tile from " + (int) this.getCenterPosX() + "; " + (int) this.getCenterPosY() + " to " + tileX * 24 + "; " + tileY * 24);
	this.isMovingToCell = true;

	this.moveX = (tileX - (int) this.getCenterPosX() / 24);
	this.moveY = (tileY - (int) this.getCenterPosY() / 24);
	
	this.targetCellX = tileX;
	this.targetCellY = tileY;

	Point targetPoint = new Point(this.targetCellX * 24, this.targetCellY * 24);
	
	int rot = RotationUtil.getRotationFromXY(0, 0, moveX, moveY);
	queueActivity(new Turn(this, rot, (int) (50 * this.moveSpeed)));
	queueActivity(new Drag(getPos(), targetPoint, (int) (1000 * this.getMoveSpeed())));	
    }

    public Point getCenterPos() {
	return new Point(this.getCenterPosX(), this.getCenterPosY());
    }
    
    public Point getPos() {
	return new Point(this.posX, this.posY);
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
    
    public abstract float getMoveSpeed();    
    public abstract float getTextureX();
    public abstract float getTextureY();    
    public abstract int getMinimumEnoughRange();
}
