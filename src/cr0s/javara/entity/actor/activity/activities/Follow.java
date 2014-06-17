package cr0s.javara.entity.actor.activity.activities;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.order.Target;

public class Follow extends Activity {

    private EntityActor self;
    private Target target;
    private int range;
    
    private static final int REPATH_DELAY_TICKS = 20;
    private static final int REPATH_SPREAD = 5;
    
    private int repathDelay;
    
    public Follow (EntityActor self, Target tgt, int range) {
	this.self = self;
	this.target = tgt;
	this.range = range;
    }
    
    @Override
    public Activity tick(EntityActor a) {
	if (this.isCancelled() || !this.target.isValidFor(self)) {
	    return this.nextActivity;
	}
	
	if (this.target.isInRange(self.getPosition(), this.range) || --this.repathDelay > 0) {
	    return this;
	}
	
	this.repathDelay = this.self.world.getRandomInt(this.REPATH_DELAY_TICKS - this.REPATH_SPREAD, this.REPATH_DELAY_TICKS + this.REPATH_SPREAD);
	
	Activity move = ((MobileEntity) this.self).moveWithinRange(this.target, this.range);
	move.queueActivity(this);
	
	return move;
    }

}
