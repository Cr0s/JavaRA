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

public class SequencePlayer extends Entity implements IEffect {

    private Sequence seq;
    private int frameIndex = 0;
    private final int TICKS_PER_FRAME = 1;
    private int maxFrames;
    
    private int ticks = TICKS_PER_FRAME;
    
    private int width, height;
    
    public SequencePlayer(Pos pos, Sequence seq) {
	this(pos.getX(), pos.getY(), null, null, seq.getTexture().width, seq.getTexture().height);

	this.seq = seq;
	
	if (seq.isFinished()) {
	    this.setDead();
	}
    }
    
    public SequencePlayer(float posX, float posY, Team team, Player owner,
	    float aSizeWidth, float aSizeHeight) {
	super(posX, posY, team, owner, aSizeWidth, aSizeHeight);
    }

    @Override
    public void updateEntity(int delta) {
	if (this.seq.isFinished()) {
	    this.setDead();
	}
	
	this.seq.update(this.currentFacing);
    }

    @Override
    public void renderEntity(Graphics g) {
	if (!this.seq.isFinished()) {
	    this.seq.render(this.posX, this.posY);
	}
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return passNum == 1;
    }

}
