package cr0s.javara.entity.vehicle.common;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.RotationUtil;

public class EntityMcv extends EntityVehicle implements ISelectable, IDeployable {

    private String TEXTURE_NAME = "mcv.shp";
    private SpriteSheet texture;

    private final int ROTATION_START_TEXTURE_INDEX = 0;
    private final int ROTATION_END_TEXTURE_INDEX = 31;
    private final int MAX_ROTATION = 32;	
    private final int BUILD_ROTATION = 12;

    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;
    private static final int SHROUD_REVEALING_RANGE = 5;

    private int updateTicks = 0;

    private int rotationDirection = 1;
    private boolean isDeploying = false;

    private final float MOVE_SPEED = 0.03f;

    public EntityMcv(float posX, float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	boundingBox.setBounds(posX, posY, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	boundingBox.setCenterX(this.getCenterPosX());
	boundingBox.setCenterY(this.getCenterPosY());
	
	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.setHp(50);
	this.setMaxHp(50);

	this.rotation = r.nextInt(32);
    }

    @Override
    public void updateEntity(int delta) {
	boundingBox.setBounds(posX, posY - 6, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	boundingBox.setCenterX(this.getCenterPosX());
	boundingBox.setCenterY(this.getCenterPosY());
	
	doRotationTick();
	this.doMoveTick(delta);

	if (isDeploying && this.canDeploy()) {
	    if (this.rotation == BUILD_ROTATION) {
		EntityConstructionYard cy = new EntityConstructionYard((int) posX - (EntityConstructionYard.WIDTH_TILES / 2 * 24), (int) posY - (EntityConstructionYard.HEIGHT_TILES / 2 * 24), this.team, this.owner);
		cy.isVisible = true;
		cy.isSelected = true;
		world.addBuildingTo(cy);

		setDead();
		return;
	    }
	}
    }

    @Override
    public void renderEntity(Graphics g) {
	if (Main.DEBUG_MODE) {
	    g.setLineWidth(1);
	    g.setColor(owner.playerColor);
	    g.draw(boundingBox);
	    //g.drawOval(posX - 1, posY - 1, this.boundingBox.getWidth() + 1, this.boundingBox.getHeight() + 1);
	}

	float tx = this.getTextureX();
	float ty = this.getTextureY();

	//g.drawRect(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	texture.startUse();
	texture.getSubImage(0, rotation).drawEmbedded(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	texture.endUse();
	
	//g.setColor(Color.white);
	//g.fillOval(this.getCenterPosX(), this.getCenterPosY(), 5, 5);
	//g.setColor(owner.playerColor);		

	drawPath(g);
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
    public boolean canDeploy() {
	// Check deploy possibility via World blockingMap
	return true;
    }

    @Override
    public void deploy() {
	if (canDeploy()) { 
	    deployConstructionYard();
	}
    }

    private void deployConstructionYard() {
	this.isDeploying = true;

	this.rotateTo(this.BUILD_ROTATION);
    }

    @Override
    public void moveTo(int tileX, int tileY) {
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
	return posX - (TEXTURE_WIDTH / 2) + 18;
    }

    @Override
    public float getTextureY() {
	return posY - (TEXTURE_HEIGHT / 2) + 12; 
    }
    
    @Override
    public int getRevealingRange() {
	return this.SHROUD_REVEALING_RANGE;
    }    
}
