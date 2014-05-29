package cr0s.javara.entity.vehicle.tank;

import java.util.LinkedList;
import java.util.List;
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
import cr0s.javara.entity.turreted.IHaveTurret;
import cr0s.javara.entity.turreted.Turret;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.RotationUtil;

public class EntityMammonthTank extends EntityVehicle implements ISelectable, Mover, IHaveCost, IHaveTurret {

    private String TEXTURE_NAME = "4tnk.shp";
    private SpriteSheet texture;

    private final int ROTATION_START_TEXTURE_INDEX = 0;
    private final int ROTATION_END_TEXTURE_INDEX = 31;

    private final int MAX_ROTATION = 32;	

    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;
    private static final int SHROUD_REVEALING_RANGE = 8;
    private static final int WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS = 15;
    private static final int WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS = 5;

    private int updateTicks = 0;

    private int turretRotation = 0;
    private RotationDirection turretRotationDirection;
    private boolean isTurretRotatingNow = false;
    private int newTurretRotation = 0;

    private Entity targetEntity = null;

    private final float MOVE_SPEED = 0.1f;

    private final float SHIFT = 12;
    
    private final int BUILDING_COST = 2000;
    private Turret turret;

    public EntityMammonthTank(Float posX, Float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.isVisible = true;

	this.setHp(900);
	this.setMaxHp(900);
	
	this.turret = new Turret(this, new Point(0, 0), texture, 32, 32);
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	
	if (!this.isIdle()) { 
	    this.turret.setTarget(new Point(goalX * 24, goalY * 24));
	} else {
	    this.turret.rotateTurretTo(this.currentFacing);
	}
	
	boundingBox.setBounds(this.posX, this.posY, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
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
	this.turret.render(g);
	texture.endUse();

	//g.setColor(Color.white);
	//g.fillOval(this.getCenterPosX(), this.getCenterPosY(), 5, 5);
	//g.setColor(owner.playerColor);		

	drawPath(g);
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
	return posX - (TEXTURE_WIDTH / 2) + (TEXTURE_WIDTH / 6) + 4;
    }

    @Override
    public float getTextureY() {
	return posY - (TEXTURE_HEIGHT / 2) + (TEXTURE_HEIGHT / 8) + 9; 
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

    @Override
    public void drawTurrets(Graphics g) {
    }

    @Override
    public void updateTurrets(int delta) {
	this.turret.update(delta);
    }

    @Override
    public List<Turret> getTurrets() {
	LinkedList<Turret> res = new LinkedList<Turret>();
	
	res.add(this.turret);
	
	return res;
    }    
}
