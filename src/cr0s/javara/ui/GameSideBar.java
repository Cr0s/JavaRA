package cr0s.javara.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;

public class GameSideBar {
    private Team team;
    private Player player;
    
    private static final int BAR_HEIGHT = 500;
    private static final int BAR_WIDTH = 200;
    
    private static final int BAR_SPACING_W = 25;
    private static final int BAR_SPACING_H = 50;
    
    private static final Color BG_COLOR = new Color(0xC0, 0xC0, 0xC0, 128);
    
    public GameSideBar(Team aTeam, Player aPlayer) {
	this.team = aTeam;
	this.player = aPlayer;
    }
    
    public void render(Graphics g) {
	return;
	
	/* TODO: create sidebar
	Color pColor = g.getColor();
	g.setColor(BG_COLOR);
	g.fillRect(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W, BAR_SPACING_H, BAR_WIDTH, BAR_HEIGHT);
	g.setColor(pColor);
	*/
    }
    
    public void update(int delta) {
	
    }
}
