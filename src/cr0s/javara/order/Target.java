package cr0s.javara.order;


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
    
    public Target(Entity te, Pos tpos) {
	if (te != null) {
	    this.targetEntity = te;
	} else {
	    this.targetCell = tpos;
	}
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

    public boolean isInRange(Pos actorCenter, float rangeInCells) {
	System.out.println("[Target] is in range: " + centerPosition().distanceTo(actorCenter) + " < " + rangeInCells * 24.0f);
	return centerPosition().distanceTo(actorCenter) < rangeInCells * 24.0f;
    }
    
    public Pos centerPosition() {
	Pos targetPos = null;
	
	if (this.isCellTarget()) {
	    targetPos = this.targetCell.getCellPos();
	    
	    return new Pos(targetPos.getX() * 24 + 12, targetPos.getY() * 24 + 12);
	} else {
	    if (this.targetEntity instanceof EntityActor) {
		return ((EntityActor) this.targetEntity).getPosition();
	    }
	}
	
	return null;
    }
    
    public boolean isValidFor(EntityActor self) {
	return (this.isEntityTarget() && !this.targetEntity.isDead()) || (this.isCellTarget() && self.world.getMap().isCellInMap(targetCell));
    }
}
