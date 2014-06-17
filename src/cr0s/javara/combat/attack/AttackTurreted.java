package cr0s.javara.combat.attack;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.turreted.IHaveTurret;
import cr0s.javara.entity.turreted.Turret;
import cr0s.javara.order.Target;

public class AttackTurreted extends AttackFollow {
    
    public AttackTurreted(EntityActor s) {
	super(s);
    }

    @Override
    protected boolean canAttack(Target tgt) {
	if (!super.canAttack(tgt)) {
	    return false;
	}
	
	if (!(this.self instanceof IHaveTurret)) {
	    return false;
	}
	
	boolean canAttack = false;
	for (Turret t : ((IHaveTurret) this.self).getTurrets()) { 
	    if (t.faceTarget(tgt.centerPosition())) {
		canAttack = true;
	    }
	}
	
	return canAttack;
    }
}
