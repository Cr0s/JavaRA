package cr0s.javara.order;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.ui.cursor.CursorType;

public abstract class OrderTargeter {
    public String orderString;
    public int priority;
    public boolean canTargetAlly;
    public boolean canTargetEnemy;
    public EntityActor entity;
    
    public OrderTargeter(String order, int prior, boolean targetAlly, boolean targetEnemy, EntityActor ent) {
	this.orderString = order;
	this.priority = prior;
	this.canTargetAlly = targetAlly;
	this.canTargetEnemy = targetEnemy;
	this.entity = ent;
    }
    
    public abstract boolean canTarget(Entity self, Target target);
    public abstract CursorType getCursorForTarget(Entity self, Target target);
}
