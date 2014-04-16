package cr0s.javara.entity.building;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.ISelectable;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class EntityBarracks extends EntityBuilding implements ISelectable {

	private SpriteSheet sheet;
	
	private int animIndex = 0;
	private int animDelayTicks = 0;
	private static final int ANIM_DELAY_TICKS = 2;
	
	private final String TEXTURE_NAME = "barr.shp";
	private final String MAKE_TEXTURE_NAME = "barrmake.shp";
	
	public static final int WIDTH_TILES = 2;
	public static final int HEIGHT_TILES = 2;
	
	public EntityBarracks(int tileX, int tileY, Team team, Player player) {
		super(tileX, tileY, team, player, WIDTH_TILES * 24, HEIGHT_TILES * 24);
		
		setBibType(BibType.SMALL);
		setProgressValue(-1);
		
		setMaxHp(50);
		setHp(getMaxHp());
		
		this.buildingSpeed = 6;
		this.makeTextureName = MAKE_TEXTURE_NAME;
		
		initTextures();
	}

	private void initTextures() {
		ShpTexture tex = ResourceManager.getInstance().getConquerTexture(TEXTURE_NAME);
		sheet = new SpriteSheet(tex.getAsCombinedImage(owner.playerColor), 48, 48);
	}
	
	@Override
	public void renderEntity(Graphics g) {
		float nx = posX;
		float ny = posY;
		
		int corruptionShift = 0;
		
		if (this.getHp() > this.getMaxHp() / 2) {
			corruptionShift = 0;
		} else {
			corruptionShift = 10;
		}
		
		sheet.startUse();
		sheet.getSubImage(0, corruptionShift + animIndex).drawEmbedded(posX, posY, this.getWidth(), this.getHeight());
		sheet.endUse();
		
		// Draw bounding box if debug mode is on
		if (Main.DEBUG_MODE) {
			g.setLineWidth(2);
			g.setColor(owner.playerColor);
			g.draw(boundingBox);
			g.setLineWidth(1);
		}
	}
	
	@Override
	public boolean shouldRenderedInPass(int passnum) {
		return passnum == 0;
	}

	@Override
	public void updateEntity(int delta) {
	    if (animDelayTicks++ > ANIM_DELAY_TICKS) {
		animDelayTicks = 0;
		
		this.animIndex = (this.animIndex + 1) % 10;
	    }
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
	public float getHeightInTiles() {
	    return this.tileHeight;
	}

	@Override
	public float getWidthInTiles() {
	    return this.tileWidth;
	}
}
