package cr0s.javara.ai;

import cr0s.javara.entity.actor.EntityActor;

public class Squad {
    public enum SquadType { ASSAULT, AIR, RUSH, PROTECTION };
    
    private EntityActor target;
    private AIPlayer bot;
    private SquadType type;
    
    public Squad(AIPlayer bot, SquadType type) {
	this(bot, type, null);
    }
    
    public Squad(AIPlayer bot, SquadType type, EntityActor target) {
	this.target = target;
	
	this.bot = bot;
	this.type = type;
    }
}
