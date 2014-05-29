package cr0s.javara.entity.turreted;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.actor.activity.activities.Turn.RotationDirection;
import cr0s.javara.util.RotationUtil;

public class Turret {
    private float offestX;
    private float offsetY;
    
    private EntityActor parentEntity;
    
    private SpriteSheet turretTexture;
    private int numFacings;
    private int startFrame;
    private int turretRotation, newTurretRotation;
    private boolean isTurretRotatingNow;
    private RotationDirection turretRotationDirection;
    
    private float targetX, targetY;
    private boolean isTargeting;
    
    public Turret(EntityActor a, Point parentOffset, SpriteSheet texture, int aStartFrame, int aNumFacings) {
	this.parentEntity = a;
	
	this.offestX = parentOffset.getX();
	this.offsetY = parentOffset.getY();
	
	this.turretTexture = texture;
	this.startFrame = aStartFrame;
	this.numFacings = aNumFacings;
    }
    
    public void update(int delta) {
	doTurretRotationTick();
    }
    
    public void render(Graphics g) {
	float x = 0f, y = 0f;
	
	if (this.parentEntity instanceof MobileEntity) {
	    x = ((MobileEntity) this.parentEntity).getTextureX() + this.offestX;
	    y = ((MobileEntity) this.parentEntity).getTextureY() + this.offsetY;
	} else {
	    x = this.parentEntity.posX + this.offestX;
	    y = this.parentEntity.posY + this.offsetY;		    
	}	
	
	this.turretTexture.renderInUse((int) x, (int) y, 0, this.startFrame + RotationUtil.quantizeFacings(this.turretRotation, this.numFacings));
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

    public void setTarget(Point point) {
	this.isTargeting = true;
	
	this.targetX = point.getX();
	this.targetY = point.getY();
    }
    
    public boolean isTargeting() {
	return this.isTargeting;
    }
}
