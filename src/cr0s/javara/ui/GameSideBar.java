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
import cr0s.javara.render.map.MinimapRenderer;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.sbpages.building.PageBuildingSoviet;
import cr0s.javara.ui.sbpages.vehicle.PageVehicle;
import cr0s.javara.ui.sbpages.SideBarPage;

public class GameSideBar {
    private Team team;
    private Player player;

    private Rectangle sidebarBounds;

    private static final int BAR_HEIGHT = 482;
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

    public static final String START_PAGE_NAME = "start";
    public static final String PAGE_BUILDING_SOVIET = "sovbuild";
    public static final String PAGE_VEHICLE = "vehicle";

    private Color progressHideColor = new Color(0, 0, 0, 128);

    private Rectangle currentViewportRect = new Rectangle(0, 0, 0, 0);
    private Rectangle radarRect = new Rectangle(0, 0, 0, 0);
    private float previewScale;
    private Point previewOrigin;

    private MinimapRenderer minimap;
    private PowerBarRenderer powerBar;

    private static final int POWERBAR_WIDTH = 10;
    
    private final int MINIMAP_UPDATE_INTERVAL_TICKS = 10;
    private int minimapUpdateTicks = MINIMAP_UPDATE_INTERVAL_TICKS;
    
    private int lowPowerAdviceTicks = 0;
    private int LOW_POWER_ADVICE_INTERVAL = 250;
    
    private boolean wasLowPower = false;

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

	this.radarRect.setBounds(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 2, BAR_SPACING_H + 2, BAR_WIDTH - 4, RADAR_HEIGHT);
	this.minimap = new MinimapRenderer(Main.getInstance().getWorld(), (int) Main.getInstance().getWorld().getMap().getBounds().getWidth() / 24, (int) (int) Main.getInstance().getWorld().getMap().getBounds().getHeight() / 24);

	this.powerBar = new PowerBarRenderer(new Point(sidebarBounds.getMinX() - POWERBAR_WIDTH, sidebarBounds.getMaxY()), POWERBAR_WIDTH, (int) sidebarBounds.getHeight());

	switchPage(START_PAGE_NAME);
    }

    public void initSidebarPages() {
	if (this.sideBarPages.isEmpty()) {
	    this.sideBarPages.put(PAGE_BUILDING_SOVIET, new PageBuildingSoviet(new Point(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 1, BAR_SPACING_H + 1)));
	    this.sideBarPages.put(PAGE_VEHICLE, new PageVehicle(new Point(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 1, BAR_SPACING_H + 1)));
	}
    }

    public void render(Graphics g) {
	Color pColor = g.getColor();
	g.setColor(BG_COLOR.multiply(this.getBackgroundColor()));
	g.fill(this.sidebarBounds);
	this.powerBar.render(g, this.getBackgroundColor());
	drawSideBarButtons(g);
	g.setColor(pColor);

    }

    public void drawRadar(Graphics g) {
	if (Main.getInstance().getPlayer().getBase().isRadarDomePresent && !Main.getInstance().getPlayer().getBase().isLowPower()) {
	    this.minimap.renderMinimap(this.previewOrigin, g, getBackgroundColor());
	} else {
	    g.setColor(Color.black.multiply(getBackgroundColor()));
	    g.fill(radarRect);	    
	}

	drawCurrentViewportRect(g);
    }

    public void drawCurrentViewportRect(Graphics g) {
	g.setColor(Color.white.multiply(this.getBackgroundColor()));

	g.setLineWidth(1);
	g.draw(this.currentViewportRect);
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
		// Is this Vehicles button with progress?
		if (i == 1 && j == 0 && this.player.getBase().isCurrentVehicleBuilding()) {
		    drawCurrentVehicleProgress(g, x + (64 * j), y + (i * 48), filterColor);
		} else { // Default case
		    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[i][j] ? 1 : 0, 2 * i + j).draw(x + (64 * j), y + (i * 48), filterColor);
		}
	    }
	}	
    }

    private void drawCurrentVehicleProgress(Graphics g, int x, int y, Color filterColor) {
	// Draw unit image first
	player.getBase().getCurrentVehicleButton().getTexture().draw(x, y, filterColor);

	// Draw progress rect
	Color pColor = g.getColor();

	g.setColor(this.progressHideColor.multiply(filterColor));
	g.fillRect(x, y, 64, 48 - player.getBase().getCurrentVehicleProgress());

	g.setColor(Color.white.multiply(filterColor));
	// Draw status
	if (player.getBase().isCurrentVehicleReady() && !player.getBase().isCurrentVehicleDeployed()) {
	    g.drawString("ready", x + 5, y + 46 - g.getFont().getLineHeight());
	} else if (player.getBase().isCurrentVehicleHold()) {
	    g.drawString("on hold", x + 1, y + 46 - g.getFont().getLineHeight());
	}

	g.setColor(pColor);
    }

    public void update(int delta) {
	if (Main.getInstance().getPlayer().getBase().isLowPower()) {
	    if (--this.lowPowerAdviceTicks <= 0) {
		this.lowPowerAdviceTicks = this.LOW_POWER_ADVICE_INTERVAL;
		
		SoundManager.getInstance().playSpeechSoundGlobal("lopower1");
	    }
	    
	    if (!wasLowPower) {
		this.wasLowPower = true;
		
		// If power down occured with radar, play radar disabling sound
		if (Main.getInstance().getPlayer().getBase().isRadarDomePresent) {
		    SoundManager.getInstance().playSfxGlobal("radardn1", 0.9f);
		}
	    }
	} else {
	    if (wasLowPower) {
		this.wasLowPower = false;
		
		// If power up occured with radar, play radar enabling sound
		if (Main.getInstance().getPlayer().getBase().isRadarDomePresent) {
		    SoundManager.getInstance().playSfxGlobal("radaron2", 0.9f);
		}		
	    }
	}

	// Update radar rect
	this.radarRect.setBounds(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 2, BAR_SPACING_H + 2, BAR_WIDTH - 4, RADAR_HEIGHT);

	if (--this.minimapUpdateTicks <= 0) {
	    this.minimapUpdateTicks = this.MINIMAP_UPDATE_INTERVAL_TICKS;
	    
	    this.minimap.update(this.getBackgroundColor());
	}
	
	// Update current viewport rect
	int size = Math.max(Main.getInstance().getContainer().getWidth(), Main.getInstance().getContainer().getHeight());
	previewScale =  Math.min(Main.getInstance().getContainer().getWidth() / 24 * 1.0f / this.radarRect.getWidth(), Main.getInstance().getContainer().getHeight() / 24 * 1.0f / this.radarRect.getHeight());	

	int spaceLeft = (int) (this.radarRect.getWidth() - Main.getInstance().getWorld().getMap().getBounds().getWidth() / 24);

	previewOrigin = new Point(this.radarRect.getMinX() + spaceLeft / 2, this.radarRect.getMinY() + spaceLeft / 2);

	Point vpPoint = this.cellToMinimapPixel(new Point(-Main.getInstance().getCamera().getOffsetX() / 24, -Main.getInstance().getCamera().getOffsetY() / 24));
	this.currentViewportRect.setBounds(0, 0, previewScale * Main.getInstance().getWorld().getMap().getWidth(), previewScale * Main.getInstance().getWorld().getMap().getHeight());
	this.currentViewportRect.setLocation(vpPoint.getMinX(), vpPoint.getMinY());	

	// Update buttons presents
	Base base = this.player.getBase();


	// Construction yards
	this.sideBarCategoriesOpened[0][1] = base.isSovietCYPresent;
	this.sideBarCategoriesOpened[0][0] = base.isAlliedCYPresent;

	// War Factory
	this.sideBarCategoriesOpened[1][0] = base.isAlliedWarFactoryPresent || base.isSovietWarFactoryPresent;

	// Barracks/Tent
	this.sideBarCategoriesOpened[1][1] = base.isBarracksPresent || base.isTentPresent;

	// Sub Pen, Ship Yard
	this.sideBarCategoriesOpened[2][0] = base.isShipYardPresent || base.isSubPenPresent;

	// Air Field, Helipad
	this.sideBarCategoriesOpened[2][1] = base.isAirLinePresent || base.isHelipadPresent;

	// Any superpower
	this.sideBarCategoriesOpened[3][0] = this.sideBarCategoriesOpened[3][1] = base.isAnySuperPowerPresent;

	if (this.currentPage != null) {
	    this.currentPage.update(delta);
	}
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

    public void startPageClick(int button, int buttonX, int buttonY) {
	if (button == 0) { // left click
	    switch (buttonX) {
	    case 0: // Left side clicked
		switch (buttonY) {
		case 0: // 
		    if (this.player.getAlignment() == Alignment.SOVIET) {
			// TODO: add allied building page
			if (this.sideBarCategoriesOpened[0][1]) {
			    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
			    switchPage(PAGE_BUILDING_SOVIET);
			}
		    }
		    break;

		case 1:
		    if ((!player.getBase().isCurrentVehicleBuilding() && !player.getBase().isCurrentVehicleHold())) {
			if (this.sideBarCategoriesOpened[1][0]) {
			    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
			    switchPage(PAGE_VEHICLE);
			}
		    } else if (player.getBase().isCurrentVehicleBuilding() && player.getBase().isCurrentVehicleHold()  && !player.getBase().isCurrentVehicleDeployed()) { // continue holded building
			player.getBase().setCurrentVehicleHold(false);
			SoundManager.getInstance().playSpeechSoundGlobal("abldgin1");
		    } else if (player.getBase().isCurrentVehicleBuilding() && player.getBase().isCurrentVehicleDeployed()) { 
			player.getBase().startBuildVehicle(player.getBase().getCurrentVehicleButton());
			SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
			SoundManager.getInstance().playSpeechSoundGlobal("abldgin1");
		    } else if (player.getBase().isCurrentVehicleBuilding() && !player.getBase().isCurrentVehicleDeployed()) {
			SoundManager.getInstance().playSpeechSoundGlobal("progres1");
		    }
		}
	    }
	} else if (button == 1) { // right click
	    switch (buttonX) {
	    case 0: // left side clicked
		switch (buttonY) {
		case 1: // second line (War Factory) clicked
		    if (player.getBase().isCurrentVehicleBuilding()) {
			if (!player.getBase().isCurrentVehicleHold() && !player.getBase().isCurrentVehicleReady() && !player.getBase().isCurrentVehicleDeployed()) {
			    player.getBase().setCurrentVehicleHold(true);
			    SoundManager.getInstance().playSpeechSoundGlobal("onhold1");
			} else if ((player.getBase().isCurrentVehicleReady() && !player.getBase().isCurrentVehicleDeployed()) || player.getBase().isCurrentVehicleHold()) {
			    SoundManager.getInstance().playSpeechSoundGlobal("cancld1");
			    player.getBase().cancelCurrentVehicle(true);
			} else if (player.getBase().isCurrentVehicleReady() || player.getBase().isCurrentVehicleDeployed()) {
			    if (this.sideBarCategoriesOpened[1][0]) { 
				SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
				switchPage(PAGE_VEHICLE); 
			    }
			}
		    }
		    break;  
		}
		break;
	    }
	}
    }

    public void mouseClicked(int button, int x, int y) {
	// Transform absolute mouse coordinates to sidebar-relative
	int barX = Main.getInstance().getContainer().getWidth() - x - BAR_SPACING_W;
	int barY = y - BAR_SPACING_H;

	// Transform click coordinates to button coordinates
	int buttonX, buttonY;

	if (currentPage == null) {
	    buttonX = 1 - (barX / 64);
	    buttonY = (y - MENU_START_Y) / 48;

	    startPageClick(button, buttonX, buttonY);
	} else {
	    if (button == 1) {
		SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		switchPage("start");
		return;
	    }

	    buttonX = 1 - (barX / 64);
	    buttonY = barY / 48;

	    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
	    this.currentPage.mouseClick(buttonX, buttonY);
	}

	System.out.println("[" + barX + "; " + barY + "] Button-" + button + " clicked: " + buttonX + " " + buttonY);
    }

    public void switchPage(String pageName) {
	this.currentPage = this.sideBarPages.get(pageName);
	this.currentPageName = pageName;
    }


    public Point cellToMinimapPixel(Point p)
    {
	Point viewOrigin = new Point(this.previewOrigin.getMinX(), this.previewOrigin.getMinY());
	Point mapOrigin = new Point(Main.getInstance().getWorld().getMap().getBounds().getMinX() / 24, Main.getInstance().getWorld().getMap().getBounds().getMinY() / 24);

	return new Point(viewOrigin.getMinX() + this.radarRect.getWidth() / 24 * previewScale * (p.getX()- mapOrigin.getMinX()), viewOrigin.getMinY() + this.radarRect.getHeight() / 24 * previewScale * (p.getMinY() - mapOrigin.getMinY()));
    }

    public Point minimapPixelToCell(Point p)
    {
	Point viewOrigin = new Point(this.radarRect.getMinX(), this.radarRect.getMinY());
	Point mapOrigin = new Point(Main.getInstance().getWorld().getMap().getBounds().getMinX(), Main.getInstance().getWorld().getMap().getBounds().getMinY());

	return new Point(mapOrigin.getMinX() + (1f / previewScale) * (p.getMinX() - viewOrigin.getMinX()), mapOrigin.getMinY() + (1f / previewScale) * (p.getMinY() - viewOrigin.getMinY()));
    }    
}
