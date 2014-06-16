package cr0s.javara.order;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.util.Pos;

public class Target {
    private Entity targetEntity;
    private Pos targetCell;
    
    public Target(Entity target) {
	this.targetEntity = target;
    }
    
    public Target (Pos target) {
	this.targetCell = target;
    }
    
    public boolean isCellTarget() {
	return this.targetCell != null;
    }
    
    public boolean isEntityTarget() {
	return this.targetEntity != null;
    }
    
    public Entity getTargetEntity() {
	return this.targetEntity;
    }
    
    public Pos getTargetCell() {
	return this.targetCell;
    }

    public boolean isInRange(Pos actorCenter, float range) {
	Pos targetPos = null;
	
	if (this.isCellTarget()) {
	    targetPos = this.targetCell;
	} else {
	    if (this.targetEntity instanceof EntityActor) {
		targetPos = ((EntityActor) this.targetEntity).getCellPosition();
	    }
	}
	
	if (targetPos == null) {
	    return false;
	}
	
	return targetPos.distanceTo(actorCenter.getCellPos()) <= range;
    }
    
    public Pos centerPosition() {
	Pos targetPos = null;
	
	if (this.isCellTarget()) {
	    targetPos = this.targetCell;
	} else {
	    if (this.targetEntity instanceof EntityActor) {
		targetPos = ((EntityActor) targetEntity).getCellPosition();
	    }
	}
	
	return new Pos(targetPos.getX() * 24 + 12, targetPos.getY() * 24 + 12);
    }
}
