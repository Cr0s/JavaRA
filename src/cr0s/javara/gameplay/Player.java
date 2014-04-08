package cr0s.javara.gameplay;

import org.newdawn.slick.Color;

import cr0s.javara.gameplay.Team.Alignment;

public class Player {
	public String name;
	public Alignment side;

	public Color playerColor;
	
	public int powerLevel = 0;
	public int powerConsumptionLevel = 0;
	
	public boolean canBuild, canTrainInfantry, canBuildVehicles, canBuildAirUnits, canBuildNavalUnits;
	
	public Player(String name, Alignment side, Color color) {
		this.name = name;
		this.side = side;
		this.playerColor = color;
	}
}
