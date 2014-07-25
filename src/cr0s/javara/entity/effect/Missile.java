package cr0s.javara.entity.effect;


import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import cr0s.javara.combat.Combat;
import cr0s.javara.combat.Projectile;
import cr0s.javara.combat.TargetType;
import cr0s.javara.combat.Weapon;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.order.Target;
import cr0s.javara.util.PointsUtil;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public class Missile extends Projectile {

    public int rot = 5;
    
    public float speed = 1;
    public int maximumPitch = 60;

    public String trail;

    public boolean jammable = false;
    
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

    private Pos targetPosition, offset;
    private Target guidedTarget;
    private int length;
    private int ticks, smokeTicks;

    public boolean turboBoost = false;

    private int enoughRange = 10;

    private TargetType boundToTerrainType;

    public int rangeLimit;
    
    public Missile(EntityActor srcActor, Pos srcPos, Pos passivePos,
	    EntityActor targetActor, int width, int height, int ang, String img, Weapon weap, float spd) {
	this(srcActor, srcPos, passivePos, targetActor, width, height, ang, img, weap, spd, 1, 5, 0);
    }
    public Missile(EntityActor srcActor, Pos srcPos, Pos passivePos,
	    EntityActor targetActor, int width, int height, int ang, String img, Weapon weap, float spd, int numFacings, int rot, float inaccuracy) {
	super(srcActor, srcPos, passivePos, targetActor, width, height);

	this.rot = rot;
	
	this.maximumPitch = ang;
	this.image = img;

	this.weapon = weap;
	this.speed = spd;

	this.pos = srcPos;
	this.targetPosition = passivePos;
	this.guidedTarget = new Target(targetActor, passivePos);
	
	this.inaccuracy = inaccuracy;
	
	if (this.inaccuracy > 0) {
	    float maxOffset = this.inaccuracy;

	    float newTargetX = PointsUtil.rangeFromPdf(srcActor.world.getRandom(), 2) * maxOffset;
	    float newTargetY = PointsUtil.rangeFromPdf(srcActor.world.getRandom(), 2) * maxOffset;
	    float newTargetZ = PointsUtil.rangeFromPdf(srcActor.world.getRandom(), 2) * maxOffset;

	    this.offset = new Pos(newTargetX, newTargetY, newTargetZ);
	} else {
	    this.offset = new Pos(0, 0, 0);
	}

	this.numFacings = numFacings;

	if (this.image != null) {
	    initTexture(this.image, this.numFacings, 0);
	}
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	this.ticks++;
	
	if (this.guidedTarget.isValidFor(this.sourceActor)) {
	    this.targetPosition = this.guidedTarget.centerPosition();
	}
	
	Pos dist = this.targetPosition.add(this.offset);
	int desiredFacing = RotationUtil.getRotationFromXY(this.pos.getX(), this.pos.getY(), dist.getX(), dist.getY() - dist.getZ());
	if (this.numFacings != 32) {
	    desiredFacing = RotationUtil.quantizeFacings(desiredFacing, this.numFacings);
	}
	
	float desiredAltitude = this.targetPosition.getZ();
	
	boolean jammed = false; // TODO: work with jams
	if (!this.guidedTarget.isValidFor(this.sourceActor)) {
	    desiredFacing = this.currentFacing;
	}
		
	this.currentFacing = RotationUtil.tickFacing(this.currentFacing, desiredFacing, this.rot);
	Pos move = new Pos(-1, -1, 0).rotate2D(RotationUtil.facingToAngle(this.currentFacing, this.numFacings)).mul(this.speed);
	
	if (this.targetPosition.getZ() > 0 && this.turboBoost) {
	    move = move.mul(1.5f);
	}
	
	if (this.pos.getZ() != desiredAltitude) {
	    float d = move.getHorizontalLength() * (float) Math.tan(this.maximumPitch);
	    float dz = Math.min(Math.max(-d, this.targetPosition.getZ() - this.pos.getZ()), d);
	    
	    move.setZ(move.getZ() + dz);
	}
	
	this.pos = this.pos.add(move);
	
	if (this.trail != null && --this.smokeTicks <= 0) {
	    Pos delayedPos = this.pos.sub(move.mul(1.5f));
	    
	    world.spawnSmokeAt(delayedPos, this.trail);
	    this.smokeTicks = this.trailInterval;
	}

	Pos cell = this.pos.getCellPos();
	// TODO: check for walls
	boolean shouldExplode = (this.pos.getZ() < 0)
		|| (dist.distanceToSq(this.pos) < this.enoughRange * this.enoughRange)
		|| (this.rangeLimit > 0 && this.ticks > this.rangeLimit) 
		|| (this.boundToTerrainType != null && this.world.getCellTargetType(cell) != this.boundToTerrainType);
	
	if (shouldExplode) {
	    explode();
	}	
    }


    @Override
    public void renderEntity(Graphics g) {
	super.renderEntity(g);
	
	float x = this.pos.getX();
	float y = this.pos.getY();
	
	float angle = RotationUtil.facingToAngle(this.currentFacing, this.numFacings);
	
	float xto = x - (float) Math.sin(angle) * 5;
	float yto = y - (float) Math.cos(angle) * 5;
	
	g.setColor(Color.red);
	g.drawLine(x, y, xto, yto);	
    }  
    
    private void explode() {
	this.setDead();

	Combat.doImpacts(this.pos, this.weapon, this.sourceActor, this.firepowerModifier);
    } 
}
