package cr0s.javara.combat;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IEffect;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.util.Pos;

public abstract class Projectile extends Entity implements IEffect {    
    public Weapon weapon;
    public float firepowerModifier = 1.0f;
    public int facing = 0;
    
    public Pos sourcePos;
    public EntityActor sourceActor;
    
    public Pos passiveTargetPos;
    public EntityActor guidedTarget;
    
    private Projectile(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
    }

    public Projectile(Pos srcPos, Pos passivePos, EntityActor targetActor, int width, int height) {
	super(srcPos.getX(), srcPos.getY(), null, null, width, height);
	
	this.sourcePos = srcPos;
	this.passiveTargetPos = passivePos;
	this.guidedTarget = targetActor;
    }

    public Projectile(EntityActor srcActor, Pos srcPos, Pos passivePos, EntityActor targetActor, int width, int height) {
	super(srcPos.getX(), srcPos.getY(), srcActor.team, srcActor.owner, width, height);
	
	this.sourceActor = srcActor;
    }
        
    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }    
}
