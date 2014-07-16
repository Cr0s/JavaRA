package cr0s.javara.ai;

public class StateMachine {

    IState currentState;
    IState previousState;

    public StateMachine() {

    }

    public void update(Squad squad) {
	if (this.currentState != null) {
	    this.currentState.tick(squad);
	}
    }

    public void changeState(Squad squad, IState newState, boolean rememberPrevious) {
	if (rememberPrevious) {
	    this.previousState = this.currentState;
	}
	
	if (this.currentState != null) {
	    this.currentState.deactivate(squad);
	}

	if (newState != null) {
	    this.currentState = newState;
	}

	this.currentState.activate(squad);
    }
    
    public interface IState {
	void activate(Squad bot);
	void tick(Squad bot);
	void deactivate(Squad bot);
    }
}
