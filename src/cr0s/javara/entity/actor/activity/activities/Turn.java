package cr0s.javara.entity.actor.activity.activities;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;

public class Turn extends Activity {

    private int desiredFacing;
    public enum RotationDirection { LEFT, RIGHT } // +1 or -1 to facing value
    private RotationDirection rotationDirection;
    
    private int ticks;
    private int ticksBetweenTurn;
    
    public static final int MAX_FACING = 32;
    
    public Turn(EntityActor a, int aDesiredFacing, int aTicksBetweenTurn) {
	this.desiredFacing = aDesiredFacing % 32;

	// Select nearest rotation direction
	if (a.currentFacing >= 24 && desiredFacing <= 8) {
	    this.rotationDirection = RotationDirection.LEFT;
	} else if (a.currentFacing <= 8 && desiredFacing >= 24) {
	    this.rotationDirection = RotationDirection.RIGHT;
	} else {
	    if (a.currentFacing < desiredFacing) {
		this.rotationDirection = RotationDirection.LEFT;
	    } else if (a.currentFacing > desiredFacing){
		this.rotationDirection = RotationDirection.RIGHT;
	    }
	}
	
	this.ticksBetweenTurn = aTicksBetweenTurn;
    }
    
    @Override
    public Activity tick(EntityActor a) {
	if (a.currentFacing == this.desiredFacing) {
	    return nextActivity;
	}
	
	if (++ticks >= ticksBetweenTurn) {
	    this.ticks = 0;
	    
	    int newFacing = a.currentFacing;
	    if (this.rotationDirection == RotationDirection.LEFT) {
		newFacing = (a.currentFacing + 1) % MAX_FACING;
	    } else if (this.rotationDirection == RotationDirection.RIGHT) {
		newFacing = (a.currentFacing - 1) % MAX_FACING;
	    }
	    
	    // Turn by circle
	    if (newFacing < 0) {
		newFacing = MAX_FACING - 1;
	    }
	    
	    a.currentFacing = newFacing % MAX_FACING;
	}
	
	return this;
    }

}
