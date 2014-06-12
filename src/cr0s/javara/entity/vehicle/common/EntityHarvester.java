package cr0s.javara.entity.vehicle.common;

import java.util.HashMap;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.IPips;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.actor.activity.activities.Wait;
import cr0s.javara.entity.actor.activity.activities.harvester.DeliverResources;
import cr0s.javara.entity.actor.activity.activities.harvester.DropResources;
import cr0s.javara.entity.actor.activity.activities.harvester.FindResources;
import cr0s.javara.entity.actor.activity.activities.harvester.FinishDrop;
import cr0s.javara.entity.actor.activity.activities.harvester.HarvestResource;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.building.common.EntityProc;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.render.map.ResourcesLayer;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.ui.cursor.CursorType;
import cr0s.javara.util.PointsUtil;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public class EntityHarvester extends EntityVehicle implements ISelectable, IShroudRevealer, IPips, IHaveCost {

    private String TEXTURE_NAME = "harv.shp";
    private SpriteSheet texture;

    private final int ROTATION_START_TEXTURE_INDEX = 0;
    private final int ROTATION_END_TEXTURE_INDEX = 31;
    private final int MAX_ROTATION = 32;	
    private final int BUILD_ROTATION = 12;

    private final float MOVE_SPEED = 0.3f;

    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;
    private static final int SHROUD_REVEALING_RANGE = 5;

    private static final int WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS = 30;
    private static final int WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS = 10;

    private static final int PIP_COUNT = 7;

    private int updateTicks = 0;

    private boolean isHarvesting = false;

    public EntityProc linkedProc;
    public Pos lastOrderPoint;
    public Pos lastHarvestedPoint;

    public static final int SEARCH_RADIUS_FROM_PROC = 30;
    public static final int SEARCH_RADIUS_FROM_ORDER = 25;

    private static final int CAPACITY = 20;
    public static final int LOAD_TICKS_PER_BALE = 7;

    private HashMap<Integer, Integer> contents;
    private int harvestingFrame;

    private final static int HARVESTING_FRAMES = 8;
    private final static int HARVESTING_FRAMES_DELAY_TICKS = 1;
    private int harvestingTicks = HARVESTING_FRAMES_DELAY_TICKS;

    public final static int DROPPING_FRAMES = 15;
    public final static int DROPPING_FRAMES_DELAY_TICKS = 2;
    private static final int MAX_FACINGS = 8;
    private int droppingTicks = DROPPING_FRAMES_DELAY_TICKS;
    private int droppingFrame;
    
    private final int BUILDING_COST = 1100;

    public EntityHarvester(Float posX, Float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	boundingBox.setBounds(posX + 6, posY - 6, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.setHp(600);
	this.setMaxHp(600);

	this.contents = new HashMap<>();

	this.ordersList.add(new HarvestTargeter(this));
	this.ordersList.add(new DeliverTargeter(this));
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	boundingBox.setBounds(posX + 6, posY - 6, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));

	if ((this.currentActivity instanceof Wait && (this.currentActivity.nextActivity instanceof HarvestResource)) 
		|| (this.currentActivity instanceof HarvestResource)) {
	    if (--this.harvestingTicks <= 0) {
		this.harvestingTicks = this.HARVESTING_FRAMES_DELAY_TICKS;
		this.harvestingFrame = (this.harvestingFrame + 1) % this.HARVESTING_FRAMES;
	    }
	} else if (this.currentActivity instanceof DropResources) {
	    if (--this.droppingTicks <= 0) {
		this.droppingTicks = this.DROPPING_FRAMES_DELAY_TICKS;

		this.droppingFrame++;

		// Repeat dropping sequence
		if (this.droppingFrame == this.DROPPING_FRAMES) {
		    this.droppingFrame = this.DROPPING_FRAMES - 5;
		}
	    }
	} else if (this.currentActivity instanceof FinishDrop) {
	    if (--this.droppingTicks <= 0) {
		this.droppingTicks = this.DROPPING_FRAMES_DELAY_TICKS;

		this.droppingFrame++;

		if (this.droppingFrame >= this.DROPPING_FRAMES) {
		    this.droppingFrame = this.DROPPING_FRAMES - 1;
		}
	    }
	}
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

	texture.startUse();

	if ((this.currentActivity instanceof Wait) || (this.currentActivity instanceof HarvestResource)) {
	    texture.getSubImage(0, 32 + (HARVESTING_FRAMES * RotationUtil.quantizeFacings(this.currentFacing, this.MAX_FACINGS) + harvestingFrame)).drawEmbedded(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT); 	    
	} else if ((this.currentFacing == EntityProc.HARV_FACING) 
		&& ((this.currentActivity instanceof DropResources) || (this.currentActivity instanceof FinishDrop))) {
	    boolean isFinishing = (this.currentActivity instanceof FinishDrop);

	    texture.getSubImage(0, 32 + (HARVESTING_FRAMES * 8) + ((isFinishing) ? DROPPING_FRAMES - this.droppingFrame :this.droppingFrame)).drawEmbedded(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	} else {
	    texture.getSubImage(0, this.currentFacing).drawEmbedded(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	texture.endUse();

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

    public void acceptResource(int resourceType) {
	if (!contents.containsKey(resourceType)) {
	    contents.put(resourceType, 1);
	} else {
	    int previousValue = contents.get(resourceType);
	    contents.remove(resourceType);
	    contents.put(resourceType, previousValue + 1);
	}
    }

    public boolean isFull() {
	return getCapacity() >= this.CAPACITY;
    }

    public boolean isEmpty() {
	return getCapacity() == 0;
    }

    public int getCapacity() {
	int sum = 0;

	for (Integer value : this.contents.values()) {
	    sum += value;
	}

	return sum;
    }

    public void clearContents() {
	if (this.linkedProc != null) {
	    for (int x : this.contents.keySet()) {
		this.linkedProc.acceptResources(((x == 1) ? 25 : 50) * this.contents.get(x));
	    }
	}

	this.contents.clear();
    }

    @Override
    public int getPipCount() {
	return this.PIP_COUNT;
    }

    @Override
    public Color getPipColorAt(int i) {
	int n = i * this.CAPACITY / this.PIP_COUNT;

	for (Integer x : this.contents.keySet()) {
	    int value = this.contents.get(x);

	    if (n < value) {
		return (x == 1) ? Color.yellow : Color.red;
	    } else {
		n -= value;
	    }
	}

	return null;
    }

    @Override
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia) {
	if (targeter.orderString.equals("Harvest") && ia.mouseButton == 1) {
	    return new Order("Harvest", null, target.getTargetCell());
	} else if (targeter.orderString.equals("Deliver") && ia.mouseButton == 1) {
	    return new Order("Deliver", null, null, target.getTargetEntity());
	}

	return super.issueOrder(self, targeter, target, ia);
    }

    @Override
    public void resolveOrder(Order order) {
	super.resolveOrder(order);
	
	if (order.orderString.equals("Harvest")) {
	    this.lastOrderPoint = order.targetPosition;
	    
	    cancelActivity();
	    
	    this.moveTo(order.targetPosition);
	    queueActivity(new FindResources());
	} else if (order.orderString.equals("Deliver")) {
	    Entity e = order.targetEntity;
	    
	    if (e != null && (e instanceof EntityProc)) {
		this.linkedProc = (EntityProc) e;
		
		if (isIdle() || (!(this.currentActivity instanceof DropResources) && !(this.currentActivity instanceof FinishDrop))) { 
		    cancelActivity();
		}
		
		queueActivity(new DeliverResources());
	    }
	}
    }    
    
    public EntityProc findClosestProc() {
	EntityProc closestProc = null;
	
	for (Entity e : world.getEntitiesList()) {
	    if (e instanceof EntityProc) {
		int distance = this.getCenterPos().distanceToSq(new Pos(e.boundingBox.getCenterX(), e.boundingBox.getCenterY()));
		if (closestProc == null || distance < this.getCenterPos().distanceToSq(new Pos(closestProc.boundingBox.getCenterX(), closestProc.boundingBox.getCenterY()))) {
		    closestProc = (EntityProc) e;
		}
	    }
	}
	
	return closestProc;
    }

    private class HarvestTargeter extends OrderTargeter {
	public HarvestTargeter(EntityActor ent) {
	    super("Harvest", 6, true, false, ent);
	}

	@Override
	public boolean canTarget(Entity self, Target target) {
	    return target.isCellTarget() && (self instanceof EntityHarvester) && (self.owner.getShroud() != null && self.owner.getShroud().isExplored(target.getTargetCell())) && !self.world.getMap().getResourcesLayer().isCellEmpty(target.getTargetCell());
	}

	@Override
	public CursorType getCursorForTarget(Entity self, Target target) {
	    return CursorType.CURSOR_HARVEST;
	}
    }    

    private class DeliverTargeter extends OrderTargeter {
	public DeliverTargeter(EntityActor ent) {
	    super("Deliver", 6, true, false, ent);
	}

	@Override
	public boolean canTarget(Entity self, Target target) {
	    Entity e = target.getTargetEntity();

	    if (e == null || !(e instanceof EntityProc) || !canEnterProc(self, target.getTargetEntity())) {
		return false;
	    }
	    
	    return true;
	}

	private boolean canEnterProc(Entity self, Entity p) {
	    return (p != null) && (p instanceof EntityProc) && ((EntityProc) p).isFrendlyTo((EntityActor) self);
	}
	
	@Override
	public CursorType getCursorForTarget(Entity self, Target target) {
	    return canEnterProc(self, target.getTargetEntity()) ? CursorType.CURSOR_ENTER : CursorType.CURSOR_NO_ENTER;
	}
    }

    @Override
    public int getBuildingCost() {
	return this.BUILDING_COST;
    }      
}
