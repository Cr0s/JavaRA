package cr0s.javara.combat.attack;

import cr0s.javara.combat.Armament;
import cr0s.javara.combat.Weapon;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Follow;
import cr0s.javara.order.Order;
import cr0s.javara.order.Target;

public class AttackFollow extends AttackBase {    
    public AttackFollow(EntityActor s) {
	super(s);
    }
    
    @Override
    public void update(int delta) {
	super.update(delta);
	
	if (this.isAttacking) {
	    this.doAttack(this.target);
	}
	
	this.isAttacking = this.target != null && this.target.isValidFor(self);
	if (!isAttacking) {
	    this.target = null;
	}
    }
    
    @Override
    public Activity getAttackActivity(Target tgt, boolean allowMove) {
	return new AttackActivity(this, this.self, tgt, allowMove);
    }

    @Override
    public void resolveOrder(Order order) {
	super.resolveOrder(order);
	
	if (order.orderString.equals("Stop")) {
	    this.target = null;
	}
    }
    
    private class AttackActivity extends Activity {

	private AttackFollow attack;
	private MobileEntity me;
	private EntityActor self;
	private Target target;
	
	public AttackActivity(AttackFollow a, EntityActor self, Target tgt, boolean allowMove) {
	    this.attack = a;
	    
	    if (self instanceof MobileEntity) {
		this.me = (MobileEntity) self;
	    }
	    
	    this.target = tgt;
	    this.self = self;
	}

	@Override
	public Activity tick(EntityActor a) {
	    if (this.isCancelled() || !this.target.isValidFor(this.self)) {
		return this.nextActivity;
	    }
	    
	    if (this.self.isDead()) {
		return this.nextActivity;
	    }
	    
	    final int rangeTolerance = 0;
	    
	    Armament weapon = this.attack.chooseArmamentForTarget(this.target);
	    if (weapon != null) {
		int range = (int) Math.max(0,  weapon.getWeapon().range - rangeTolerance);
		this.attack.target = this.target;
		
		if (this.me != null && !this.target.isInRange(this.self.getPosition(), weapon.getWeapon().range)) {
		    Follow f = (Follow) this.me.moveFollow(this.self, this.target, range);
		    f.queueActivity(this);
		    
		    return f;
		}
	    }
	    
	    return this;
	}
	
    }
}
