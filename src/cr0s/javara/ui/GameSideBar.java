package cr0s.javara.ui;

import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.gameplay.Base;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Production;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.render.map.MinimapRenderer;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.sbpages.building.PageBuildingSoviet;
import cr0s.javara.ui.sbpages.infantry.PageInfantry;
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

    private Color translucentColor = new Color(255, 255, 255, 120);
    private Color opaqueColor = new Color(255, 255, 255, 255);

    private HashMap<String, SideBarPage> sideBarPages;
    private SideBarPage currentPage;
    private String currentPageName;

    public static final String START_PAGE_NAME = "start";
    public static final String PAGE_BUILDING_SOVIET = "sovbuild";
    public static final String PAGE_VEHICLE = "vehicle";
    public static final String PAGE_INFANTRY = "infantry";
    
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
	    this.sideBarPages.put(PAGE_INFANTRY, new PageInfantry(new Point(Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 1, BAR_SPACING_H + 1)));
	}
    }

    public void render(Graphics g) {
	Color pColor = g.getColor();
	g.setColor(BG_COLOR.multiply(this.getBackgroundColor()));
	g.fill(this.sidebarBounds);
	this.powerBar.render(g, this.getBackgroundColor());
	drawSideBarButtons(g);
	drawMoney(g);
	g.setColor(pColor);

    }

    public void drawMoney(Graphics g) {
	g.getFont().drawString(this.sidebarBounds.getX(), this.sidebarBounds.getY() - g.getFont().getLineHeight(), Main.getInstance().getPlayer().getBase().getDisplayCash() + Main.getInstance().getPlayer().getBase().getDisplayOre() + "$", Color.yellow.darker(0.2f));
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
	int x = Main.getInstance().getContainer().getWidth() - BAR_WIDTH - BAR_SPACING_W + 1;
	int y = MENU_START_Y;

	drawBuildingButtons(g, x, y, filterColor);
	drawWarFactoryButton(g, x, y, filterColor);
	drawBarracksButton(g, x, y, filterColor);


	for (int i = 2; i < 4; i++) {
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[i][0] ? 1 : 0, 2 * i).draw(x, y + (i * 48), filterColor);
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[i][1] ? 1 : 0, 2 * i + 1).draw(x + 64, y + (i * 48), filterColor);
	}	
    }

    private void drawBarracksButton(Graphics g, final int sX, final int sY, Color filterColor) {
	EntityActor currentActor = this.player.getBase().getProductionQueue().getCurrentInfantryProduction().getTargetActor();

	if (currentActor != null) {
	    this.player.getBase().getProductionQueue().getCurrentInfantryProduction().drawProductionButton(g, sX + 64, sY + (1 * 48), filterColor, true);
	} else {
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[1][1] ? 1 : 0, 3).draw(sX + 64, sY + (1 * 48), filterColor);
	}	
    }

    private void drawWarFactoryButton(Graphics g, final int sX, final int sY, Color filterColor) {
	EntityActor currentActor = this.player.getBase().getProductionQueue().getCurrentProducingVehicle();

	if (currentActor != null) {
	    this.player.getBase().getProductionQueue().getCurrentVehicleProduction().drawProductionButton(g, sX, sY + (1 * 48), filterColor, true);
	} else {
	    this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[1][0] ? 1 : 0, 2).draw(sX, sY + (1 * 48), filterColor);
	}
    }

    private void drawBuildingButtons(Graphics g, int sX, int sY, Color filterColor) {
	boolean isSovietLeft = this.player.getAlignment() == Alignment.SOVIET;

	EntityActor currentActor = this.getPlayer().getBase().getProductionQueue().getCurrentProducingBuilding();
	if (currentActor != null) {
	    if (!isSovietLeft && currentActor.unitProductionAlingment != Alignment.SOVIET) {
		sX += 64;
	    } else if (isSovietLeft && currentActor.unitProductionAlingment == Alignment.ALLIED) {
		sX += 64;
	    }

	    this.getPlayer().getBase().getProductionQueue().getProductionForBuilding(currentActor).drawProductionButton(g, sX, sY, filterColor, true);

	    // Draw blackouted right texture
	    if (isSovietLeft) {
		this.menuCategoriesSheet.getSubImage(0, 1).draw(sX + 64, sY, filterColor);
	    } else {
		this.menuCategoriesSheet.getSubImage(0, 0).draw(sX + 64, sY, filterColor);
	    }
	} else {
	    if (isSovietLeft) {
		// Draw soviet, then allied
		this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][1] ? 1 : 0, 1).draw(sX, sY, filterColor);
		this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][0] ? 1 : 0, 0).draw(sX + 64, sY, filterColor);
	    } else {
		// Draw allied, then soviet
		this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][0] ? 1 : 0, 0).draw(sX, sY, filterColor);
		this.menuCategoriesSheet.getSubImage(this.sideBarCategoriesOpened[0][1] ? 1 : 0, 1).draw(sX + 64, sY, filterColor);
	    }	    
	}
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

    public void productionButtonClick(Production production, int button, int buttonX, int buttonY) {
	if (button == 0) {
	    if (production.isOnHold()) {
		production.setOnHold(false);

		SoundManager.getInstance().playSpeechSoundGlobal("abldgin1"); // "Building"
	    } else {
		if (production.getTargetActor() instanceof EntityBuilding) {
		    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);

		    if (production.isReady() && !production.isBuilding()) {
			Main.getInstance().getBuildingOverlay().setBuildingMode((EntityBuilding) production.getTargetActor());
		    } else if (!production.isReady() && production.isBuilding()){
			SoundManager.getInstance().playSpeechSoundGlobal("progres1"); // "Unable to comply, building in progress"
		    } else if (!production.isReady() && !production.isBuilding()) {
			SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
			SoundManager.getInstance().playSpeechSoundGlobal("abldgin1");
			production.restartBuilding();
		    } 
		} else {
		    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		    
		    if (!production.isReady() && !production.isBuilding()) {
			if (!(production.getTargetActor() instanceof EntityInfantry)) { 
			    SoundManager.getInstance().playSpeechSoundGlobal("abldgin1");
			} else {
			    SoundManager.getInstance().playSpeechSoundGlobal("train1");
			}
			production.restartBuilding();
		    } 
		}
	    }
	} else if (button == 1) {
	    if (production.isBuilding()) {
		if (production.isOnHold() || production.isReady()) {
		    production.cancel(true);

		    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		    SoundManager.getInstance().playSpeechSoundGlobal("cancld1"); // "Canceled"
		} else if (!production.isOnHold()) {
		    production.setOnHold(true);

		    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		    SoundManager.getInstance().playSpeechSoundGlobal("onhold1"); // "On hold"
		} 
	    } else if (!production.isReady() || production.isDeployed()) {
		SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		production.resetTargetActor();
		openPageByClick(buttonX, buttonY);
	    } else {
		production.cancel(true);

		SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		SoundManager.getInstance().playSpeechSoundGlobal("cancld1"); // "Canceled"		
	    }
	}	
    }

    public void openPageByClick(int buttonX, int buttonY) {
	switch (buttonY) {
	case 0: // building productions
	    if (buttonX == 0) { // left side click
		if (this.player.getAlignment() == Alignment.SOVIET && this.sideBarCategoriesOpened[0][1]) {
		    // TODO: add allied building page
		    SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		    switchPage(PAGE_BUILDING_SOVIET);
		} else if (buttonX == 1) {

		}
	    }

	    break;

	case 1:
	    if (buttonX == 0) {
		SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		switchPage(PAGE_VEHICLE);
	    } else if (buttonX == 1) {
		SoundManager.getInstance().playSfxGlobal("ramenu1", 0.8f);
		switchPage(PAGE_INFANTRY);
	    }

	    break;
	}
    }

    public void startPageClick(int button, int buttonX, int buttonY) {
	switch (buttonY) {
	case 0: // building productions
	    if (buttonX == 0) { // left side click
		EntityActor buildingActor = player.getBase().getProductionQueue().getCurrentProducingBuilding();

		if (buildingActor != null) {
		    Production production = player.getBase().getProductionQueue().getProductionForBuilding(buildingActor);

		    productionButtonClick(production, button, buttonX, buttonY);
		} else {
		    openPageByClick(buttonX, buttonY);
		}
	    } else if (buttonX == 1) { // right side click

	    }
	    break;

	case 1:
	    if (buttonX == 0) {
		EntityActor buildingActor = player.getBase().getProductionQueue().getCurrentProducingVehicle();

		if (buildingActor != null) {
		    Production production = player.getBase().getProductionQueue().getCurrentVehicleProduction();

		    productionButtonClick(production, button, buttonX, buttonY);
		} else {
		    openPageByClick(buttonX, buttonY);
		}		
	    } else {
		EntityActor buildingActor = player.getBase().getProductionQueue().getCurrentInfantryProduction().getTargetActor();

		if (buildingActor != null) {
		    Production production = player.getBase().getProductionQueue().getCurrentInfantryProduction();

		    productionButtonClick(production, button, buttonX, buttonY);
		} else {
		    openPageByClick(buttonX, buttonY);
		}		
	    }
	    break;

	case 2:
	    break;

	case 3:
	    break;
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
