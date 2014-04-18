package cr0s.javara.gameplay;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.render.shrouds.Shroud;

public class Player {
	public String name;
	private Alignment side;

	public Color playerColor;
	
	public int powerLevel = 0;
	public int powerConsumptionLevel = 0;
	
	public boolean canBuild, canTrainInfantry, canBuildVehicles, canBuildAirUnits, canBuildNavalUnits;
	
	public LinkedList<Entity> selectedEntities = new LinkedList<>();
	
	private Base base;
	private Team team;
	
	private Shroud playerShroud;
	
	public Player(String name, Alignment side, Color color) {
		this.name = name;
		this.side = side;
		this.playerColor = color;
		
		this.base = new Base(team, this);
	}
	
	public void postMoveOrder(float destX, float destY) {
	    for (Entity e : this.selectedEntities) {
		if (!e.isDead() && (e instanceof IMovable)) {
		    ((IMovable)e).moveTo((int)destX, (int)destY);
		}
	    }
	}
	
	public Alignment getAlignment() {
	    return this.side;
	}

	public Base getBase() {
	    return this.base;
	}
	
	public void setTeam(Team t) {
	    t.addPlayer(this);
	    this.team = t;
	}
	
	public Shroud getShroud() {
	    return this.playerShroud;
	}
	
	public void setShroud(Shroud s) {
	    this.playerShroud = s;
	}
}
