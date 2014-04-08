package cr0s.javara.entity.building;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class EntityConstructionYard extends EntityBuilding {

	private SpriteSheet sheet;
	
	private Image normal, corrupted;
	private final String TEXTURE_NAME = "fact.shp";
	
	public EntityConstructionYard(int tileX, int tileY, Team team, Player player) {
		super(tileX, tileY, team, player, 72, 72);
		
		maxHp = 100;
		hp = maxHp;
		initTextures();
	}

	private void initTextures() {
		ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);
		corrupted = tex.getAsImage(51, owner.playerColor).getImage();
		normal = tex.getAsImage(0, owner.playerColor).getImage();	
	}
	
	@Override
	public void renderEntity(Graphics g) {
		float nx = posX + 12f;
		float ny = posY + 12f;
		
		if (this.hp > this.maxHp / 2) {
			normal.draw(nx, ny);
		} else {
			corrupted.draw(nx, ny);
		}
		
		//g.setColor(Color.green);
		//g.draw(boundingBox);
	}
	
	@Override
	public boolean shouldRenderedInPass(int passnum) {
		return passnum == 0;
	}

	@Override
	public void updateEntity(int delta) {
		// TODO Auto-generated method stub
		
	}
}
