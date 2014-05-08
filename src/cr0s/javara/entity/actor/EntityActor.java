package cr0s.javara.entity.actor;

import org.newdawn.slick.Graphics;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;

public class EntityActor extends Entity {

    public Activity currentActivity;
    
    public EntityActor(float posX, float posY, Team team, Player owner,
	    final float aSizeWidth, final float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
    }

    @Override
    public void updateEntity(final int delta) {
	updateDelta = delta;
	
	if (this.currentActivity != null) {
	    this.currentActivity = this.currentActivity.tick(this);
	}
    }

    public void queueActivity(Activity a) {
	if (this.currentActivity != null) {
	    this.currentActivity.queueActivity(a);
	} else {
	    this.currentActivity = a;
	}
    }
    
    public void cancelActivity() {
	if (this.currentActivity != null) {
	    this.currentActivity.cancel();
	} 	
    }
    
    @Override
    public void renderEntity(final Graphics g) {
    }

    @Override
    public boolean shouldRenderedInPass(final int passNum) {
	return false;
    }

    public boolean isFrendlyTo(EntityActor other) {
	// TODO: add ally logic
	if (this.owner == other.owner) {
	    return true;
	}
	
	return false;
    }
    
    public boolean isIdle() {
	return this.currentActivity == null;
    }
}
