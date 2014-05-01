package cr0s.javara.ui.sbpages;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Point;

import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public abstract class SideBarItemsButton {
    private ShpTexture buttonTexture;
    private String textureName;
    private Image buttonImg;
    
    private Point position;
    private boolean isVisible;
    private Color disabledColor = new Color(0, 0, 0, 200);
    
    public int posX, posY;
    
    private String description;
    
    public SideBarItemsButton(String aDescription, String aTextureName, Point pagePos, int aPosX, int aPosY, boolean aIsVisible) {
	this.position = new Point(pagePos.getX() + aPosX * 64, pagePos.getY() + aPosY * 48);
	this.posX = aPosX;
	this.posY = aPosY;
	
	this.description = aDescription;
	this.isVisible = aIsVisible;
	
	if (!aTextureName.isEmpty()) {
	    this.textureName = aTextureName;
	    this.buttonTexture = ResourceManager.getInstance().getSidebarTexture(aTextureName);
	    this.buttonImg = buttonTexture.getAsImage(0, Main.getInstance().getSideBar().getPlayer().playerColor);
	}
    }
    
    public void render(Graphics g, Color filterColor) {
	this.buttonImg.draw(position.getX(), position.getY(), filterColor);
    }

    public void renderDisabled(Graphics g, Color filterColor) {
	this.buttonImg.draw(position.getX(), position.getY(), filterColor);
	Color pColor = g.getColor();
	g.setColor(disabledColor.multiply(filterColor));
	g.fillRect(position.getX(), position.getY(), 64, 48);
	g.setColor(pColor);
    }
        
    
    public Point getPosition() {
	return this.position;
    }
    
    public boolean isVisible() {
	return this.isVisible;
    }

    public String getDescription() {
	return this.description;
    }
    
    public Image getTexture() {
	return this.buttonImg;
    }

    public void setVisible(boolean b) {
	this.isVisible = b;
    }

    public String getTextureName() {
	return this.textureName;
    }
}
