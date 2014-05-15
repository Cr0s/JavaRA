package cr0s.javara.entity.vehicle.tank;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.activity.activities.Drag;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.actor.activity.activities.Turn.RotationDirection;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.RotationUtil;

public class EntityHeavyTank extends EntityVehicle implements ISelectable, Mover, IHaveCost {

    private String TEXTURE_NAME = "3tnk.shp";
    private SpriteSheet texture;

    private final int ROTATION_START_TEXTURE_INDEX = 0;
    private final int ROTATION_END_TEXTURE_INDEX = 31;

    private final int MAX_ROTATION = 32;	

    private static final int TEXTURE_WIDTH = 36;
    private static final int TEXTURE_HEIGHT = 36;
    private static final int SHROUD_REVEALING_RANGE = 8;
    private static final int WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS = 15;
    private static final int WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS = 5;

    private int updateTicks = 0;

    private int turretRotation = 0;
    private RotationDirection turretRotationDirection;
    private boolean isTurretRotatingNow = false;
    private int newTurretRotation = 0;

    private Entity targetEntity = null;

    private final float MOVE_SPEED = 0.3f;

    private final float SHIFT = 12;
    
    private final int BUILDING_COST = 1150;

    public EntityHeavyTank(Float posX, Float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.isVisible = true;

	this.setHp(20);
	this.setMaxHp(20);
	
	this.buildingSpeed = 99;
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	
	boundingBox.setBounds(posX + (TEXTURE_WIDTH / 4) - 6, posY + (TEXTURE_WIDTH / 4) - 12, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	doTurretRotationTick();
    }

    @Override
    public void renderEntity(Graphics g) {
	super.renderEntity(g);
	
	if (Main.DEBUG_MODE) {
	    g.setLineWidth(1);
	    g.setColor(owner.playerColor);
	    g.draw(boundingBox);
	    //g.drawOval(posX - 1, posY - 1, this.boundingBox.getWidth() + 1, this.boundingBox.getHeight() + 1);
	}

	//g.drawRect(this.getTextureX(), this.getTextureY(), TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture.startUse();
	texture.getSubImage(0, currentFacing).drawEmbedded(this.getTextureX(), this.getTextureY(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	texture.getSubImage(0, 32 + turretRotation).drawEmbedded(this.getTextureX(), this.getTextureY(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	texture.endUse();

	//g.setColor(Color.white);
	//g.fillOval(this.getCenterPosX(), this.getCenterPosY(), 5, 5);
	//g.setColor(owner.playerColor);		

	drawPath(g);
    }

    /**
     * Do a rotation tick.
     * @return result of rotation. True - rotaton is finished. False - rotation in process.
     */
    public boolean doTurretRotationTick() {
	if (this.isTurretRotatingNow) {
	    if (this.getTurretRotation() == this.newTurretRotation) {
		this.isTurretRotatingNow = false;
		return true;
	    }

	    if (this.turretRotationDirection == RotationDirection.LEFT) {
		this.setTurretRotation((this.getTurretRotation() + 1) % Turn.MAX_FACING);
	    } else if (this.turretRotationDirection == RotationDirection.RIGHT) {
		this.setTurretRotation((this.getTurretRotation() - 1) % Turn.MAX_FACING);
	    }

	    return false;
	} else {
	    if (!this.isIdle()) {
		int rot = RotationUtil.getRotationFromXY(this.getCenterPosX(), this.getCenterPosY(), this.goalX * 24, this.goalY * 24) % Turn.MAX_FACING;
		this.rotateTurretTo(rot);	   		    
	    } else {
		if (this.targetEntity == null) { 
		    this.rotateTurretTo(this.currentFacing);
		}
	    }
	}

	return true;
    }

    /**
     * Sets rotation to entity immediately.
     * @param rot
     */
    public void setTurretRotation(int rot) {
	if (rot < 0) { rot = 31; } 
	this.turretRotation = rot;
    }

    /**
     * Sets desired rotation and let entity rotate with some rotation speed to desired rotation;
     * @param rot desired rotation value
     */
    public void rotateTurretTo(int rot) {
	rot = rot % 32;

	this.newTurretRotation = rot;

	// Select nearest rotation direction
	if (getTurretRotation() >= 24 && rot <= 8) {
	    this.turretRotationDirection = RotationDirection.LEFT;
	} else if (getTurretRotation() <= 8 && rot >= 24) {
	    this.turretRotationDirection = RotationDirection.RIGHT;
	} else
	    if (getTurretRotation() < rot) {
		this.turretRotationDirection = RotationDirection.LEFT;
	    } else if (getTurretRotation() > rot){
		this.turretRotationDirection = RotationDirection.RIGHT;
	    } else {
		this.isTurretRotatingNow = false;
		return;
	    }

	this.isTurretRotatingNow = true;
    }

    private int getTurretRotation() {
	return this.turretRotation;
    }

    @Override
    public void select() {
	this.isSelected = true;
    }

    @Override
    public void cancelSelect() {
	this.isSelected = false;
    }

    @Override
    public boolean isSelected() {
	return this.isSelected;
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }

    @Override
    public float getMoveSpeed() {
	return MOVE_SPEED;
    }

    @Override
    public float getTextureX() {
	return posX - (TEXTURE_WIDTH / 2) + 12;
    }

    @Override
    public float getTextureY() {
	return posY - (TEXTURE_HEIGHT / 2) + 6; 
    }

    @Override
    public int getRevealingRange() {
	return this.SHROUD_REVEALING_RANGE;
    }

    @Override
    public Path findPathFromTo(MobileEntity e, int aGoalX, int aGoalY) {
	return world.getVehiclePathfinder().findPathFromTo(this, aGoalX, aGoalY);
    }
    
    @Override
    public int getMinimumEnoughRange() {
	return 2;
    }    
    
    @Override
    public int getWaitAverageTime() {
	return this.WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS;
    }

    @Override
    public int getWaitSpreadTime() {
	return this.WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS;
    }

    @Override
    public int getBuildingCost() {
	return this.BUILDING_COST;
    }    
}
