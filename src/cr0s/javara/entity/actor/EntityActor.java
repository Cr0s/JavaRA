package cr0s.javara.entity.actor;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.order.IOrderIssuer;
import cr0s.javara.order.IOrderResolver;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;

public abstract class EntityActor extends Entity implements IOrderIssuer, IOrderResolver {

    public Activity currentActivity;
    protected ArrayList<OrderTargeter> ordersList;
    
    public EntityActor(float posX, float posY, Team team, Player owner,
	    final float aSizeWidth, final float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
	
	this.ordersList = new ArrayList<>();
    }

    @Override
    public void updateEntity(final int delta) {
	if (this.currentActivity != null) {
	    this.currentActivity = this.currentActivity.tick(this);
	}
    }

    public void queueActivity(Activity a) {
	if (this.currentActivity != null) {
	    this.currentActivity.queueActivity(a);
	} else {
	    this.currentActivity = a;
	}
    }
    
    public void cancelActivity() {
	if (this.currentActivity != null) {
	    this.currentActivity.cancel();
	} 	
    }
    
    @Override
    public void renderEntity(final Graphics g) {
    }

    @Override
    public boolean shouldRenderedInPass(final int passNum) {
	return false;
    }

    public boolean isFrendlyTo(EntityActor other) {
	// TODO: add ally logic
	if (this.owner == other.owner) {
	    return true;
	}
	
	return false;
    }
    
    public boolean isIdle() {
	return this.currentActivity == null;
    }

    @Override
    public abstract void resolveOrder(Order order);

    @Override
    public ArrayList<OrderTargeter> getOrders() {
	return this.ordersList;
    }

    @Override
    public abstract Order issueOrder(Entity self, OrderTargeter targeter, Target target, InputAttributes ia);
}
