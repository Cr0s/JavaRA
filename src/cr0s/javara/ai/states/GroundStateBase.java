package cr0s.javara.ai.states;

import cr0s.javara.ai.Squad;
import cr0s.javara.ai.StateMachine.IState;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.util.Pos;

public abstract class GroundStateBase extends StateBase {

}

class GroundUnitsIdleState extends GroundStateBase implements IState {

    @Override
    public void activate(Squad bot) {
	// TODO Auto-generated method stub

    }

    @Override
    public void tick(Squad owner) {
	if (!owner.isValid()) {
	    return;
	}

	if (!owner.targetIsValid()) {
	    if (!owner.getUnits().isEmpty()) {
		EntityActor t = owner.getBot().findClosestEnemy(owner.getUnits().get(0).getPosition());
		if (t == null) {
		    return;
		}

		owner.setTarget(t);
	    }
	}


    }

    @Override
    public void deactivate(Squad bot) {
	// TODO Auto-generated method stub

    }

}

class GroundUnitsAttackMoveState extends GroundStateBase implements IState {

    @Override
    public void activate(Squad bot) {

    }

    @Override
    public void tick(Squad owner) {
	if (!owner.isValid()) {
	    return;
	}

	if (!owner.targetIsValid()) {
	    EntityActor randomUnit = owner.getUnits().get(owner.getBot().getRandom().nextInt(owner.getUnits().size()));
	    EntityActor closestEnemy = owner.getBot().findClosestEnemy(randomUnit.getCellPosition());

	    if (closestEnemy != null) {
		owner.setTarget(closestEnemy);
	    } else {
		// TODO: owner.getFsm().changeState(owner, new GroundUnitsFleeState(), true);
		
		return;
	    }
	}
	
	// TODO: finish this
	//EntityActor leader = 
    }

    @Override
    public void deactivate(Squad bot) {

    }

}
