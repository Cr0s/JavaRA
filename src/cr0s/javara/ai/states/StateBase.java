package cr0s.javara.ai.states;

import java.util.ArrayList;

import cr0s.javara.ai.Squad;
import cr0s.javara.combat.Armament;
import cr0s.javara.combat.attack.ICanAttack;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Attack;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.main.Main;
import cr0s.javara.order.Order;
import cr0s.javara.util.Pos;

public abstract class StateBase {
    protected final static int DANGER_RADIUS = 10;
    
    protected static void goToRandomOwnBuilding (Squad s) {
	Pos loc = randomBuildingLocation(s);
	
	for (EntityActor a : s.getUnits()) {
	    a.resolveOrder(new Order("Move", a, loc));
	}
    }
    
    protected static Pos randomBuildingLocation(Squad s) {
	Pos location = s.getBot().getPlayerSpawnPoint();
	
	ArrayList<EntityBuilding> buildings = s.getBot().getBase().getBuildings();
	if (!buildings.isEmpty()) {
	    location = buildings.get(s.getBot().getRandom().nextInt(buildings.size())).getCellPosition();
	}
	
	return location;
    }
    
    protected static boolean busyAttack(EntityActor a) {
	if (a.isIdle()) {
	    return false;
	}
	
	Activity act = a.getCurrentActivity();
	if (act instanceof Attack) {
	    return true;
	}
	
	Activity next = act.nextActivity;
	if (next == null) {
	    return false;
	}
	
	if (next instanceof Attack) {
	    return true;
	}
	
	return false;
    }
    
    protected static boolean canAttackTarget(EntityActor a, EntityActor target) {
	if (!(a instanceof ICanAttack)) {
	    return false;
	}
	
	ArrayList<Armament> armas = ((ICanAttack) a).getAttackStrategy().armaments;
	for (Armament arma : armas) {
	    if (arma.getWeapon().isValidAgainst(target)) {
		return true;
	    }
	}
	
	return false;
    }
    
    protected static boolean shouldFlee(Squad squad) {
	if (!squad.isValid()) {
	    return false;
	}
	
	EntityActor u = squad.getUnits().get(squad.getBot().getRandom().nextInt(squad.getUnits().size()));
	ArrayList<EntityActor> units = Main.getInstance().getWorld().getActorsInCircle(u.getPosition(), DANGER_RADIUS);
	for (EntityActor a : units) {
	    if (!a.isDead() && (a instanceof EntityBuilding) && a.owner == squad.getBot()) {
		return false;
	    }
	}
	
	for (EntityActor a : units) {
	    if (!a.isDead() && a.owner.isEnemyFor(squad.getBot()) && (a instanceof ICanAttack)) {
		return true;
	    }
	}
	
	return false;
    }
}
