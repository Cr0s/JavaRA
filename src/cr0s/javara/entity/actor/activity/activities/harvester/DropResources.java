package cr0s.javara.entity.actor.activity.activities.harvester;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.vehicle.common.EntityHarvester;

public class DropResources extends Activity {

    private static final int WAITING_TICKS_PER_RESOURCE = 1;
    private int waitingTicks;
    
    public DropResources(EntityHarvester harv) {
	this.waitingTicks = this.WAITING_TICKS_PER_RESOURCE * harv.getCapacity();
    }
    
    @Override
    public Activity tick(EntityActor a) {
	if (isCancelled) {
	    return nextActivity;
	}
	
	EntityHarvester harv = (EntityHarvester) a;
	
	// Our target refiner is dead
	if (harv.linkedProc == null || harv.linkedProc.isDead() || harv.linkedProc.isDestroyed()) {
	    queueActivity(new DeliverResources()); // try to find another one refinery
	    return nextActivity;
	}	
	
	// If we're trying to drop resources not inside the refiner
	if (harv.getCellPos().getX() != harv.linkedProc.getHarvesterCell().getX() && 
		harv.getCellPos().getY() != harv.linkedProc.getHarvesterCell().getY()) {
	    
	    // Try to advance to refiner
	    DeliverResources deliverActivity = new DeliverResources();
	    deliverActivity.queueActivity(this.nextActivity);
	    queueActivity(deliverActivity);
	    
	    return nextActivity;
	}
	
	// If we not on drop facing, turn to it
	if (harv.currentFacing != harv.linkedProc.HARV_FACING) {
	    Turn turnActivity = new Turn(harv, harv.linkedProc.HARV_FACING, 1);
	    turnActivity.queueActivity(this);
	    
	    return turnActivity;
	}
	
	if (--this.waitingTicks <= 0) {
	    actuallyDropResources(harv);
	    
	    queueActivity(new FinishDrop());
	    queueActivity(new FindResources());
	    return nextActivity;
	}
	
	return this;
    }

    private void actuallyDropResources(EntityHarvester harv) {
	harv.clearContents();
    }
}
