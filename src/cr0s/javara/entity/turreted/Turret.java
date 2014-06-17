package cr0s.javara.entity.turreted;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.actor.activity.activities.Turn.RotationDirection;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public class Turret {
    private float offestX;
    private float offsetY;

    private float turretX, turretY;

    private EntityActor parentEntity;

    private SpriteSheet turretTexture;
    private int numFacings;
    private int startFrame;
    private int turretRotation, newTurretRotation;
    private boolean isTurretRotatingNow;
    private RotationDirection turretRotationDirection;

    private float targetX, targetY;
    private boolean isTargeting;

    private int currentRecoil = 0;
    private int maxRecoil = 0;                 // in pixels
    private final static int MAX_RECOIL_DEFAULT = 2; 

    private final static int RECOIL_INTERVAL_TICKS = 5;
    private int recoilTicks = 0;
    private int recoilOffsetX, recoilOffsetY;


    private int width, height;

    public Turret(EntityActor a, Pos parentOffset, SpriteSheet texture, int aStartFrame, int aNumFacings) {
	this(a, parentOffset, texture, aStartFrame, aNumFacings, MAX_RECOIL_DEFAULT);
    }

    public Turret(EntityActor a, Pos parentOffset, SpriteSheet texture, int aStartFrame, int aNumFacings, int aMaxRecoil) {
	this.parentEntity = a;

	this.offestX = parentOffset.getX();
	this.offsetY = parentOffset.getY();

	this.turretTexture = texture;
	this.startFrame = aStartFrame;
	this.numFacings = aNumFacings;

	this.maxRecoil = aMaxRecoil;
    }

    public void update(int delta) {	
	updateTurretPos();

	doTurretRotationTick();

	// Do a recoil
	if (this.maxRecoil != 0 && this.currentRecoil >= 0) {
	    if (++this.recoilTicks > this.RECOIL_INTERVAL_TICKS) {
		this.recoilTicks = 0;
	    }

	    Pos recoilOffset = getRecoilOffset();

	    this.recoilOffsetX = (int) (recoilOffset.getX() * this.currentRecoil);
	    this.recoilOffsetY = (int) (recoilOffset.getY() * this.currentRecoil);		    

	    this.currentRecoil--;
	}
    }

    public void setTurretSize(int w, int h) {
	this.width = w;
	this.height = h;
    }

    private void updateTurretPos() {
	if (this.parentEntity instanceof MobileEntity) {
	    this.turretX = ((MobileEntity) this.parentEntity).getTextureX() + this.offestX;
	    this.turretY = ((MobileEntity) this.parentEntity).getTextureY() + this.offsetY;
	} else {
	    this.turretX = this.parentEntity.posX + this.offestX;
	    this.turretY = this.parentEntity.posY + this.offsetY;
	}
	
	this.turretX += this.recoilOffsetX;
	this.turretY += this.recoilOffsetY;
    }

    private Pos getRecoilOffset() {
	float recoilX = 0;
	float recoilY = 0;

	Pos recoilVector = RotationUtil.facingToRecoilVector(this.turretRotation);
	recoilX = this.currentRecoil * recoilVector.getX();
	recoilY = this.currentRecoil * recoilVector.getY();

	return new Pos(recoilX, recoilY);
    }

    public void render(Graphics g) {
	updateTurretPos();

	this.turretTexture.renderInUse((int) turretX, (int) turretY, 0, this.startFrame + RotationUtil.quantizeFacings(this.turretRotation, this.numFacings));
    }

    /**
     * Do a rotation tick.
     * @return result of rotation. True - rotaton is finished. False - rotation in process.
     */
    private boolean doTurretRotationTick() {
	if (this.isTurretRotatingNow) {
	    if (this.getTurretRotation() == this.newTurretRotation) {
		this.isTurretRotatingNow = false;
		return true;
	    }

	    if (this.turretRotationDirection == RotationDirection.LEFT) {
		this.setTurretRotation((this.getTurretRotation() + 1) % this.numFacings);
	    } else if (this.turretRotationDirection == RotationDirection.RIGHT) {
		this.setTurretRotation((this.getTurretRotation() - 1) % this.numFacings);
	    }

	    return false;
	} else {
	    if (this.isTargeting) {
		int rot = 0;

		if (parentEntity instanceof MobileEntity) {
		    rot = RotationUtil.getRotationFromXY(((MobileEntity) parentEntity).getTextureX() + this.offestX, ((MobileEntity) parentEntity).getTextureY() + this.offsetY, this.targetX, this.targetY) % this.numFacings;
		} else {
		    rot = RotationUtil.getRotationFromXY(this.parentEntity.posX + this.offestX, this.parentEntity.posY + this.offsetY, this.targetX, this.targetY) % this.numFacings;		    
		}

		this.rotateTurretTo(rot);	

		this.isTargeting = false;
	    }
	}

	return true;
    }

    /**
     * Sets rotation to entity immediately.
     * @param rot
     */
    private void setTurretRotation(int rot) {
	if (rot < 0) { rot = 31; } 
	this.turretRotation = rot;
    }

    /**
     * Sets desired rotation and let entity rotate with some rotation speed to desired rotation;
     * @param rot desired rotation value
     */
    public void rotateTurretTo(int rot) {
	rot = rot % this.numFacings;

	this.newTurretRotation = rot;

	// Select nearest rotation direction
	if (getTurretRotation() >= 24 && rot <= 8) {
	    this.turretRotationDirection = RotationDirection.LEFT;
	} else if (getTurretRotation() <= 8 && rot >= 24) {
	    this.turretRotationDirection = RotationDirection.RIGHT;
	} else
	    if (getTurretRotation() < rot) {
		this.turretRotationDirection = RotationDirection.LEFT;
	    } else if (getTurretRotation() > rot){
		this.turretRotationDirection = RotationDirection.RIGHT;
	    } else {
		this.isTurretRotatingNow = false;
		return;
	    }

	this.isTurretRotatingNow = true;
    }

    private int getTurretRotation() {
	return this.turretRotation;
    }

    public void setTarget(Pos point) {
	this.isTargeting = true;

	this.targetX = point.getX();
	this.targetY = point.getY();
    }

    public boolean isTargeting() {
	return this.isTargeting;
    }

    public void recoil() {
	this.currentRecoil = this.maxRecoil;
    }

    public int getCurrentFacing() {
	return this.turretRotation;
    }

    public Pos getCenterPos() {
	return new Pos(this.turretX + this.width / 2, this.turretY + this.height / 2);
    }

    public boolean faceTarget(Pos centerPosition) {
	int facingToTarget = RotationUtil.getRotationFromXY(this.getCenterPos().getX(), this.getCenterPos().getY(), centerPosition.getX(), centerPosition.getY());
	//System.out.println("[FaceTarget] arg: " + centerPosition + " | self: " + this.getCenterPos() + " | facing: " + facingToTarget);
	setTarget(centerPosition);
	this.rotateTurretTo(facingToTarget);
	
	return this.turretRotation == facingToTarget;
    }
}
