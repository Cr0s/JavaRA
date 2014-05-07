package cr0s.javara.entity.actor.activity.activities;

import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;

public class Deploy extends Activity {

    @Override
    public Activity tick(EntityActor a) {
	((IDeployable) a).executeDeployment();
	
	return nextActivity;
    }

}
