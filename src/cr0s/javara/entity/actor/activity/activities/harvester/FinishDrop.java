package cr0s.javara.entity.actor.activity.activities.harvester;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;

public class FinishDrop extends Activity {

    private static final int FINISH_DELAY_TICKS = 8;
    private int waitTicks = FINISH_DELAY_TICKS;
    
    @Override
    public Activity tick(EntityActor a) {
	
	if (--this.waitTicks <= 0) {
	    return nextActivity;
	}
	
	return this;
    }

}
