package cr0s.javara.vehicle.tank;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.pathfinding.Mover;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.IMovable;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.entity.vehicle.EntityVehicle.RotationDirection;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.RotationUtil;

public class EntityHeavyTank extends EntityVehicle implements ISelectable, IMovable, Mover {

    private String TEXTURE_NAME = "3tnk.shp";
    private SpriteSheet texture;

    private final int ROTATION_START_TEXTURE_INDEX = 0;
    private final int ROTATION_END_TEXTURE_INDEX = 31;

    private final int MAX_ROTATION = 32;	

    private static final int TEXTURE_WIDTH = 36;
    private static final int TEXTURE_HEIGHT = 36;
    private static final int SHROUD_REVEALING_RANGE = 8;

    private int updateTicks = 0;

    private int turretRotation = 0;
    private RotationDirection turretRotationDirection;
    private boolean isTurretRotatingNow = false;
    private int newTurretRotation = 0;

    private Entity targetEntity = null;

    private final float MOVE_SPEED = 0.06f;

    private final float SHIFT = 12;

    public EntityHeavyTank(float posX, float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.isVisible = true;

	this.setHp(20);
	this.setMaxHp(20);
    }

    @Override
    public void updateEntity(int delta) {
	boundingBox.setBounds(posX + (TEXTURE_WIDTH / 4) - 6, posY + (TEXTURE_WIDTH / 4) - 12, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	doMoveTick(delta);
	doRotationTick();
	doTurretRotationTick();
    }

    @Override
    public void renderEntity(Graphics g) {
	if (Main.DEBUG_MODE) {
	    g.setLineWidth(1);
	    g.setColor(owner.playerColor);
	    g.draw(boundingBox);
	    //g.drawOval(posX - 1, posY - 1, this.boundingBox.getWidth() + 1, this.boundingBox.getHeight() + 1);
	}

	//g.drawRect(this.getTextureX(), this.getTextureY(), TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture.startUse();
	texture.getSubImage(0, rotation).drawEmbedded(this.getTextureX(), this.getTextureY(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	texture.getSubImage(0, 32 + turretRotation).drawEmbedded(this.getTextureX(), this.getTextureY(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	texture.endUse();

	//g.setColor(Color.white);
	//g.fillOval(this.getCenterPosX(), this.getCenterPosY(), 5, 5);
	//g.setColor(owner.playerColor);		

	drawPath(g);
    }

    /**
     * Do a rotation tick.
     * @return result of rotation. True - rotaton is finished. False - rotation in process.
     */
    public boolean doTurretRotationTick() {
	if (this.isTurretRotatingNow) {
	    if (this.getTurretRotation() == this.newTurretRotation) {
		this.isTurretRotatingNow = false;
		return true;
	    }

	    if (this.turretRotationDirection == RotationDirection.LEFT) {
		this.setTurretRotation((this.getTurretRotation() + 1) % maxRotation);
	    } else if (this.turretRotationDirection == RotationDirection.RIGHT) {
		this.setTurretRotation((this.getTurretRotation() - 1) % maxRotation);
	    }

	    return false;
	} else {
	    if (this.isMovingByPath) {
		int rot = RotationUtil.getRotationFromXY(this.getCenterPosX(), this.getCenterPosY(), this.goalX * 24, this.goalY * 24);
		this.rotateTurretTo(rot);	   		    
	    } else {
		if (this.targetEntity == null) { 
		    this.rotateTurretTo(this.getRotation());
		}
	    }
	}

	return true;
    }

    /**
     * Sets rotation to entity immediately.
     * @param rot
     */
    public void setTurretRotation(int rot) {
	if (rot < 0) { rot = 31; } 
	this.turretRotation = rot;
    }

    /**
     * Sets desired rotation and let entity rotate with some rotation speed to desired rotation;
     * @param rot desired rotation value
     */
    public void rotateTurretTo(int rot) {
	rot = rot % 32;

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

    /**
     * Returns rotation value
     * @return rotation value
     */
    public int getRotation() {
	return this.rotation;
    }


    @Override
    public void select() {
	this.isSelected = true;
    }

    @Override
    public void cancelSelect() {
	this.isSelected = false;
    }

    @Override
    public boolean isSelected() {
	return this.isSelected;
    }

    @Override
    public void moveTo(int tileX, int tileY) {
	// this.moveToAdjacentTile(((int) this.posX + 24) / 24, ((int) this.posY + 24) / 24);
	this.findPathAndMoveTo(tileX / 24, tileY / 24);
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }

    @Override
    public float getMoveSpeed() {
	return MOVE_SPEED;
    }

    @Override
    public float getTextureX() {
	return posX - (TEXTURE_WIDTH / 2) + 12;
    }

    @Override
    public float getTextureY() {
	return posY - (TEXTURE_HEIGHT / 2) + 6; 
    }

    @Override
    public int getRevealingRange() {
	return this.SHROUD_REVEALING_RANGE;
    }
}
