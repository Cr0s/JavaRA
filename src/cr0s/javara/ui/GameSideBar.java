package cr0s.javara.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;

public class GameSideBar {
    private Team team;
    private Player player;

    private static final int BAR_HEIGHT = 325;
    private static final int BAR_WIDTH = 130;

    private static final int BAR_SPACING_W = 25;
    private static final int BAR_SPACING_H = 150;

    private static final int RADAR_HEIGHT = BAR_WIDTH - 4;

    private static final int MENU_START_Y = BAR_SPACING_H + RADAR_HEIGHT + 6;//30;

    private static final Color BG_COLOR = new Color(0xC0, 0xC0, 0xC0, 128);

    private SpriteSheet menuCategoriesSheet;

    private boolean[][] sideBarCategoriesOpened;
    private Color filterColor = new Color(255, 255, 255, 255);

    public GameSideBar(Team aTeam, Player aPlayer) {
	try {
	    this.menuCategoriesSheet = new SpriteSheet(ResourceManager.SIDEBAR_CATEGORIES_SHEET, 64, 48);

	    System.out.println("Button sheet: " + this.menuCategoriesSheet.getHorizontalCount() + " x " + this.menuCategoriesSheet.getVerticalCount());
	} catch (SlickException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	this.team = aTeam;
	this.player = aPlayer;

	this.sideBarCategoriesOpened = new boolean[6][2];
	this.sideBarCategoriesOpened[0][1] = true;
	this.sideBarCategoriesOpened[1][1] = true;
    }

    public void render(Graphics g) {
	Color pColor = g.getColor();
	g.setColor(BG_COLOR);
	g.fillRect(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W, BAR_SPACING_H, BAR_WIDTH, BAR_HEIGHT);
	drawRadar(g);
	drawSideBarButtons(g);
	g.setColor(pColor);

    }

    public void drawRadar(Graphics g) {
	g.setColor(Color.black.multiply(filterColor));
	g.fillRect(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 2, BAR_SPACING_H + 2, BAR_WIDTH - 4, RADAR_HEIGHT);
    }

    public void drawSideBarButtons(Graphics g) {
	int sX = Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 1;
	int sY = MENU_START_Y;

	if (this.player.getAlignment() == Alignment.SOVIET) {
	    // Draw soviet, then allied
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][1] ? 1 : 0, 1).draw(sX, sY, filterColor);
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][0] ? 1 : 0, 0).draw(sX + 64, sY, filterColor);
	} else {
	    // Draw allied, then soviet
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][0] ? 1 : 0, 0).draw(sX, sY, filterColor);
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][1] ? 1 : 0, 1).draw(sX + 64, sY, filterColor);
	}

	for (int i = 1; i < 4; i++) {
	    int x = Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 1;
	    int y = MENU_START_Y;

	    for (int j = 0; j < 2; j++) {
		this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[i][j ] ? 1 : 0, 2 * i + j).draw(x + (64 * j), y + (i * 48), filterColor);
	    }
	}
    }

    public void update(int delta) {

    }
}
