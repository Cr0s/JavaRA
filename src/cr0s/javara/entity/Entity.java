package cr0s.javara.entity;

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
}
