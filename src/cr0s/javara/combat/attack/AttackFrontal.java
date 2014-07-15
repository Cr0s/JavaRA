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
    
    private static final float RANGE_TOLERANCE = 1.0f;
    private final float ANGLE_TOLERANCE = 1f;
    
    public AttackFrontal(EntityActor s) {
	super(s);
    }

    @Override
    protected boolean canAttack(Target tgt) {
	if (!super.canAttack(tgt)) {
	    return false;
	}

	int facingToTarget = RotationUtil.getRotationFromXY(self.getPosition().getX(), self.getPosition().getY(), tgt.centerPosition().getX(), tgt.centerPosition().getY());
	if (this.self.getMaxFacings() != 32) {
	    facingToTarget = RotationUtil.quantizeFacings(facingToTarget, this.self.getMaxFacings());
	}
	float angle = RotationUtil.facingToAngle(facingToTarget, this.self.getMaxFacings());
	
	if (Math.abs(RotationUtil.facingToAngle(this.self.currentFacing, this.self.getMaxFacings()) - angle) % 360 > ANGLE_TOLERANCE) {
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
	
	return new Attack(this, this.self, tgt, arma.getWeapon().range - this.RANGE_TOLERANCE, allowMove);
    }
}
