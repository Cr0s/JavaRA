package cr0s.javara.entity.actor.activity.activities;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.util.Action;

public class Wait extends Activity {

    private int remainingTicks;
    
    public Wait(int period) {
	this.remainingTicks = period;
    }
    
    @Override
    public Activity tick(EntityActor a) {
	if (isCancelled || --this.remainingTicks <= 0) {
	    return nextActivity;
	}
	
	return this;
    }
}
