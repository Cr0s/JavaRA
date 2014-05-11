package cr0s.javara.order;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.main.CursorType;

public class MoveOrderTargeter extends OrderTargeter {

    public MoveOrderTargeter(EntityActor ent) {
	super("Move", 5, false, false, ent);
    }
    
    @Override
    public boolean canTarget(Entity self, Target target) {
	if (target == null || !target.isCellTarget() || self == null) {
	    return false;
	}
	
	return true;
    }

    @Override
    public CursorType getCursorForTarget(Entity self, Target target) {
	Point cellPos = target.getTargetCell();

	if (self == null || target == null || cellPos == null) {
	    return CursorType.CURSOR_POINTER;
	}
	
	return self.world.isCellPassable(cellPos) ? CursorType.CURSOR_GOTO : CursorType.CURSOR_NO_GOTO;
    }

}
