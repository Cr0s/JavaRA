package cr0s.javara.entity;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.render.World;

public abstract class Entity {
	public World world;
	
	public float posX, posY;
	public float renderX, renderY;
	
	public Team team;
	public Player owner;
	
	public boolean isSelected = false;
	public boolean isVisible = false;
	
	public Rectangle boundingBox;

	public boolean isMouseOver = false;
	
	private static final int SELECTION_BOX_ADD = 5;
	
	public Entity (float posX, float posY, Team team, Player owner, float sizeWidth, float sizeHeight) {
		this.posX = posX;
		this.posY = posY;
		
		this.boundingBox = new Rectangle(posX, posY, sizeWidth, sizeHeight);
		
		this.team = team;
		this.owner = owner;
	}
	
	public void setWorld(World w) {
		this.world = w;
	}
	
	public abstract void updateEntity(int delta);
	public abstract void renderEntity(Graphics g);
	public abstract boolean shouldRenderedInPass(int passNum);
	
	public void drawSelectionBox(Graphics g) {
	    final int REDUCE = 5;
	    g.setLineWidth(2);
	    g.setColor(Color.white);
	    
	    int bbWidth = (int) (this.boundingBox.getWidth() + 2*SELECTION_BOX_ADD);
	    int bbHeight = (int) (this.boundingBox.getHeight() + 2*SELECTION_BOX_ADD);
	    
	    float cornerXUpLeft = this.boundingBox.getMinX() - 2*SELECTION_BOX_ADD;
	    float cornerYUpLeft = this.boundingBox.getMinY() - 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXUpLeft, cornerYUpLeft, cornerXUpLeft + bbWidth / REDUCE, cornerYUpLeft);
	    g.drawLine(cornerXUpLeft, cornerYUpLeft, cornerXUpLeft, cornerYUpLeft + bbHeight / REDUCE);
	    
	    float cornerXDownLeft = this.boundingBox.getMinX() - 2*SELECTION_BOX_ADD;
	    float cornerYDownLeft = this.boundingBox.getMinY() + this.boundingBox.getHeight() + 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXDownLeft, cornerYDownLeft, cornerXDownLeft + bbWidth / REDUCE, cornerYDownLeft);
	    g.drawLine(cornerXDownLeft, cornerYDownLeft, cornerXDownLeft, cornerYDownLeft - bbHeight / REDUCE);
	    
	    // Right corners
	    float cornerXUpRight = this.boundingBox.getMaxX() + 2*SELECTION_BOX_ADD;
	    float cornerYUpRight = this.boundingBox.getMaxY() - this.boundingBox.getHeight() - 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXUpRight, cornerYUpRight, cornerXUpRight - bbWidth / REDUCE, cornerYUpRight);
	    g.drawLine(cornerXUpRight, cornerYUpRight, cornerXUpRight, cornerYUpRight + bbHeight / REDUCE);
	    
	    float cornerXDownRight = this.boundingBox.getMaxX() + 2*SELECTION_BOX_ADD;
	    float cornerYDownRight = this.boundingBox.getMaxY() + 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXDownRight, cornerYDownRight, cornerXDownRight - bbWidth / REDUCE, cornerYDownRight);
	    g.drawLine(cornerXDownRight, cornerYDownRight, cornerXDownRight, cornerYDownRight - bbHeight / REDUCE);
	    
	    drawHpBar(g);
    
	}
	
	public void drawHpBar(Graphics g) {
	    float cornerXUpLeft = this.boundingBox.getMinX() - 2*SELECTION_BOX_ADD;
	    float cornerYUpLeft = this.boundingBox.getMinY() - 2*SELECTION_BOX_ADD;
	    float cornerXDownLeft = this.boundingBox.getMinX() - 2*SELECTION_BOX_ADD;
	    float cornerYDownLeft = this.boundingBox.getMinY() + this.boundingBox.getHeight() + 2*SELECTION_BOX_ADD;
	    float cornerXUpRight = this.boundingBox.getMaxX() + 2*SELECTION_BOX_ADD;
	    float cornerYUpRight = this.boundingBox.getMaxY() - this.boundingBox.getHeight() - 2*SELECTION_BOX_ADD;
	    float cornerXDownRight = this.boundingBox.getMaxX() + 2*SELECTION_BOX_ADD;
	    float cornerYDownRight = this.boundingBox.getMaxY() + 2*SELECTION_BOX_ADD;
	    
	    // Draw HP bar
	    g.setLineWidth(5);
	    g.setColor(Color.black);
	    g.drawLine(cornerXUpLeft, cornerYUpLeft - 8, cornerXUpRight, cornerYUpRight - 8);
	    
	    g.setLineWidth(3);
	    g.setColor(Color.green);
	    g.drawLine(cornerXUpLeft + 1, cornerYUpLeft - 8, cornerXUpRight - 1, cornerYUpRight - 8);	
	}	
}
