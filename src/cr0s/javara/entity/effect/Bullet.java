package cr0s.javara.entity.effect;

import java.awt.Color;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.combat.Combat;
import cr0s.javara.combat.Projectile;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.PointsUtil;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public class Bullet extends Projectile {

    public int speed = 1;
    public int speedMax;
    
    public int angle;
    public int angleMax;
    
    public String trail;
    
    public float inaccuracy = 0f;
    public String image;
    
    public boolean high = false;
    public boolean shadow = false;
    
    public int trailInterval = 2;
    public int trailDelay = 1;
    public int contrailLength = 0;
    
    public Color contrailColor = Color.white;
    public boolean contrailUsePlayerColor = false;
    public int contrailDelay = 1;

    private Pos target;
    private int length;
    private int ticks, smokeTicks;
    
    public Bullet(EntityActor srcActor, Pos srcPos, Pos passivePos,
	    EntityActor targetActor, int width, int height) {
	super(srcActor, srcPos, passivePos, targetActor, width, height);
	
	if (speedMax > 1) {
	    speed = srcActor.world.getRandomInt(speed, speedMax);
	}
	
	if (angleMax > 1) {
	    angle = srcActor.world.getRandomInt(angle, angleMax);
	}
	
	float distanceToTarget = (float) this.pos.distanceTo(this.target);
	
	target = this.passiveTargetPos;
	if (this.inaccuracy > 0) {
	    float maxOffset = this.inaccuracy * distanceToTarget / this.weapon.range;
	    
	    float newTargetX = PointsUtil.rangeFromPdf(srcActor.world.getRandom(), 2) * maxOffset;
	    float newTargetY = PointsUtil.rangeFromPdf(srcActor.world.getRandom(), 2) * maxOffset;
	    
	    this.target = new Pos(newTargetX, newTargetY);
	}
	
	this.currentFacing = RotationUtil.getRotationFromXY(this.pos.getX(), this.pos.getY(), this.target.getX(), this.target.getY());
	this.length = (int) Math.max(1, distanceToTarget);
	
	
	if (this.image != null) {
	    initTexture(this.image, 1, 0);
	}
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	
	this.pos = PointsUtil.lerpQuadratic(this.sourcePos, this.target, this.angle, this.ticks, this.length);
	updateFacing();
	
	if (this.trail != null && --this.smokeTicks <= 0) {
	    Pos delayedPos = PointsUtil.lerpQuadratic(this.sourcePos, this.target, this.angle, this.ticks - this.trailDelay, this.length);
	    world.spawnSmokeAt(delayedPos, this.trail);
	    
	    this.smokeTicks = this.trailInterval;
	    
	    // TODO: check for walls
	    if (this.ticks++ >= length) {
		explode();
	    }
	}
    }

    private void updateFacing() {
	if (this.numFacings <= 1) {
	    return;
	}
	
	float at = (float) this.ticks / (this.length - 1);
	float attitude = (float) (Math.tan(this.angle) * (1 - 2 * at) / (4 * 24));

	float u = (this.currentFacing % 16) / (this.numFacings * 1.0f);
	float scale = 12 * u * (1 - u);

	this.currentFacing = (int) (this.currentFacing < 16
		? this.currentFacing - scale * attitude
		: this.currentFacing + scale * attitude);	
    }
    
    private void explode() {
	this.setDead();
	
	Combat.doImpacts(this.pos, this.weapon, this.sourceActor, this.firepowerModifier);
    } 
}
