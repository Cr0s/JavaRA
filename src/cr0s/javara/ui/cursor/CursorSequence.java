package cr0s.javara.ui.cursor;

import org.newdawn.slick.Color;

import cr0s.javara.render.Sequence;
import cr0s.javara.resources.ShpTexture;

public class CursorSequence extends Sequence {

    public int offsetX, offsetY;
    
    private static final int DEFAULT_OFFSET_X = -16;
    private static final int DEFAULT_OFFSET_Y = -12;
    
    public CursorSequence(ShpTexture cursorTex, int startIndex) {
	this(cursorTex, startIndex, 0, 1, 1, null);	
    }
    
    public CursorSequence(ShpTexture cursorTex, int startIndex, int length) {
	this(cursorTex, startIndex, 0, length, 1, null);
    }
    
    public CursorSequence(ShpTexture cursorTex, int startIndex, int length, int oX, int oY) {
	this(cursorTex, startIndex, 0, length, 1, null);
	
	this.offsetX = oX;
	this.offsetY = oY;
    }
    
    private CursorSequence(ShpTexture t, int startIndex, int facingsCount,
	    int len, int delayTicks, Color remap) {
	super(t, startIndex, facingsCount, len, delayTicks, remap);
	
	this.offsetX = DEFAULT_OFFSET_X;
	this.offsetY = DEFAULT_OFFSET_Y;
	
	this.isLoop = true;
    }

}
