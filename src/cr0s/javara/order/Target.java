package cr0s.javara.order;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;

public class Target {
    private Entity targetEntity;
    private Point targetCell;
    
    public Target(Entity target) {
	this.targetEntity = target;
    }
    
    public Target (Point target) {
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
    
    public Point getTargetCell() {
	return this.targetCell;
    }
}
