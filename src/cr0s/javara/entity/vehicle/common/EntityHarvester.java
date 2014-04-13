package cr0s.javara.entity.vehicle.common;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.IDeployable;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.RotationUtil;

public class EntityHarvester extends EntityVehicle implements ISelectable {

	private String TEXTURE_NAME = "harv.shp";
	private SpriteSheet texture;
	
	private final int ROTATION_START_TEXTURE_INDEX = 0;
	private final int ROTATION_END_TEXTURE_INDEX = 31;
	private final int MAX_ROTATION = 32;	
	private final int BUILD_ROTATION = 12;
	
	private static final int TEXTURE_WIDTH = 48;
	private static final int TEXTURE_HEIGHT = 48;
		
	private int updateTicks = 0;
	
	private int rotationDirection = 1;
	private boolean isHarvesting = false; // TODO: make harvesting animation
	
	public EntityHarvester(float posX, float posY, Team team, Player player) {
		super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		
		texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
		Random r = new Random();
		
		this.setHp(50);
		this.setMaxHp(50);
		
		setRotation(12);
	}

	@Override
	public void updateEntity(int delta) {
		boundingBox.setBounds(posX, posY, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
		
		if (updateTicks++ < 2) {
		    return;
		}
		
		updateTicks = 0;
		
		doRotationTick();		
	}

	@Override
	public void renderEntity(Graphics g) {
		if (Main.DEBUG_MODE) {
			g.setLineWidth(1);
			g.setColor(owner.playerColor);
			//g.draw(boundingBox);
			g.drawOval(posX - 1, posY - 1, this.boundingBox.getWidth() + 1, this.boundingBox.getHeight() + 1);
		}
		
		texture.startUse();
		texture.getSubImage(0, rotation).drawEmbedded(posX - (TEXTURE_WIDTH / 4), posY - (TEXTURE_HEIGHT / 4), TEXTURE_WIDTH, TEXTURE_HEIGHT);
		texture.endUse();
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
	    int rot = RotationUtil.getRotationFromXY(posX, posY, tileX, tileY);
	    this.rotateTo(rot);
	}

	@Override
	public boolean shouldRenderedInPass(int passNum) {
	    return passNum == 1;
	}
}
