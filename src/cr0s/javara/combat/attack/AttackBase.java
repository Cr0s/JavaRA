package cr0s.javara.combat.attack;

import java.util.ArrayList;

import cr0s.javara.combat.Armament;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.order.IOrderIssuer;
import cr0s.javara.order.IOrderResolver;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.ui.cursor.CursorType;
import cr0s.javara.util.Pos;

public abstract class AttackBase implements IOrderResolver, IOrderIssuer {
    public boolean isAttacking;

    public ArrayList<Armament> armaments;
    protected EntityActor self;

    private int facing;

    private ArrayList<OrderTargeter> orders = new ArrayList<OrderTargeter>();

    public AttackBase(EntityActor s) {
	this.self = s;

	this.armaments = new ArrayList<Armament>();
    }

    public void addArmament(Armament arma) {
	this.armaments.add(arma);
    }

    protected boolean canAttack(Target tgt) {
	if (this.self.world == null || this.self.isDead() || tgt == null || !tgt.isValidFor(this.self)) {
	    System.out.println("canTAttack(): Target is invalid");
	    return false;
	}

	for (Armament arma : this.armaments) {
	    if (!arma.isReloading()) {
		return true;
	    }
	}

	System.out.println("canTAttack(): Armament is reloading");
	return false;
    }

    public void doAttack(Target tgt) {
	if (!canAttack(tgt)) {
	    return;
	}

	for (Armament arma : this.armaments) {
	    arma.checkFire(this.facing, tgt);
	}
    }

    public float getMaxRange() {
	float maxRange = 0f;

	for (Armament arma : this.armaments) {
	    if (arma.getWeapon().range > maxRange) {
		maxRange = arma.getWeapon().range;
	    }
	}

	return maxRange;
    }

    public class AttackTargeter extends OrderTargeter {
	AttackBase ab;

	public AttackTargeter(EntityActor ent, AttackBase aab) {
	    super("Attack", 6, false, true, ent);

	    this.ab = aab;
	}

	public boolean canTargetActor(Target tgt) {
	    EntityActor a = (EntityActor) tgt.getTargetEntity();
	    if (!ab.hasAnyValidWeapons(tgt)) {
		return false;
	    }

	    // Can't target self
	    if (a == this.entity) {
		return false;
	    }

	    boolean isEnemy = !a.isFrendlyTo(this.entity);

	    return isEnemy;
	}

	public boolean canTargetCell(Target tgt) {
	    if (!this.entity.world.getMap().isInMap(tgt.getTargetCell())) {
		return false;
	    }

	    if (!ab.hasAnyValidWeapons(tgt)) {
		return false;
	    }

	    return true;
	}

	@Override
	public boolean canTarget(Entity self, Target target) {
	    return (target.isCellTarget() && canTargetCell(target)) 
		    || (target.isEntityTarget() && target.getTargetEntity() instanceof EntityActor && canTargetActor(target));
	}

	@Override
	public CursorType getCursorForTarget(Entity self, Target target) {
	    return CursorType.CURSOR_ATTACK;
	}
    }

    public Armament chooseArmamentForTarget(Target tgt) {
	for (Armament arma : this.armaments) {
	    if (arma.getWeapon().isValidAgainst(tgt)) {
		return arma;
	    }
	}

	return null;
    }

    public boolean hasAnyValidWeapons(Target tgt) {
	return chooseArmamentForTarget(tgt) != null;
    }

    @Override
    public ArrayList<OrderTargeter> getOrders() {
	if (this.orders.isEmpty()) {
	    this.orders.add(new AttackTargeter(this.self, this));
	}

	return this.orders;
    }

    @Override
    public Order issueOrder(Entity self, OrderTargeter targeter, Target target,
	    InputAttributes ia) {
	if (targeter instanceof AttackTargeter) {
	    if (target.isCellTarget()) {
		//String order, Entity subj, Pos targetLoc, Entity targetEnt, String targetStr, Pos extraLoc
		return new Order("Attack", null, target.getTargetCell());
	    } else {
		if (target.getTargetEntity() instanceof EntityActor) {
		    EntityActor a = (EntityActor) target.getTargetEntity();
		    
		    return new Order("Attack", null, null, target.getTargetEntity());
		} else {
		    return null;
		}
	    }
	}

	return null;
    }

    @Override
    public void resolveOrder(Order order) {
	if (order.orderString.equals("Attack")) {
	    Target tgt = new Target(order.targetEntity, order.targetPosition);
	    
	    if (tgt == null || !tgt.isValidFor(this.self)) {
		return;
	    }
	    
	    this.attackTarget(tgt, true);
	}
    }

    private void attackTarget(Target tgt, boolean allowMove) {
	this.self.cancelActivity();
	
	this.self.queueActivity(this.getAttackActivity(tgt, allowMove));
    }

    public boolean isReachableTarget(Target tgt, boolean allowMove) {
	return this.hasAnyValidWeapons(tgt) 
		&& (tgt.isInRange(this.self.getPosition(), this.getMaxRange()) 
			|| (this.self instanceof MobileEntity && allowMove));
    }
    
    public abstract Activity getAttackActivity(Target tgt, boolean allowMove);
    
    public void update(int delta) {
	for (Armament arma : this.armaments) {
	    arma.update(delta);
	}
    }
}
