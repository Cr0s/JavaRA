package cr0s.javara.entity.actor.activity.activities;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.util.Action;
import cr0s.javara.util.WaitAction;


public class WaitFor extends Activity {

    private WaitAction f;
    private boolean interruptable = true;
    private boolean cancelNextActivity = true;
    
    public WaitFor(WaitAction f) {
	this.f = f;
    }
    
    public WaitFor(WaitAction f, boolean interruptable) {
	this.f = f;
	this.interruptable = interruptable;
    }

    public WaitFor(WaitAction f, boolean interruptable, boolean cancelNextActivity) {
	this.f = f;
	this.interruptable = interruptable;
	this.cancelNextActivity = cancelNextActivity;
    }    
    
    @Override
    public Activity tick(EntityActor a) {
	return (f == null || f.waitFor()) ? this.nextActivity : this;
    }
    
    @Override
    public void cancel() {
	if (!this.interruptable) {
	    return;
	}
	
	// Interrupt
	this.f = null;
	super.cancel();
	
	// Interrupt next if needed
	if (this.cancelNextActivity && this.nextActivity != null) {
	    this.nextActivity.cancel();
	}
    }
}