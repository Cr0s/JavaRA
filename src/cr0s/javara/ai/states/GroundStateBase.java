package cr0s.javara.ai.states;

import cr0s.javara.ai.Squad;
import cr0s.javara.ai.StateMachine.IState;
import cr0s.javara.entity.actor.EntityActor;

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
