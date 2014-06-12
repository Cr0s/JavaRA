package cr0s.javara.entity.actor.activity.activities.harvester;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Wait;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.util.Pos;

public class HarvestResource extends Activity {

    public HarvestResource() {
	// TODO Auto-generated constructor stub
    }

    @Override
    public Activity tick(EntityActor a) {
	if (isCancelled) {
	    return nextActivity;
	}
	
	EntityHarvester harv = (EntityHarvester) a;
	
	// Deliver resources if full
	if (harv.isFull()) {
	    return deliverResources();
	}
	
	Pos currentCell = harv.getCellPos();
	
	// Try to find resources if there is no resources in current cell
	if (harv.world.getMap().getResourcesLayer().isCellEmpty(currentCell)) {
	    return findResources(currentCell);
	}
	
	// We have resources there, harvest it
	int resourceType = harv.world.getMap().getResourcesLayer().harvestCell(currentCell);
	if (resourceType != -1) { 
	    harv.acceptResource(resourceType);
	    harv.lastHarvestedPoint = currentCell;
	} else {
	    return findResources(currentCell);
	}
	
	Wait waitActivity = new Wait(harv.LOAD_TICKS_PER_BALE);
	waitActivity.queueActivity(this);
	
	return waitActivity;
    }

    public Activity deliverResources() {
	DeliverResources deliverActivity = new DeliverResources();
	deliverActivity.queueActivity(nextActivity);

	return deliverActivity;	
    }
    
    public Activity findResources(Pos currentCell) {
	FindResources findActivity = new FindResources();
	findActivity.queueActivity(this.nextActivity);

	return findActivity;	
    }
}
