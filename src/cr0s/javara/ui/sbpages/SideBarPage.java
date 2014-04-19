package cr0s.javara.ui.sbpages;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.main.Main;

public abstract class SideBarPage {
    private ArrayList<SideBarItemsButton> buttons;
    
    private SideBarItemsButton currentButton;
    
    private boolean isCurrentButtonReady;
    
    private Point position;
    
    protected SideBarPage(Point pos) {
	this.buttons = new ArrayList<>();
	
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
    
    public abstract void mouseClick(float x, float y);
    
    public SideBarItemsButton getCurrentButton() {
	return this.currentButton;
    }
    
    protected void addButton(SideBarItemsButton button) {
	this.buttons.add(button);
    }
    
    public boolean isCurrentButtonReady() {
	return this.isCurrentButtonReady();
    }
    
    public Point getPosition() {
	return this.position;
    }
}
