package cr0s.javara.entity.vehicle.common;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;

public class EntityMcv extends EntityVehicle implements ISelectable {

	private String TEXTURE_NAME = "mcv.shp";
	private SpriteSheet texture;
	
	private final int ROTATION_START_TEXTURE_INDEX = 0;
	private final int ROTATION_END_TEXTURE_INDEX = 31;
	private final int MAX_ROTATION = 32;	
	
	private static final int TEXTURE_WIDTH = 48;
	private static final int TEXTURE_HEIGHT = 48;
		
	private int updateTicks = 0;
	
	public EntityMcv(float posX, float posY, Team team, Player player) {
		super(posX, posY, team, player, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		
		texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	@Override
	public void updateEntity(int delta) {
		boundingBox.setBounds(posX, posY, (TEXTURE_WIDTH / 2), (TEXTURE_HEIGHT / 2));
		
		this.setRotation((this.rotation + 1) % MAX_ROTATION);
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
}
