package cr0s.javara.combat.attack;

import cr0s.javara.combat.Armament;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Attack;
import cr0s.javara.entity.actor.activity.activities.Follow;
import cr0s.javara.entity.actor.activity.activities.Wait;
import cr0s.javara.entity.turreted.IHaveTurret;
import cr0s.javara.entity.turreted.Turret;
import cr0s.javara.order.Target;
import cr0s.javara.util.RotationUtil;

public class AttackCharge extends AttackBase {
    private boolean isCharged = false;
    private int maxChargeTicks;
    private int rechargeTicks;
    public int ticks;
    
    public AttackCharge(EntityActor s) {
	super(s);
    }

    @Override
    protected boolean canAttack(Target tgt) {
	if (!super.canAttack(tgt)) {
	    return false;
	}

	return this.isCharged;
    }

    @Override
    public Activity getAttackActivity(Target tgt, boolean allowMove) {
	Armament arma = this.chooseArmamentForTarget(tgt);
	if (arma == null) {
	    return null;
	}
	
	return new Charge(this, this.self, tgt, maxChargeTicks);
    }
    
    public void setCharged(boolean charged) {
	this.isCharged = charged;
    }
    
    @Override
    public void doAttack(Target tgt) {
	if (!canAttack(tgt)) {
	    return;
	}

	for (Armament arma : this.armaments) {
	    arma.checkFire(0, tgt);
	}
    }    
    
    public class Charge extends Activity {

	private AttackCharge attack;
	private MobileEntity me;
	private EntityActor self;
	private Target target;
	private int maxChargeTicks;
	
	public Charge(AttackCharge a, EntityActor self, Target tgt, int maxChargeTicks) {
	    this.attack = a;
	    
	    if (self instanceof MobileEntity) {
		this.me = (MobileEntity) self;
	    }
	    
	    this.target = tgt;
	    this.self = self;
	    
	    this.maxChargeTicks = maxChargeTicks;
	}

	@Override
	public Activity tick(EntityActor a) {
	    if (this.isCancelled() || this.attack == null || !this.target.isInRange(this.self.getPosition(), this.attack.getMaxRange()) || !this.target.isValidFor(this.self)) {
		return this.nextActivity;
	    }
	    
	    if (this.self.isDead()) {
		return this.nextActivity;
	    }
	    
	    if (++ticks > this.maxChargeTicks) {
		setCharged(true);
		ticks = 0;
		
		doAttack(this.target);
		
		Wait wait = new Wait(rechargeTicks);
		wait.queueActivity(this);
		wait.queueActivity(this.nextActivity);
		
		return wait;
	    }
	    
	    return this;
	}
	
    }

    public void setMaxChargeTicks(int maxChargeTicks) {
	this.maxChargeTicks = maxChargeTicks;
    }    
    
    public void setRechargeTicks(int rechargeTicks) {
	this.rechargeTicks = rechargeTicks;
    }      
}
