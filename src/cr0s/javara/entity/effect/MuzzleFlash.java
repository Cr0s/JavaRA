package cr0s.javara.entity.effect;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IEffect;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.render.Sequence;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.Pos;

public class MuzzleFlash extends Entity implements IEffect {

    private SpriteSheet tex;
    private int frameIndex = 0;
    private final int TICKS_PER_FRAME = 1;
    private int maxFrames;
    private int numFacings;
    private int facing;
    
    private int ticks = TICKS_PER_FRAME;
    
    private int width, height;
    
    private Sequence seq;
    
    public MuzzleFlash(Pos pos, String texture, int fac, int numFacings, int length) {
	this(pos.getX(), pos.getY(), null, null, 24, 24);
	
	ShpTexture t = ResourceManager.getInstance().getConquerTexture(texture);
	this.width = t.width;
	this.height= t.height;
	
	seq = new Sequence(t, 0, numFacings, length, 1, null);
	this.facing = fac;
    }
    
    public MuzzleFlash(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
    }

    @Override
    public void updateEntity(int delta) {
	if (!seq.isFinished()) {
	    seq.update(this.currentFacing);
	} else {
	    this.setDead();
	}
    }

    @Override
    public void renderEntity(Graphics g) {
	this.seq.render(this.posX - this.width / 2, this.posY - this.height / 2);
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }

}
