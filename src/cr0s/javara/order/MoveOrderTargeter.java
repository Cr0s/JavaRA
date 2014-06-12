package cr0s.javara.order;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.ui.cursor.CursorType;
import cr0s.javara.util.Pos;

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
	Pos cellPos = target.getTargetCell();

	if (self == null || target == null || cellPos == null) {
	    return CursorType.CURSOR_POINTER;
	}
	
	return ((MobileEntity) self).canEnterCell(cellPos) ? CursorType.CURSOR_GOTO : CursorType.CURSOR_NO_GOTO;
    }

}
