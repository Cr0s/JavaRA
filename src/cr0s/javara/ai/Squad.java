package cr0s.javara.ai;

import java.util.ArrayList;

import cr0s.javara.ai.Squad.SquadType;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.order.Target;

public class Squad {
    public enum SquadType { ASSAULT, AIR, RUSH, PROTECTION };
    
    private Target target;
    private AIPlayer bot;
    private SquadType type;
    
    private ArrayList<EntityActor> units = new ArrayList<>();
    private StateMachine fsm;
    
    public Squad(AIPlayer bot, SquadType type) {
	this(bot, type, null);
    }
    
    public Squad(AIPlayer bot, SquadType type, EntityActor target) {
	this.target = new Target(target);
	
	this.bot = bot;
	this.type = type;
	
	this.fsm = new StateMachine();
    }

    public boolean isValid() {
	return !this.units.isEmpty();
    }

    public void removeDeadAndNotOwnUnits() {
	ArrayList<EntityActor> newUnits = new ArrayList<>();
	
	for (EntityActor a : this.units) {
	    if (!a.isDead() && a.owner == bot) {
		newUnits.add(a);
	    }
	}
	
	this.units = newUnits;
    }

    public SquadType getType() {
	return this.type;
    }

    public void update() {
	if (this.isValid()) {
	    this.fsm.update(this);
	}
    }

    public void addUnit(EntityActor a) {
	this.units.add(a);
    }

    public boolean targetIsValid() {
	return !this.units.isEmpty() && this.target.isValidFor(this.units.get(0));
    }

    public void setTarget(EntityActor tgt) {
	this.target = new Target(tgt);
    }
    
    public AIPlayer getBot() {
	return this.bot;
    }

    public ArrayList<EntityActor> getUnits() {
	return this.units;
    }
    
    public StateMachine getFsm() {
	return this.fsm;
    }
}
