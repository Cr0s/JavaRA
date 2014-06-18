package cr0s.javara.combat.attack;

import cr0s.javara.combat.Armament;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Attack;
import cr0s.javara.entity.turreted.IHaveTurret;
import cr0s.javara.entity.turreted.Turret;
import cr0s.javara.order.Target;
import cr0s.javara.util.RotationUtil;

public class AttackFrontal extends AttackBase {
    
    private final float ANGLE_TOLERANCE = 11.25f;
    
    public AttackFrontal(EntityActor s) {
	super(s);
    }

    @Override
    protected boolean canAttack(Target tgt) {
	if (!super.canAttack(tgt)) {
	    return false;
	}

	int facingToTarget = RotationUtil.getRotationFromXY(self.getPosition().getX(), self.getPosition().getY(), tgt.centerPosition().getX(), tgt.centerPosition().getY());
	float angle = RotationUtil.facingToAngle(facingToTarget);
	
	if (Math.abs(RotationUtil.facingToAngle(this.self.currentFacing) - angle) % 360 > ANGLE_TOLERANCE) {
	    return false;
	}
	
	return true;
    }

    @Override
    public Activity getAttackActivity(Target tgt, boolean allowMove) {
	Armament arma = this.chooseArmamentForTarget(tgt);
	if (arma == null) {
	    return null;
	}
	
	return new Attack(this, this.self, tgt, arma.getWeapon().range, allowMove);
    }
}
