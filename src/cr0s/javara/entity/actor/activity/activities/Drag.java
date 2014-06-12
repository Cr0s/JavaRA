package cr0s.javara.entity.actor.activity.activities;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.util.PointsUtil;
import cr0s.javara.util.Pos;

public class Drag extends Activity {
    
    private Pos start, end;
    private int lengthInTicks;

    private int ticks;
    
    public Drag(Pos aStart, Pos aEnd, int aLengthInTicks) {
	this.start = aStart;
	this.end = aEnd;
	this.lengthInTicks = aLengthInTicks;
    }
    
    @Override
    public Activity tick(EntityActor a) {
	if (isCancelled || !(a instanceof MobileEntity)) {
	    System.out.println("Drag is cancelled.");
	    return nextActivity;
	}
	
	MobileEntity me = (MobileEntity)a;
	
	//Pos currentPos = me.getPos();
	Pos nextPos;
	if (lengthInTicks > 1) { 
	    nextPos = PointsUtil.interpolatePos(start, end, ticks, lengthInTicks - 1);
	} else {
	    nextPos = end;
	}
	
	me.setPos(nextPos);
	
	if (++ticks >= lengthInTicks) {
	    me.isMovingToCell = false;
	    return this.nextActivity;
	}
	
	return this;
    }

}
