package cr0s.javara.entity.actor.activity.activities;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.building.EntityBuilding;

public class Move extends Activity {

    private Point destCell;
    private int destRange;
    private EntityBuilding ignoreBuilding;
    
    private Path currentPath;
    private int currentPathIndex;
    
    public Move(MobileEntity me, Point destination) {
	this.destCell = destination;
	this.destRange = 0;
	
	this.currentPath = me.findPathFromTo(me, (int) (destination.getX() / 24), (int) (destination.getY() / 24));
    }
    
    public Move(MobileEntity me, Point destination, int enoughRange) {
	this.destCell = destination;
	this.destRange = enoughRange;
	
	this.currentPath = me.findPathFromTo(me, (int) (destination.getX() / 24), (int) (destination.getY() / 24));
    }
    
    private Point popPath() {
	int px = 0, py = 0;
	
	// TODO: add pathfinder and deal with blockers logic
	
	return new Point(px, py);
    }
    
    @Override
    public Activity tick(EntityActor a) {
	// TODO Auto-generated method stub
	return null;
    }

    public void setIgnoreBuilding(EntityBuilding eb) {
	this.ignoreBuilding = eb;
    }
}
