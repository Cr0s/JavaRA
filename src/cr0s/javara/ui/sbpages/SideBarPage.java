package cr0s.javara.ui.sbpages;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.main.Main;

public abstract class SideBarPage {
    private ArrayList<SideBarItemsButton> buttons;
    private HashMap<String, SideBarItemsButton> buttonsHash;
    
    private SideBarItemsButton currentButton;
    
    private boolean isCurrentButtonReady;
    
    private Point position;
    
    protected SideBarPage(Point pos) {
	this.buttons = new ArrayList<>();
	this.buttonsHash = new HashMap<>();
	
	this.position = pos;
    }
    
    public void render(Graphics g, Color filterColor) {
	for (SideBarItemsButton button : this.buttons) {
	    if (button.isVisible()) {
		button.render(g, filterColor);
	    } else {
		button.renderDisabled(g, filterColor);
	   }
	}
    }
    public abstract void update(int delta);
    
    public void mouseClick(int x, int y) {
	System.out.println("[SBP] Button pos: " + x + "; " + y);
	
	for (SideBarItemsButton btn : this.buttons) {
	    if (btn.posX == x && btn.posY == y) {
		if (btn.isVisible()) {
		    System.out.println("[SBP] Found button: " + btn.getDescription());
		    buttonClicked(btn);
		    return;
		}
	    }
	}
    }
    
    public SideBarItemsButton getCurrentButton() {
	return this.currentButton;
    }
    
    protected void addButton(SideBarItemsButton button) {
	this.buttons.add(button);
	this.buttonsHash.put(button.getTextureName(), button);
    }
    
    public boolean isCurrentButtonReady() {
	return this.isCurrentButtonReady();
    }
    
    public Point getPosition() {
	return this.position;
    }
    
    public SideBarItemsButton getButton(String name) {
	return this.buttonsHash.get(name);
    }
    
    public abstract void buttonClicked(SideBarItemsButton button);
}
