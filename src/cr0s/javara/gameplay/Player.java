package cr0s.javara.gameplay;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.gameplay.Team.Alignment;

public class Player {
	public String name;
	public Alignment side;

	public Color playerColor;
	
	public int powerLevel = 0;
	public int powerConsumptionLevel = 0;
	
	public boolean canBuild, canTrainInfantry, canBuildVehicles, canBuildAirUnits, canBuildNavalUnits;
	
	public LinkedList<Entity> selectedEntities = new LinkedList<>();
	
	public Player(String name, Alignment side, Color color) {
		this.name = name;
		this.side = side;
		this.playerColor = color;
	}
	
	public void postMoveOrder(float destX, float destY) {
	    for (Entity e : this.selectedEntities) {
		if (!e.isDead() && (e instanceof IMovable)) {
		    ((IMovable)e).moveTo((int)destX, (int)destY);
		}
	    }
	}
}
