package cr0s.javara.entity.vehicle.soviet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.combat.Armament;
import cr0s.javara.combat.Armament.Barrel;
import cr0s.javara.combat.attack.AttackTurreted;
import cr0s.javara.combat.attack.AutoTarget;
import cr0s.javara.combat.weapon.Weapon105mm;
import cr0s.javara.combat.weapon.Weapon120mm;
import cr0s.javara.combat.weapon.WeaponMammothTusk;
import cr0s.javara.combat.weapon.WeaponSCUD;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.actor.activity.activities.Turn.RotationDirection;
import cr0s.javara.entity.building.common.EntityProc;
import cr0s.javara.entity.building.common.EntityWarFactory;
import cr0s.javara.entity.turreted.IHaveTurret;
import cr0s.javara.entity.turreted.Turret;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public class EntityMammothTank extends EntityVehicle implements ISelectable, Mover, IHaveCost, IHaveTurret {

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

    private final float MOVE_SPEED = 0.07f;

    private final float SHIFT = 12;

    private final int BUILDING_COST = 100;
    private Turret turret;

    private AttackTurreted attack;
    Pos choosen;

    private ArrayList<Pos> cells = new ArrayList<Pos>();
    private AutoTarget autoTarget;
    
    
    public EntityMammothTank(Float posX, Float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.isVisible = true;

	this.setHp(900);
	this.setMaxHp(900);

	this.turret = new Turret(this, new Pos(0, 0), texture, 32, 32);
	this.turret.setTurretSize(TEXTURE_WIDTH, TEXTURE_HEIGHT);

	Armament arma = new Armament(this, new Weapon120mm());
	arma.addBarrel(new Pos(14, -3, 6), 0);
	arma.addBarrel(new Pos(14, 3, 6), 0);

	Armament arma2 = new Armament(this, new WeaponMammothTusk());
	arma2.addBarrel(new Pos(-3, -6, 6), -30);
	arma2.addBarrel(new Pos(-3, 6, 6), 30);

	attack = new AttackTurreted(this);
	attack.armaments.add(arma);
	attack.armaments.add(arma2);

	this.ordersList.addAll(attack.getOrders());
	
	this.autoTarget = new AutoTarget(this, this.attack);
	
	this.setName("4tnk");
	
	this.requiredToBuild.add(EntityWarFactory.class);
	//this.requiredToBuild.add(EntityFix.class);
	//this.requiredToBuild.add(EntitySovietTechCenter.class);
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);

	if (!this.attack.isAttacking) {
	    if (!this.isIdle() && (this.currentActivity instanceof Move || this.currentActivity instanceof Move.MovePart)) { 
		this.turret.setTarget(new Pos(goalX * 24, goalY * 24));
	    } else {
		this.turret.rotateTurretTo(this.currentFacing);
	    }
	}

	attack.update(delta);
	this.autoTarget.update(delta);
	
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

	/*for (Armament arma : this.attack.armaments) {
	Pos actorCenter = (this instanceof IHaveTurret)
		? ((IHaveTurret) this).getTurrets().get(0).getCenterPos()
			: this.getPosition();
		g.setColor(Color.white);
		g.fillOval(actorCenter.getX() - 2, actorCenter.getY() - 2, 4, 4);
		g.setColor(owner.playerColor);		

		float angle = RotationUtil.facingToAngle(this.turret.getCurrentFacing(), Turn.MAX_FACING);
		g.setColor(Color.red);
		g.setLineWidth(1);
		g.drawLine(actorCenter.getX(), actorCenter.getY(), (float) (actorCenter.getX() - 30 * Math.sin(angle)), (float) (actorCenter.getY() - 30 * Math.cos(angle)));

		g.setColor(Color.green);
		g.drawLine((float) (actorCenter.getX() - 15 * Math.cos(angle)), (float) (actorCenter.getY() - 15 * -Math.sin(angle)),
			(float) (actorCenter.getX() + 15 * Math.cos(angle)), (float) (actorCenter.getY() + 15 * -Math.sin(angle)));

		g.setColor(Color.white);
		for (int i = 0; i < arma.getBarrels().size(); i++) {
		    Barrel b = arma.getBarrels().get(i);

		    Pos muzzlePos = arma.getMuzzlePos(b);

		    //g.setColor(i % 2 == 0 ? Color.green : Color.red);
		    g.fillRect(muzzlePos.getX() - 2, muzzlePos.getY() - 2, 4, 4);

		    float a = RotationUtil.facingToAngle(arma.getMuzzleFacing(b), this.getMaxFacings());
		    float x = (float) (muzzlePos.getX() - Math.sin(a) * 10);
		    float y = (float) (muzzlePos.getY() - Math.cos(a) * 10);
		    g.drawLine(muzzlePos.getX(), muzzlePos.getY(), x, y);
		}		

	}*/
	
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

    @Override
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia) {
	if (super.issueOrder(self, targeter, target, ia) == null) {
	    return this.attack.issueOrder(self, targeter, target, ia);
	}

	this.attack.cancelAttack();

	return super.issueOrder(self, targeter, target, ia);
    }

    @Override
    public void resolveOrder(Order order) {
	if (order.orderString.equals("Attack") || order.orderString.equals("Stop")) {
	    this.attack.resolveOrder(order);
	} else {
	    super.resolveOrder(order);
	}
    }    
}
