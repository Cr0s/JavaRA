package cr0s.javara.entity.vehicle.soviet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.combat.Armament;
import cr0s.javara.combat.Armament.Barrel;
import cr0s.javara.combat.ArmorType;
import cr0s.javara.combat.Weapon;
import cr0s.javara.combat.attack.AttackFrontal;
import cr0s.javara.combat.attack.AttackTurreted;
import cr0s.javara.combat.attack.AutoTarget;
import cr0s.javara.combat.weapon.Weapon105mm;
import cr0s.javara.combat.weapon.WeaponSCUD;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.activity.activities.Turn.RotationDirection;
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

public class EntityV2Launcher extends EntityVehicle implements ISelectable, Mover, IHaveCost {

    private String TEXTURE_NAME = "v2rl.shp";
    private SpriteSheet texture;

    private static final int TEXTURE_WIDTH = 40;
    private static final int TEXTURE_HEIGHT = 40;
    private static final int ATTACK_OFFSET = 64;
    private static final int ATTACKING_FACINGS = 8;
    
    private static final int SHROUD_REVEALING_RANGE = 5;
    private static final int WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS = 15;
    private static final int WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS = 5;

    private int updateTicks = 0;

    private Entity targetEntity = null;

    private final float MOVE_SPEED = 0.3f;

    private final float SHIFT = 12;

    private final int BUILDING_COST = 1150;

    private AttackFrontal attack;
    private AutoTarget autoTarget;
    
    public EntityV2Launcher(Float posX, Float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.isVisible = true;

	this.setMaxHp(200);
	this.setHp(this.getMaxHp());
	
	this.armorType = ArmorType.LIGHT;

	Armament arma = new Armament(this, new WeaponSCUD());
	arma.addBarrel(new Pos(0, 0), 0);

	attack = new AttackFrontal(this);
	attack.armaments.add(arma);

	this.autoTarget = new AutoTarget(this, this.attack);
	
	this.ordersList.addAll(attack.getOrders());
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);

	boundingBox.setBounds(posX + (TEXTURE_WIDTH / 4) - 7, posY + (TEXTURE_WIDTH / 4) - 12, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));	

	this.attack.update(delta);
	this.autoTarget.update(delta);
    }

    @Override
    public void renderEntity(Graphics g) {
	super.renderEntity(g);

	if (Main.DEBUG_MODE) {
	    g.setLineWidth(1);
	    g.setColor(owner.playerColor);
	    g.draw(boundingBox);
	}


	texture.startUse();
	
	int textureIndex = (this.attack.armaments.get(0).isReloading() ? 32 : 0) + currentFacing;
	if (this.attack.isAttacking) {
	    int attackingFacing = RotationUtil.quantizeFacings(this.currentFacing, this.ATTACKING_FACINGS) % this.ATTACKING_FACINGS;
	    textureIndex = this.ATTACK_OFFSET + (this.attack.armaments.get(0).isReloading() ? this.ATTACKING_FACINGS : 0) + attackingFacing;
	}
	
	texture.getSubImage(0, textureIndex).drawEmbedded(this.getTextureX(), this.getTextureY(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	
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

    @Override
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia) {
	if (super.issueOrder(self, targeter, target, ia) == null) {
	    return this.attack.issueOrder(self, targeter, target, ia);
	}

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
