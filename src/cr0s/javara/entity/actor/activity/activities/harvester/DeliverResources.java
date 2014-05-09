package cr0s.javara.entity.actor.activity.activities.harvester;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.vehicle.common.EntityHarvester;

public class DeliverResources extends Activity {

    @Override
    public Activity tick(EntityActor a) {
	if (isCancelled) {
	    return nextActivity;
	}
	
	EntityHarvester harv = (EntityHarvester) a;
	
	Point procDestPoint = null;
	if (harv.linkedProc == null) {
	    // TODO: harv.linkedProc = harv.owner.getBase().findClosestProc(harv);
	}
	
	if (harv.linkedProc == null) {
	    return nextActivity;
	}
	
	procDestPoint = harv.linkedProc.getHarvesterCell();
	
	Move moveActivity = new Move(harv, procDestPoint, 0, null);
	moveActivity.queueActivity(new DropResources(harv));
	queueActivity(moveActivity);
	
	return this.nextActivity;
    }

}
