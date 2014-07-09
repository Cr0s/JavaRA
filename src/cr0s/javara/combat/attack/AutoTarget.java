package cr0s.javara.combat.attack;

import java.util.ArrayList;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.order.Target;

public class AutoTarget {
    public boolean allowMovement = true;
    
    public int scanRadius = -1;
    public UnitStance initialStance = UnitStance.ATTACK_ANYTHING;
    
    public int minimumScanInterval = 3;
    public int maximumScanInterval = 8;
    
    public boolean targetWhenIdle = true;
    public boolean targetWhenDamaged = true;
    public boolean enableStances = true;
    
    private AttackBase attack;
    private AttackTurreted at;
    
    private int nextScanTime = 0;
    
    public UnitStance stance;
    public EntityActor agressor;
    public EntityActor targetedActor;
    
    private EntityActor self;
    
    public AutoTarget(EntityActor self, AttackBase attack) {
	this.self = self;
	
	this.attack = attack;
	if (attack instanceof AttackTurreted) {
	    this.at = (AttackTurreted) attack;
	}
	
	this.stance = this.initialStance;
    }
    
    //public void damaged(AttackInfo e) {
	//
    //}
    
    public void update(int delta) {
	if (this.self.isIdle()) {
	    this.tickIdle();
	} else {
	   this.tick();
	}
    }

    private void tick() {
	if (this.nextScanTime > 0) {
	    --this.nextScanTime;
	}
    }

    private void tickIdle() {
	// We can't attack in current stance, so do nothing
	if (this.stance == UnitStance.DEFEND || !this.targetWhenIdle) {
	    return;
	}
	
	boolean allowMove = this.allowMovement && this.stance != UnitStance.DEFEND;
	if (this.at == null || this.at.target == null || !this.at.isReachableTarget(this.at.target, allowMove)) {
	    this.scanAndAttack();
	}
    }

    private void scanAndAttack() {
	EntityActor targetActor = this.scanForTarget(null);
	
	if (targetActor != null) {
	    this.attack(targetActor);
	}
    }

    private void attack(EntityActor targetActor) {
	this.targetedActor = targetActor;
	
	Target target = new Target(targetActor);
	this.attack.attackTarget(target, false, this.allowMovement && this.stance != UnitStance.DEFEND);
    }

    private EntityActor scanForTarget(EntityActor currentTarget) {
	float range = (this.scanRadius > 0) ? this.scanRadius : this.attack.getMaxRange();
    
	if (this.self.isIdle() || currentTarget == null || !new Target(currentTarget).isInRange(this.self.getPosition(), range)) {
	    if (--this.nextScanTime <= 0) {
		this.nextScanTime = 0;
		return this.chooseTarget(range);
	    }
	}
	
	return currentTarget;
    }
    
    private EntityActor chooseTarget(float rangeInCells) {
	this.nextScanTime = this.self.world.getRandomInt(this.minimumScanInterval, this.maximumScanInterval);
	ArrayList<EntityActor> actorsInRange = this.self.world.getActorsInCircle(this.self.getPosition(), rangeInCells * 24.0f);
	
	// Choose closest hostile actor that we can use our weapons against it
	EntityActor bestTarget = null;
	for (EntityActor e : actorsInRange) {
	    if (!e.isFrendlyTo(this.self) && this.attack.hasAnyValidWeapons(new Target(e))) { // Is this actor hostile and we can use our weapons against it?
		
		// Choose it as best if it closest to us or not was chosen before
		if (bestTarget == null || e.getPosition().distanceToSq(this.self.getPosition()) < bestTarget.getPosition().distanceToSq(this.self.getPosition())) {
		    bestTarget = e;
		}
	    }
	}
	
	return bestTarget;
    }
}
