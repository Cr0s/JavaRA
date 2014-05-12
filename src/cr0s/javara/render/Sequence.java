package cr0s.javara.render;

import org.newdawn.slick.Color;

import cr0s.javara.resources.ShpTexture;

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
    
    public Sequence(ShpTexture t, int startIndex, int facingsCount, int len, int delayTicks, Color remap) {
	this.tex = t;
	this.start = startIndex;
	this.facings = facingsCount;
	this.length = len;
	this.ticks = delayTicks;
	
	this.remapColor = remap;
    }
    
    public void update(int facing) {
	this.currentFacing = facing;
	
	if (!isFinished && (ticks == 0 || --currentTicks <= 0)) {
	    this.currentTicks = this.ticks;
	    
	    if (this.length > 0) {
		this.currentFrame++;

		if (this.currentFrame >= length) {
		    this.isFinished = true;
		}
	    }
	}
	
	
    }
    
    public void render(float x, float y) {
	this.tex.getAsImage(this.start + (this.length * this.currentFacing) + this.currentFrame, remapColor).draw(x, y);
    }

    public boolean isFinished() {
	return this.isFinished;
    }
    
    public void reset() {
	this.currentFrame = 0;
	this.currentTicks = 0;
	
	this.isFinished = false;
    }
}
