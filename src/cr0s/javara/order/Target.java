package cr0s.javara.order;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
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
}
