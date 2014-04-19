package cr0s.javara.ui;

import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

import cr0s.javara.gameplay.Base;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.ui.sbpages.PageBuildingSoviet;
import cr0s.javara.ui.sbpages.SideBarPage;

public class GameSideBar {
    private Team team;
    private Player player;

    private Rectangle sidebarBounds;
    
    private static final int BAR_HEIGHT = 480;
    private static final int BAR_WIDTH = 130;

    private static final int BAR_SPACING_W = 25;
    private static final int BAR_SPACING_H = 25;

    private static final int RADAR_HEIGHT = BAR_WIDTH - 4;

    private static final int MENU_START_Y = BAR_SPACING_H + RADAR_HEIGHT + 6;//30;

    private static final Color BG_COLOR = new Color(0xC0, 0xC0, 0xC0, 255);

    private SpriteSheet menuCategoriesSheet;

    //             y  x
    private boolean[][] sideBarCategoriesOpened;
    
    private Color translucentColor = new Color(255, 255, 255, 90);
    private Color opaqueColor = new Color(255, 255, 255, 255);
    
    private HashMap<String, SideBarPage> sideBarPages;
    private SideBarPage currentPage;
    private String currentPageName;
    
    private static final String START_PAGE_NAME = "start";
    private static final String PAGE_BUILDING_SOVIET = "sovbuild";
    
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
	
	this.sideBarPages = new HashMap<>();	
	this.sidebarBounds = new Rectangle(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W, BAR_SPACING_H, BAR_WIDTH, BAR_HEIGHT);

	
	
	this.sideBarCategoriesOpened = new boolean[6][2];
	this.sideBarCategoriesOpened[0][1] = true;
	this.sideBarCategoriesOpened[1][1] = true;
	
	switchPage(START_PAGE_NAME);
    }
    
    public void initSidebarPages() {
	if (this.sideBarPages.isEmpty()) {
	    this.sideBarPages.put(PAGE_BUILDING_SOVIET, new PageBuildingSoviet(new Point(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W, BAR_SPACING_H)));
	}
    }

    public void render(Graphics g) {
	Color pColor = g.getColor();
	g.setColor(BG_COLOR);
	g.fill(this.sidebarBounds);
	drawSideBarButtons(g);
	g.setColor(pColor);

    }

    public void drawRadar(Graphics g) {
	g.setColor(Color.black.multiply(getBackgroundColor()));
	g.fillRect(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 2, BAR_SPACING_H + 2, BAR_WIDTH - 4, RADAR_HEIGHT);
    }

    public void drawSideBarButtons(Graphics g) {
	int sX = Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 1;
	int sY = MENU_START_Y;

	Color filterColor = getBackgroundColor();
	
	if (currentPage == null) {
	    drawStartPage(g, sX, sY, filterColor);
	} else {
	    drawCurrentPage(g, sX, sY, filterColor);
	}
    }

    private void drawCurrentPage(Graphics g, int sX, int sY, Color filterColor) {
	if (this.currentPage != null) {
	    this.currentPage.render(g, filterColor);
	}
    }
    
    private void drawStartPage(Graphics g, int sX, int sY, Color filterColor) {
	drawRadar(g);
	
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
	Base base = this.player.getBase();
	
	// Update buttons presents
	
	// Construction yards
	this.sideBarCategoriesOpened[0][1] = base.isSovietCYPresent;
	this.sideBarCategoriesOpened[0][0] = base.isAlliedCYPresent;
	
	// War Factory
	this.sideBarCategoriesOpened[1][0] = base.isWarFactoryPresent;
	
	// Barracks/Tent
	this.sideBarCategoriesOpened[1][1] = base.isBarracksPresent || base.isTentPresent;
	
	// Sub Pen, Ship Yard
	this.sideBarCategoriesOpened[2][0] = base.isShipYardPresent || base.isSubPenPresent;
	
	// Air Field, Helipad
	this.sideBarCategoriesOpened[2][1] = base.isAirLinePresent || base.isHelipadPresent;
	
	// Any superpower
	this.sideBarCategoriesOpened[3][0] = this.sideBarCategoriesOpened[3][1] = base.isAnySuperPowerPresent;
    }
    
    public Player getPlayer() {
	return this.player;
    }
    
    public boolean isMouseInsideBar() {
	int mouseX = Main.getInstance().getContainer().getInput().getMouseX();
	int mouseY = Main.getInstance().getContainer().getInput().getMouseY();
	
	return this.sidebarBounds.contains(mouseX, mouseY);
    }
    
    private Color getBackgroundColor() {
	if (!isMouseInsideBar()) {
	    return translucentColor;
	} else {
	    return opaqueColor;
	}
    }
    
    public void startPageClick(int buttonX, int buttonY) {
	switch (buttonX) {
	case 0: // Construction Yard clicked
	    if (buttonY == 0) { // 
		if (this.player.getAlignment() == Alignment.SOVIET) {
		    if (this.sideBarCategoriesOpened[0][1]) {
			switchPage(PAGE_BUILDING_SOVIET);
		    }
		}
	    }
	}
    }
    
    public void mouseClicked(int button, int x, int y) {
	// Transform absolute mouse coordinates to sidebar-relative
	int barX = Main.getInstance().getContainer().getWidth() - x - BAR_SPACING_W;
	int barY = Main.getInstance().getContainer().getHeight() - y - BAR_SPACING_H;
	
	// Transform click coordinates to button coordinates
	int buttonX, buttonY;
	
	if (currentPage == null) {
	    buttonX = 1 - (barX / 64);
	    buttonY = (y - MENU_START_Y) / 48;
	    
	    if (button == 0) {
		startPageClick(buttonX, buttonY);
	    }
	} else {
	    if (button == 1) {
		switchPage("start");
		return;
	    }
	    
	    buttonX = barX / 64;
	    buttonY = barY / 48;
	}
	
	System.out.println("[" + barX + "; " + barY + "] Button-" + button + " clicked: " + buttonX + " " + buttonY);
    }
    
    private void switchPage(String pageName) {
	this.currentPage = this.sideBarPages.get(pageName);
	this.currentPageName = pageName;
    }
}
