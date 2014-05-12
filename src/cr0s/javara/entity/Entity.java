package cr0s.javara.entity;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityWarFactory;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.render.EntityBlockingMap.FillsSpace;
import cr0s.javara.render.World;

public abstract class Entity {
	public World world;
	
	public float posX, posY;
	public float moveX, moveY;
	
	private boolean isDead;
	private int hp, maxHp;
	
	public Team team;
	public Player owner;
	
	public boolean isSelected = false;
	public boolean isVisible = false;
	
	public Rectangle boundingBox;

	public boolean isMouseOver = false;

	private boolean isInvuln = false;

	private static final int SELECTION_BOX_ADD = 5;
	
	public float sizeWidth, sizeHeight;
	
	public int currentFacing;
	
	public float updateDelta;
	
	public FillsSpace fillsSpace;
	
	public Entity (float posX, float posY, Team team, Player owner, float aSizeWidth, float aSizeHeight) {
		this.posX = posX;
		this.posY = posY;
		
		this.sizeWidth = aSizeWidth;
		this.sizeHeight = aSizeWidth;
		
		this.boundingBox = new Rectangle(posX, posY, sizeWidth, sizeHeight);

		
		this.team = team;
		this.owner = owner;
		
		this.fillsSpace = FillsSpace.DONT_FILLS;
	}
	
	public void setWorld(World w) {
		this.world = w;
	}
	
	public abstract void updateEntity(int delta);
	public abstract void renderEntity(Graphics g);
	public abstract boolean shouldRenderedInPass(int passNum);
	
	public void drawSelectionBox(Graphics g) {
	    final int REDUCE = 8;
	    g.setLineWidth(2);
	    g.setColor(Color.white);
	    
	    float minX = this.boundingBox.getMinX();
	    float minY = this.boundingBox.getMinY();
	    float maxX = this.boundingBox.getMaxX();
	    float maxY = this.boundingBox.getMaxY();
	    
	    int bbWidth = (int) (this.boundingBox.getWidth() + 2*SELECTION_BOX_ADD);
	    int bbHeight = (int) (this.boundingBox.getHeight() + 2*SELECTION_BOX_ADD);
	    
	    float cornerXUpLeft = minX - 2*SELECTION_BOX_ADD;
	    float cornerYUpLeft = minY - 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXUpLeft, cornerYUpLeft, cornerXUpLeft + bbWidth / REDUCE, cornerYUpLeft);
	    g.drawLine(cornerXUpLeft, cornerYUpLeft, cornerXUpLeft, cornerYUpLeft + bbHeight / REDUCE);
	    
	    float cornerXDownLeft = minX - 2*SELECTION_BOX_ADD;
	    float cornerYDownLeft = minY + this.boundingBox.getHeight() + 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXDownLeft, cornerYDownLeft, cornerXDownLeft + bbWidth / REDUCE, cornerYDownLeft);
	    g.drawLine(cornerXDownLeft, cornerYDownLeft, cornerXDownLeft, cornerYDownLeft - bbHeight / REDUCE);
	    
	    // Right corners
	    float cornerXUpRight = maxX + 2*SELECTION_BOX_ADD;
	    float cornerYUpRight = maxY - this.boundingBox.getHeight() - 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXUpRight, cornerYUpRight, cornerXUpRight - bbWidth / REDUCE, cornerYUpRight);
	    g.drawLine(cornerXUpRight, cornerYUpRight, cornerXUpRight, cornerYUpRight + bbHeight / REDUCE);
	    
	    float cornerXDownRight = maxX + 2*SELECTION_BOX_ADD;
	    float cornerYDownRight = maxY + 2*SELECTION_BOX_ADD;
	    g.drawLine(cornerXDownRight, cornerYDownRight, cornerXDownRight - bbWidth / REDUCE, cornerYDownRight);
	    g.drawLine(cornerXDownRight, cornerYDownRight, cornerXDownRight, cornerYDownRight - bbHeight / REDUCE);
	    
	    drawHpBar(g);
	    if (this instanceof IPips) {
		drawPips(g);
	    }
	}
	
	public void drawHpBar(Graphics g) {
	    final int BAR_COMPRESS = 3; // "Compress" bar by N pixels from left and right
	    
	    float minX = this.boundingBox.getMinX();
	    float minY = this.boundingBox.getMinY();
	    float maxX = this.boundingBox.getMaxX();
	    float maxY = this.boundingBox.getMaxY();
	    
	    float cornerXUpLeft = minX - 2*SELECTION_BOX_ADD + BAR_COMPRESS;
	    float cornerYUpLeft = minY - 2*SELECTION_BOX_ADD;
	    float cornerXDownLeft = minX - 2*SELECTION_BOX_ADD;
	    float cornerYDownLeft = minY + this.boundingBox.getHeight() + 2*SELECTION_BOX_ADD;
	    float cornerXUpRight = maxX + 2*SELECTION_BOX_ADD - BAR_COMPRESS;
	    float cornerYUpRight = maxY - this.boundingBox.getHeight() - 2*SELECTION_BOX_ADD;
	    float cornerXDownRight = maxX + 2*SELECTION_BOX_ADD;
	    float cornerYDownRight = maxY + 2*SELECTION_BOX_ADD;
	    
	    float barWidth = (cornerXUpRight - cornerXUpLeft) - 1;
	    
	    // Draw HP bar
	    g.setLineWidth(5);
	    g.setColor(Color.black);
	    g.drawLine(cornerXUpLeft, cornerYUpLeft - 8, cornerXUpRight, cornerYUpRight - 8);
	    
	    float hpBarWidth = this.getHp() / (float) Math.max(1, this.getMaxHp()) * barWidth;
	    
	    g.setLineWidth(3);
	    g.setColor(getHpColor());
	    g.drawLine(cornerXUpLeft + 1, cornerYUpLeft - 8, cornerXUpLeft + hpBarWidth + 1, cornerYUpRight - 8);	
	    
	    // Draw progress bar
	    if (this instanceof EntityBuilding && ((EntityBuilding) this).getProgressValue() != -1) {
		int total = ((EntityBuilding) this).getMaxProgress();
		int ready = ((EntityBuilding) this).getProgressValue();

		float barWidthProgress = (ready / (float) total) * barWidth;
		
		g.setLineWidth(5);
		g.setColor(Color.black);
		g.drawLine(cornerXUpLeft, cornerYUpLeft - 2, cornerXUpRight, cornerYUpRight - 2);

		g.setLineWidth(3);
		g.setColor(Color.magenta);
		g.drawLine(cornerXUpLeft + 1, cornerYUpLeft - 2, cornerXUpLeft + 1 + barWidthProgress, cornerYUpRight - 2);			
	    }
	    
	   if (this instanceof EntityWarFactory) {
	       if (((EntityBuilding)this).isPrimary()) {
		   drawPrimarySign(g);
	       }
	   }
	}	
	
	private void drawPips(Graphics g) {
	    final int PIPS_OFFSET_Y = 8;
	    final int PIPS_OFFSET_X = 4;
	    final int PIPS_SPACING_X = 5;
	    final int PIPS_SIZE = 3;
	    
	    float cornerYDownLeft = this.boundingBox.getMinY() + this.boundingBox.getHeight() + 2*SELECTION_BOX_ADD;
	    float cornerXDownLeft = this.boundingBox.getMinX() - 2*SELECTION_BOX_ADD;   
	    
	    int pipY = (int) cornerYDownLeft - PIPS_OFFSET_Y; 
	    int startPipX = (int) cornerXDownLeft + PIPS_SPACING_X;
	    
	    
	    IPips pipedEntity = (IPips) this;
	    
	    for (int pip = 0; pip < pipedEntity.getPipCount(); pip++) {
		Color pipColor = pipedEntity.getPipColorAt(pip);
		
		int pipX = startPipX + pip * PIPS_SPACING_X;
		
		g.setColor(Color.gray);
		g.setLineWidth(1);
		
		g.drawRect(pipX, pipY, PIPS_SIZE, PIPS_SIZE);
		
		if (pipColor != null) {
		    g.setColor(pipColor);
		    
		    g.fillRect(pipX + 1, pipY + 1, PIPS_SIZE - 1, PIPS_SIZE - 1);
		}
	    }
	}
	
	public void drawPrimarySign(Graphics g) {
	    g.setColor(Color.white);
	    final String s = "Primary";
	    g.drawString(s, this.boundingBox.getCenterX() - (g.getFont().getWidth(s) / 2), this.boundingBox.getMaxY() - g.getFont().getLineHeight() - 2);
	}
	
	public Color getHpColor() {
	    float part = this.getHp() / (float) Math.max(1, this.getMaxHp());
	    
	    if (part <= 0.25f) {
		return Color.red;
	    } else if (part > 0.25f && part < 0.50f) {
		return Color.orange;
	    } else if (part >= 0.50f) {
		return Color.green;
	    }
	    
	    return Color.black;
	}
	
	public boolean isDead() {
	    return this.isDead;
	}
	
	public void setDead() {
	    this.isVisible = false;
	    this.isDead = true;
	}
	
	public int getHp() {
	    return this.hp;
	}
	
	public void setHp(int aHp) {
	    this.hp = aHp;
	}
	
	public int getMaxHp() {
	    return this.maxHp;
	}
	
	public void setMaxHp(int aMaxHp) {
	    this.maxHp = aMaxHp;
	}
	
	public void giveDamage(int amount) {
		if (!isInvuln) {
			this.setHp(this.getHp() - amount);
			
			if (this.getHp() <= 0) {
				this.setHp(0);
				setDead();
			}
		}
	}
	
	public void setInvuln(boolean invuln) {
	    this.isInvuln = invuln;
	}
	
	public boolean getInvuln() {
	    return this.isInvuln;
	}
}
