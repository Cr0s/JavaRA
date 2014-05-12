package cr0s.javara.entity.infantry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

import org.newdawn.slick.BigImage;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.actor.activity.activities.MoveInfantry;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.render.EntityBlockingMap.FillsSpace;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.render.Sequence;
import cr0s.javara.resources.ShpTexture;

public abstract class EntityInfantry extends MobileEntity implements IShroudRevealer {

    private static final float DEFAULT_MOVE_SPEED = 0.01f;
    
    private static final int WIDTH = 50;
    private static final int HEIGHT = 39;

    public static final int MAX_FACING = 8;
    
    public static Point subcellOffsets[] = new Point[6];
    
    static {
	subcellOffsets[SubCell.TOP_LEFT.ordinal()] = new Point(-21, -12);
	subcellOffsets[SubCell.TOP_RIGHT.ordinal()] = new Point(-5, -12);
	
	subcellOffsets[SubCell.CENTER.ordinal()] = new Point(-13, -6);
	
	subcellOffsets[SubCell.BOTTOM_LEFT.ordinal()] = new Point(-21, 2);
	subcellOffsets[SubCell.BOTTOM_RIGHT.ordinal()] = new Point(-5, 2);
    }
    
    protected ShpTexture texture;
    protected int currentFrame;
    
    protected Sequence currentSequence;
    
    public EntityInfantry(float posX, float posY, Team team, Player owner, SubCell sub) {
	super(posX, posY, team, owner, WIDTH, HEIGHT);
	
	this.currentSubcell = sub;
	
	this.posX += subcellOffsets[sub.ordinal()].getX();
	this.posY += subcellOffsets[sub.ordinal()].getY();
	
	this.fillsSpace = FillsSpace.ONE_SUBCELL;
    }
    
    @Override
    public Path findPathFromTo(MobileEntity e, int aGoalX, int aGoalY) {
	return world.getInfantryPathfinder().findPathFromTo((EntityInfantry) e, aGoalX, aGoalY);
    }

    public static EntityInfantry newInstance(EntityInfantry b) {
	Constructor ctor;
	try {
	    ctor = (b.getClass()).getDeclaredConstructor(Float.class, Float.class, Team.class, Player.class);
	    ctor.setAccessible(true);
	    EntityInfantry newEntityInfantry = ((EntityInfantry) ctor.newInstance(b.posX, b.posY, b.team, b.owner));

	    return newEntityInfantry;
	} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
		| IllegalArgumentException | InvocationTargetException e) {
	    e.printStackTrace();
	}

	return null;
    }
   
    @Override
    public boolean canEnterCell(Point cellPos) {
	return world.isCellPassable(cellPos, (this.desiredSubcell == null) ? this.currentSubcell : this.desiredSubcell);
    }
    
    @Override
    public float getMoveSpeed() {
	return DEFAULT_MOVE_SPEED;
    }    
    
    @Override
    public float getTextureX() {
	return this.posX;
    }

    @Override
    public float getTextureY() {
	return this.posY;
    } 
    
    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	
	if (this.currentSequence != null) { 
	    this.currentSequence.update(this.currentFacing);
	}
	
	this.boundingBox.setBounds(this.posX + this.texture.width / 3 + 2, this.posY + this.texture.height / 2 - 15, 13, 18);
    }
    
    @Override
    public void renderEntity(Graphics g) {
	//if (this.sheet != null) {
	    this.currentSequence.render(this.posX, this.posY);
	//}
    }
    
    @Override
    public void moveTo(Point destCell, EntityBuilding ignoreBuilding) {
	this.goalX = (int) destCell.getX();
	this.goalY = (int) destCell.getY();

	MoveInfantry move = new MoveInfantry(this, destCell, getMinimumEnoughRange(), ignoreBuilding);
	
	// If we already moving
	if (this.currentActivity instanceof MoveInfantry) {
	    this.currentActivity.cancel();
	} else if (this.currentActivity instanceof MoveInfantry.MovePart) {
	    this.currentActivity.queueActivity(move);
	    return;
	}
	
	queueActivity(move);
    }
    
    @Override
    public void startMovingByPath(Path p, EntityBuilding ignoreBuilding) {
	this.goalX = (int) p.getX(p.getLength() - 1);
	this.goalY = (int) p.getY(p.getLength() - 1);
	
	queueActivity(new MoveInfantry(this, p, new Point(goalX, goalY), ignoreBuilding));
    }     
    
    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }
    
    @Override
    public float getCenterPosX() {
	return this.boundingBox.getCenterX();
    }

    @Override
    public float getCenterPosY() {
	return this.boundingBox.getCenterY();
    }	
}
