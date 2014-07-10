package cr0s.javara.entity.vehicle.common;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.activity.activities.Deploy;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.ui.cursor.CursorType;
import cr0s.javara.util.RotationUtil;

public class EntityMcv extends EntityVehicle implements ISelectable, IDeployable, IHaveCost {

    private String TEXTURE_NAME = "mcv.shp";
    private SpriteSheet texture;

    private final int ROTATION_START_TEXTURE_INDEX = 0;
    private final int ROTATION_END_TEXTURE_INDEX = 31;
    private final int MAX_ROTATION = 32;	
    private final int BUILD_ROTATION = 12;

    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;
    private static final int SHROUD_REVEALING_RANGE = 5;
    private static final int WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS = 50;
    private static final int WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS = 15;

    private int updateTicks = 0;

    private int rotationDirection = 1;
    private boolean isDeploying = false;

    private final float MOVE_SPEED = 0.1f;
    private final int BUILDING_COST = 2000;

    public EntityMcv(Float posX, Float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	boundingBox.setBounds(posX, posY, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	boundingBox.setCenterX(this.getCenterPosX());
	boundingBox.setCenterY(this.getCenterPosY());
	
	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.setHp(600);
	this.setMaxHp(600);
	
	this.currentFacing = 16;
	
	this.ordersList.add(new McvDeployTargeter(this));
	
	this.setName("mcv");
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	boundingBox.setBounds(posX, posY - 6, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	boundingBox.setCenterX(this.getCenterPosX());
	boundingBox.setCenterY(this.getCenterPosY());
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

	float tx = this.getTextureX();
	float ty = this.getTextureY();

	//g.drawRect(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture.startUse();
	texture.getSubImage(0, currentFacing).drawEmbedded(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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
    public boolean canDeploy() {
	// Check deploy possibility via World blockingMap
	int bx = (int) (posX / 24) - (EntityConstructionYard.WIDTH_TILES / 2);
	int by = (int) (posY / 24) - (EntityConstructionYard.HEIGHT_TILES / 2);
	
	for (int x = 0; x < EntityConstructionYard.WIDTH_TILES; x++) {
	    for (int y = 0; y < EntityConstructionYard.HEIGHT_TILES; y++) {
		if (!world.isCellBuildable(bx + x, by + y, true)) {
		    return false;
		}
		
		Entity e = world.getEntityInPoint((bx + x) * 24, (by + y) * 24);
		if (e != null && !(e instanceof EntityMcv)) {
		    return false;
		}
	    }
	}
	
	return true;
    }

    @Override
    public void deploy() {
	if (canDeploy()) { 
	    deployConstructionYard();
	}
    }

    private void deployConstructionYard() {
	this.isDeploying = true;

	queueActivity(new Turn(this, this.BUILD_ROTATION, 3));
	queueActivity(new Deploy());
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }

    @Override
    public float getMoveSpeed() {
	return this.MOVE_SPEED;
    }

    @Override
    public float getTextureX() {
	return posX - (TEXTURE_WIDTH / 2) + 18;
    }

    @Override
    public float getTextureY() {
	return posY - (TEXTURE_HEIGHT / 2) + 12; 
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
    public void executeDeployment() {
	if (!this.isDeploying) {
	    return;
	}
	
	EntityConstructionYard cy = new EntityConstructionYard(posX - (EntityConstructionYard.WIDTH_TILES / 2 * 24), posY - (EntityConstructionYard.HEIGHT_TILES / 2 * 24), this.team, this.owner);
	cy.isVisible = true;
	cy.isSelected = true;
	world.addBuildingTo(cy);

	setDead();
    }
    
    @Override
    public int getMinimumEnoughRange() {
	return 3;
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
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia) {
	if (targeter.orderString.equals("Deploy") && ia.mouseButton == 1) {
	    return new Order("Deploy", null, null);
	}
	
	return super.issueOrder(self, targeter, target, ia);
    }
    
    @Override
    public void resolveOrder(Order order) {
	super.resolveOrder(order);
	
	if (order.orderString.equals("Deploy")) {
	    this.deploy();
	}
    }
    
    private class McvDeployTargeter extends OrderTargeter {
	public McvDeployTargeter(EntityActor ent) {
	    super("Deploy", 8, true, false, ent);
	}

	@Override
	public boolean canTarget(Entity self, Target target) {
	    return (self instanceof EntityMcv) && (target.getTargetEntity() == self);
	}

	@Override
	public CursorType getCursorForTarget(Entity self, Target target) {
	    return (((EntityMcv)self).canDeploy()) ? CursorType.CURSOR_DEPLOY : CursorType.CURSOR_NO_DEPLOY;
	}
    }

    @Override
    public int getBuildingCost() {
	return this.BUILDING_COST;
    }
}
