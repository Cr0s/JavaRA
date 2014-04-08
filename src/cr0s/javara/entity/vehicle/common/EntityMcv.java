package cr0s.javara.entity.vehicle.common;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.resources.ResourceManager;

public class EntityMcv extends EntityVehicle {

	private String TEXTURE_NAME = "mcv.shp";
	private SpriteSheet texture;
	
	private final int ROTATION_START_TEXTURE_INDEX = 0;
	private final int ROTATION_END_TEXTURE_INDEX = 31;
		
	public EntityMcv(int tileX, int tileY, Team team, Player player,
			int sizeWidth, int sizeHeight) {
		super(tileX, tileY, team, player, sizeWidth, sizeHeight);
		
		texture = new SpriteSheet(ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME).getAsCombinedImage(owner.playerColor).getImage(), sizeWidth, sizeHeight);
	}

	@Override
	public void updateEntity(int delta) {

	}

	@Override
	public void renderEntity(Graphics g) {
		
	}
}
