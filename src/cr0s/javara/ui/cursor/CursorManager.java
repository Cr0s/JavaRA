package cr0s.javara.ui.cursor;

import org.newdawn.slick.Graphics;

import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class CursorManager {
    private CursorType currentCursor;
      
    private CursorSequence currentCursorSequence;
    
    private CursorSequence pointerCursor, selectCursor, gotoCursor, noGotoCursor, deployCursor, noDeployCursor, sellCursor, noSellCursor, repairCursor, noRepairCursor, attackCursor, enterCursor, noEnterCursor, goldRepairCursor, noGoldRepairCursor, cursorAtomBomb, cursorHarvest;
    
    private static CursorManager instance;
    
    private ShpTexture cursorTexture;

    private int currentX;

    private int currentY;
    
    private CursorManager() {
	loadCursors();
    }
    
    public static CursorManager getInstance() {
	if (instance == null) {
	    instance = new CursorManager();
	}
	
	return instance;
    }
    
    private void loadCursors() {
	this.cursorTexture = ResourceManager.getInstance().getShpTexture("mouse.shp");
	
	this.pointerCursor = new CursorSequence(this.cursorTexture, 0, 1, 0, 0);
	this.selectCursor = new CursorSequence(this.cursorTexture, 15, 6);
	this.gotoCursor =  new CursorSequence(this.cursorTexture, 10, 4);
	this.noGotoCursor = new CursorSequence(this.cursorTexture, 14);
	this.deployCursor = new CursorSequence(this.cursorTexture, 59, 9);
	this.noDeployCursor = new CursorSequence(this.cursorTexture, 211);
	this.sellCursor = new CursorSequence(this.cursorTexture, 68, 12);
	this.noSellCursor = new CursorSequence(this.cursorTexture, 119);
	this.repairCursor = new CursorSequence(this.cursorTexture, 35, 24);
	this.noRepairCursor = new CursorSequence(this.cursorTexture, 120);
	this.attackCursor = new CursorSequence(this.cursorTexture, 195, 8);
	this.enterCursor = new CursorSequence(this.cursorTexture, 113, 3);
	this.noEnterCursor = new CursorSequence(this.cursorTexture, 212);
	this.goldRepairCursor = new CursorSequence(this.cursorTexture, 170, 24);
	this.noGoldRepairCursor = new CursorSequence(this.cursorTexture, 213);
	this.cursorAtomBomb = new CursorSequence(this.cursorTexture, 90, 7);
	this.cursorHarvest = new CursorSequence(this.cursorTexture, 21, 8);
    }

    public CursorType getCursor() {
	return this.currentCursor;
    }

    public void setCursorType(CursorType cursor) {
	if (this.currentCursor == cursor) {
	    return;
	}
	
	try {
	    switch (cursor) {
	    case CURSOR_POINTER:
		this.currentCursorSequence = this.pointerCursor;
		break;

	    case CURSOR_SELECT:
		this.currentCursorSequence = this.selectCursor;
		break;

	    case CURSOR_GOTO:
		this.currentCursorSequence = this.gotoCursor;
		break;

	    case CURSOR_NO_GOTO:
		this.currentCursorSequence = this.noGotoCursor;
		break;

	    case CURSOR_DEPLOY:
		this.currentCursorSequence = this.deployCursor;
		break;

	    case CURSOR_NO_DEPLOY:
		this.currentCursorSequence = this.noDeployCursor;
		break;

	    case CURSOR_ENTER:
		this.currentCursorSequence = this.enterCursor;
		break;
		
	    case CURSOR_NO_ENTER:
		this.currentCursorSequence = this.noEnterCursor;
		break;
		
	    case CURSOR_ATTACK:
		this.currentCursorSequence = this.attackCursor;
		break;
		
	    case CURSOR_HARVEST:
		this.currentCursorSequence = this.cursorHarvest;
		break;
		
	    default:
		break;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	this.currentCursor = cursor;
    }
    
    public void update() {
	int x = Main.getInstance().getContainer().getInput().getMouseX();
	int y = Main.getInstance().getContainer().getInput().getMouseY();
	
	this.currentX = x;
	this.currentY = y;
	
	if (this.currentCursorSequence != null) {
	    this.currentCursorSequence.update(0);
	}
    }
    
    public void drawCursor(Graphics g) {
	if (this.currentCursorSequence != null) {
	    this.currentCursorSequence.render(this.currentX + this.currentCursorSequence.offsetX, this.currentY + this.currentCursorSequence.offsetY);
	}
    }
}
