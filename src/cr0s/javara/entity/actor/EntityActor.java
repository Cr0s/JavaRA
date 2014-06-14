package cr0s.javara.entity.actor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import org.newdawn.slick.Graphics;

import cr0s.javara.combat.ArmorType;
import cr0s.javara.combat.TargetType;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.INotifySelected;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.order.IOrderIssuer;
import cr0s.javara.order.IOrderResolver;
import cr0s.javara.order.InputAttributes;
import cr0s.javara.order.Order;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.util.Pos;

public abstract class EntityActor extends Entity implements IOrderIssuer, IOrderResolver, INotifySelected {

    public Activity currentActivity;
    protected ArrayList<OrderTargeter> ordersList;
    protected HashMap<String, Integer[]> selectedSounds;
    
    protected int unitVersion = 0; // for same voice per unit
    public Alignment unitProductionAlingment = Alignment.NEUTRAL;
    
    public LinkedList<Class> requiredToBuild;
    
    public ArmorType armorType = ArmorType.NONE;
    public TreeSet<TargetType> targetTypes = new TreeSet<TargetType>();
    
    public EntityActor(float posX, float posY, Team team, Player owner,
	    final float aSizeWidth, final float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
	
	this.ordersList = new ArrayList<>();
	this.selectedSounds = new HashMap<>();
	
	requiredToBuild = new LinkedList<>();
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
    
    @Override
    public void notifySelected() {
    }
    
    public HashMap<String, Integer[]> getSounds() {
	return this.selectedSounds;
    }
    
    public String getSelectSound() {
	return "";
    }
    
    public void playSelectedSound() {	
    }

    public void playOrderSound() {
    }
    
    public EntityActor newInstance() {
	Constructor ctor;
	
	try {
	    ctor = (this.getClass()).getDeclaredConstructor(Float.class, Float.class, Team.class, Player.class);
	    ctor.setAccessible(true);
	    EntityActor newEntity = (EntityActor) ctor.newInstance(this.posX, this.posY, this.team, this.owner);

	    return newEntity;
	} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
		| IllegalArgumentException | InvocationTargetException e) {
	    e.printStackTrace();
	}

	
	return null;
    }

    public Pos getPosition() {
	return new Pos(this.boundingBox.getCenterX(), this.boundingBox.getCenterY(), this.posZ);
    }
    
    public Pos getCellPosition() {
	return getPosition().getCellPos();
    }    
}
