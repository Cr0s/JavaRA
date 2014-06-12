package cr0s.javara.render;

import org.newdawn.slick.Color;

import cr0s.javara.entity.actor.activity.activities.Turn;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.RotationUtil;

public class Sequence {
    private ShpTexture tex;
    private int start;
    private int facings;
    private int length;
    private int ticks;
    
    private int currentTicks;
    private int currentFrame;
    
    private boolean isFinished;
    private int currentFacing;
    
    private Color remapColor;
    protected boolean isLoop;
    
    public Sequence(ShpTexture t, int startIndex, int facingsCount, int len, int delayTicks, Color remap) {
	this.tex = t;
	this.start = startIndex;
	this.facings = facingsCount;
	
	if (this.facings == 0) {
	    this.facings = 1;
	}
	
	if (len > 0) {
	    this.length = len;
	} else {
	    this.length = 1;
	}
	
	this.ticks = delayTicks;
	
	this.remapColor = remap;
    }
    
    public void update(int facing) {
	this.currentFacing = facing;
	
	if (!isFinished && (ticks == 0 || --currentTicks <= 0)) {
	    this.currentTicks = this.ticks;
	    
	    if (this.length > 0) {
		if (this.currentFrame >= length) {
		    this.currentFrame = 0;
		    this.isFinished = !isLoop;
		    return;
		}
		
		this.currentFrame++;
	    } else {
		this.currentFrame = 0;
	    }
	}
    }
    
    public void render(float x, float y) {
	int f = 0;
	if (this.facings < 8) { 
	    f = RotationUtil.quantizeFacings(this.currentFacing, this.facings);
	} else {
	    f = this.currentFacing;
	}
	
	int i = this.start + (f * this.length) + (this.currentFrame % this.length);
	
	this.tex.getAsImage(i, this.remapColor).draw(x, y);
    }

    public boolean isFinished() {
	return this.isFinished;
    }
    
    public void reset() {
	this.currentFrame = 0;
	this.currentTicks = 0;
	
	this.isFinished = false;
    }
    
    public void setIsLoop(boolean loop) {
	this.isLoop = loop;
    }
}
