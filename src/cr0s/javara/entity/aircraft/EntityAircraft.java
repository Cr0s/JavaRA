package cr0s.javara.entity.aircraft;

import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public abstract class EntityAircraft extends MobileEntity {

    public EntityAircraft(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
	// TODO Auto-generated constructor stub
    }

    @Override
    public Path findPathFromTo(MobileEntity e, int aGoalX, int aGoalY) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public float getTextureX() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public float getTextureY() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public boolean canEnterCell(Pos cellPos) {
	return this.world.getMap().isInMap(cellPos);
    }

    @Override
    protected Activity moveToRange(Pos cellPos, int range) {
	
	return null;
    }

    protected Pos flyStep(int facing) {
	return new Pos(-1, -1, this.getPosition().getZ()).rotate2D(RotationUtil.facingToAngle(facing, this.getMaxFacings())).mul(this.getMoveSpeed());
    }
    
    public boolean canLand(Pos cellPos) {
	if (!this.world.getMap().isInMap(cellPos)) {
	    return false;
	}
	
	if (!this.world.blockingEntityMap.isAnyUnitInCell(cellPos)) {
	    return false;
	}
	
	return true;
    }
}
