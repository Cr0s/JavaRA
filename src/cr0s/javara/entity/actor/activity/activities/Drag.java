package cr0s.javara.entity.actor.activity.activities;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.util.InterpolatePos;

public class Drag extends Activity {
    
    private Point start, end;
    private int lengthInTicks;

    private int ticks;
    
    public Drag(Point aStart, Point aEnd, int aLengthInTicks) {
	this.start = aStart;
	this.end = aEnd;
	this.lengthInTicks = aLengthInTicks;
    }
    
    @Override
    public Activity tick(EntityActor a) {
	if (isCancelled || !(a instanceof MobileEntity)) {
	    return nextActivity;
	}
	
	MobileEntity me = (MobileEntity)a;
	
	//Point currentPos = me.getPos();
	Point nextPos;
	if (lengthInTicks > 1) { 
	    nextPos = InterpolatePos.interpolatePos(start, end, ticks, lengthInTicks - 1);
	} else {
	    nextPos = end;
	}
	
	me.setPos(nextPos);
	
	if (++ticks >= lengthInTicks) {
	    cancel();
	    me.isMovingToCell = false;
	}
	
	return this;
    }

}
