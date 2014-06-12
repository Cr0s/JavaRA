package cr0s.javara.entity.effect;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.Entity;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class MoveFlash extends Entity {

    private SpriteSheet tex;
    private int frameIndex = 0;
    private final int TICKS_PER_FRAME = 2;
    private final int MAX_FRAMES = 5;
    
    private int ticks = TICKS_PER_FRAME;
    
    public MoveFlash(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
	ShpTexture t = ResourceManager.getInstance().getTemplateShpTexture(Main.getInstance().getWorld().getMap().getTileSet().getSetName(), "moveflsh.tem");
	t.forcedColor = new Color(255, 255, 255, 128);
	
	tex = new SpriteSheet(t.getAsCombinedImage(null), 23, 23);
    }

    @Override
    public void updateEntity(int delta) {
	if (--ticks <= 0) {
	    this.ticks = TICKS_PER_FRAME;
	    
	    this.frameIndex++;
	    
	    if (this.frameIndex >= MAX_FRAMES) {
		this.frameIndex = 0;
		this.setDead();
	    }
	}
    }

    @Override
    public void renderEntity(Graphics g) {
	tex.getSubImage(0, frameIndex).draw(this.posX - 12, this.posY - 12);
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }

}
