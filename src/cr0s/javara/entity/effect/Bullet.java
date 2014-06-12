package cr0s.javara.entity.effect;

import java.awt.Color;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.combat.Projectile;
import cr0s.javara.entity.actor.EntityActor;
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

    private Pos pos, target;
    private int length;
    private int facing;
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
	
	this.facing = RotationUtil.getRotationFromXY(this.pos.getX(), this.pos.getY(), this.target.getX(), this.target.getY());
	this.length = (int) Math.max(1, distanceToTarget);
	
	// TODO: anim work
	
	
	
    }

    @Override
    public void updateEntity(int delta) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void renderEntity(Graphics g) {
	// TODO Auto-generated method stub
	
    }
 
}
