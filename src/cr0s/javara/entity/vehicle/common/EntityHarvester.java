package cr0s.javara.entity.vehicle.common;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.RotationUtil;

public class EntityHarvester extends EntityVehicle implements ISelectable, IShroudRevealer {

    private String TEXTURE_NAME = "harv.shp";
    private SpriteSheet texture;

    private final int ROTATION_START_TEXTURE_INDEX = 0;
    private final int ROTATION_END_TEXTURE_INDEX = 31;
    private final int MAX_ROTATION = 32;	
    private final int BUILD_ROTATION = 12;

    private final float MOVE_SPEED = 0.09f;

    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;
    private static final int SHROUD_REVEALING_RANGE = 5;
    
    private static final int WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS = 30;
    private static final int WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS = 10;

    private int updateTicks = 0;

    private int rotationDirection = 1;
    private boolean isHarvesting = false; // TODO: make harvesting animation

    public EntityHarvester(Float posX, Float posY, Team team, Player player) {
	super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);

	boundingBox.setBounds(posX + 6, posY - 6, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
	texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	Random r = new Random();

	this.setHp(50);
	this.setMaxHp(50);
	
	//this.buildingSpeed = 50;

	//setRotation(12);
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
	boundingBox.setBounds(posX + 6, posY - 6, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));

	//doRotationTick();	
    }

    @Override
    public void renderEntity(Graphics g) {
	super.renderEntity(g);
	
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
	texture.getSubImage(0, currentFacing).drawEmbedded(tx, ty, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	texture.endUse();

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
    public boolean moveTo(int tileX, int tileY) {
	super.moveTo(new Point(tileX / 24, tileY / 24));
	
	return true;
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

    @Override
    public Path findPathFromTo(MobileEntity e, int aGoalX, int aGoalY) {
	return world.getVehiclePathfinder().findPathFromTo(this, aGoalX, aGoalY);
    }
    
    @Override
    public int getMinimumEnoughRange() {
	return 3;
    }
    
    @Override
    public boolean canEnterCell(Point cellPos) {
	return world.isCellPassable((int) cellPos.getX(), (int) cellPos.getY());
    }

    @Override
    public int getWaitAverageTime() {
	return this.WAIT_FOR_BLOCKER_AVERAGE_TIME_TICKS;
    }

    @Override
    public int getWaitSpreadTime() {
	return this.WAIT_FOR_BLOCKER_TIME_SPREAD_TICKS;
    }
}
