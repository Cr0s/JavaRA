package cr0s.javara.entity.actor.activity.activities;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.order.Target;
import cr0s.javara.util.Pos;
import cr0s.javara.util.WaitAction;

public class Follow extends Activity {

    private EntityActor self;
    private Target target;
    private int range;
    
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
	
	final Pos cachedPosition = this.target.centerPosition();
	Activity move = ((MobileEntity) this.self).moveWithinRange(this.target, this.range);
	
	if (this.target.isInRange(self.getPosition(), this.range)) {
	    WaitFor wait = new WaitFor(new WaitAction() {
		@Override
		public boolean waitFor() {
		    return !Follow.this.target.isValidFor(Follow.this.self) || !Follow.this.target.centerPosition().equals(cachedPosition);
		}
	    });
	    
	    wait.queueActivity(move);
	    move.queueActivity(this);
	    
	    return wait;
	}
	
	move.queueActivity(this);
	return move;
    }

}
