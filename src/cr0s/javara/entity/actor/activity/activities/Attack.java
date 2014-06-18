package cr0s.javara.entity.actor.activity.activities;

import cr0s.javara.combat.attack.AttackBase;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.order.Target;
import cr0s.javara.util.RotationUtil;

public class Attack extends Activity {

    private Target target;
    private boolean allowMove;
    private AttackBase attack;
    private float range;
    
    private static final int REPATH_DELAY_TICKS = 1;
    private static final int REPATH_SPREAD = 5;
    
    private int repathDelay;
    
    public Attack(AttackBase attack, EntityActor self, Target tgt, float range, boolean allowMove) {
	this.target = tgt;
	this.range = range;
	this.allowMove = allowMove;
	this.attack = attack;
    }
    
    @Override
    public Activity tick(EntityActor self) {
	Activity ret = this.innerTick(self);
	this.attack.isAttacking = ret == this;

	return ret;
    }
    
    private Activity innerTick(EntityActor self) {
	if (this.isCancelled()) {
	    return this.nextActivity;
	}
	
	if (!this.target.isValidFor(self)) {
	    return this.nextActivity;
	}
	
	if (!this.target.isInRange(self.getPosition(), this.range)) {
	    if (--this.repathDelay > 0) {
		return this;
	    }
	    
	    this.repathDelay = self.world.getRandomInt(this.REPATH_DELAY_TICKS - this.REPATH_SPREAD,  this.REPATH_DELAY_TICKS + this.REPATH_SPREAD);
	    
	    if (this.allowMove && (self instanceof MobileEntity)) {
		Activity move = ((MobileEntity) self).moveWithinRange(this.target, (int) this.range);
		move.queueActivity(this);
		
		return move;
	    }
	    
	    return this.nextActivity;
	}
	
	// Face the target
	int desiredFacing = RotationUtil.getRotationFromXY(self.getPosition().getX(), self.getPosition().getY(), this.target.centerPosition().getX(), this.target.centerPosition().getY());

	if (self.getMaxFacings() != 32) {
	    desiredFacing = RotationUtil.quantizeFacings(desiredFacing, self.getMaxFacings());
	}
	
	if (desiredFacing != self.currentFacing) {
	    Turn turn = new Turn(self, desiredFacing, 0);
	    turn.queueActivity(this);
	    
	    return turn;
	}
	
	this.attack.doAttack(this.target);
	return this;
    }
}
