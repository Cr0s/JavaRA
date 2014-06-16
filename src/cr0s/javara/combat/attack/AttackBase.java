package cr0s.javara.combat.attack;

import java.util.ArrayList;

import cr0s.javara.combat.Armament;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.order.IOrderIssuer;
import cr0s.javara.order.IOrderResolver;
import cr0s.javara.order.Target;

public abstract class AttackBase implements IOrderResolver, IOrderIssuer {
    public boolean isAttackings;
    
    public ArrayList<Armament> armaments;
    private EntityActor self;
    
    private int facing;
    
    public AttackBase(EntityActor s) {
	this.self = s;
	
	this.armaments = new ArrayList<Armament>();
    }
    
    public void addArmament(Armament arma) {
	this.armaments.add(arma);
    }
    
    protected boolean canAttack(Target tgt) {
	if (this.self.world == null || this.self.isDead()) {
	    return false;
	}
		
	for (Armament arma : this.armaments) {
	    if (!arma.isReloading()) {
		return true;
	    }
	}
	
	return false;
    }
    
    public void doAttack(Target tgt) {
	if (!canAttack(tgt)) {
	    return;
	}
	
	for (Armament arma : this.armaments) {
	    arma.checkFire(facing, tgt);
	}
    }
}
