package cr0s.javara.gameplay;

import java.util.ArrayList;

public class Team {
	public enum Alignment { SOVIET, ALLIED, NEUTRAL }; 

	public ArrayList<Player> playersInTeam = new ArrayList<>();

	public Team() {
	    
	}
	
	public void addPlayer(Player player) {
	    if (!this.playersInTeam.contains(player)) {
		this.playersInTeam.add(player);
	    }
	}
}
