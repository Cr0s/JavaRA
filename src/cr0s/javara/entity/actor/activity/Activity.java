package cr0s.javara.entity.actor.activity;

import cr0s.javara.entity.actor.EntityActor;

public abstract class Activity {
    protected Activity nextActivity;
    protected boolean isCancelled;
    
    public Activity getNext() {
	return this.nextActivity;
    }
    
    public void cancel() {
	this.isCancelled = true;
	this.nextActivity = null;
    }
    
    public boolean isCancelled() {
	return this.isCancelled;
    }
    
    public void queueActivity(Activity a) {
	if (this.nextActivity != null) {
	    this.nextActivity.queueActivity(a);
	} else {
	    this.nextActivity = a;
	}
    }
    
    public abstract Activity tick(EntityActor a);
}
