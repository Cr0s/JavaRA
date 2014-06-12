package cr0s.javara.ui.sbpages.building;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.building.common.EntityAdvPowerPlant;
import cr0s.javara.entity.building.common.EntityOreSilo;
import cr0s.javara.entity.building.common.EntityPowerPlant;
import cr0s.javara.entity.building.common.EntityProc;
import cr0s.javara.entity.building.common.EntityRadarDome;
import cr0s.javara.entity.building.common.EntityWarFactory;
import cr0s.javara.entity.building.soviet.EntityBarracks;
import cr0s.javara.gameplay.Base;
import cr0s.javara.main.Main;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
import cr0s.javara.ui.sbpages.SideBarPage;

public class PageBuildingSoviet extends SideBarPage {
    
    public PageBuildingSoviet(Point pos) {
	super(pos);
	
	addButton(new BuildingSidebarButton("SAM Site", "samicon.shp", this.getPosition(), 0, 0, false));
	addButton(new BuildingSidebarButton("Atom Bomb Silo", "msloicon.shp", this.getPosition(), 1, 0, false));
	
	addButton(new BuildingSidebarButton("Tesla Coil", "tslaicon.shp", this.getPosition(), 0, 1, false));
	addButton(new BuildingSidebarButton("Iron Curtain", "ironicon.shp", this.getPosition(), 1, 1, false));
	
	addButton(new BuildingSidebarButton("Flame Tower", "fturicon.shp", this.getPosition(), 0, 2, false));
	addButton(new BuildingSidebarButton("Soviet Tech Center", "stekicon.shp", this.getPosition(), 1, 2, false));
	
	addButton(new BuildingSidebarButton("Air Field", "afldicon.shp", this.getPosition(), 0, 3, false));
	addButton(new BuildingSidebarButton("Kennel", "kennicon.shp", this.getPosition(), 1, 3, false));
	
	addButton(new BuildingSidebarButton("Helipad", "hpadicon.shp", this.getPosition(), 0, 4, false));
	addButton(new BuildingSidebarButton("Radar Dome", "domeicon.shp", this.getPosition(), 1, 4, false));
	
	addButton(new BuildingSidebarButton("Sub Pen", "spenicon.shp", this.getPosition(), 0, 5, false));
	addButton(new BuildingSidebarButton("Service Depot", "fixicon.shp", this.getPosition(), 1, 5, false));
	
	addButton(new BuildingSidebarButton("War Factory", "weapicon.shp", this.getPosition(), 0, 6, false));
	addButton(new BuildingSidebarButton("Ore Silo", "siloicon.shp", this.getPosition(), 1, 6, false));
	
	addButton(new BuildingSidebarButton("Advanced Power Plant", "apwricon.shp", this.getPosition(), 0, 7, false));
	addButton(new BuildingSidebarButton("Ore Refinery", "procicon.shp", this.getPosition(), 1, 7, false));
	
	addButton(new BuildingSidebarButton("Power Plant", "powricon.shp", this.getPosition(), 0, 8, false));
	addButton(new BuildingSidebarButton("Barracks", "barricon.shp", this.getPosition(), 1, 8, false));
	
	addButton(new BuildingSidebarButton("Wired Fence", "fencicon.shp", this.getPosition(), 0, 9, false));
	addButton(new BuildingSidebarButton("Concrete Wall", "brikicon.shp", this.getPosition(), 1, 9, false));
    }

    @Override
    public void render(Graphics g, Color filterColor) {
	super.render(g, filterColor);
    }

    @Override
    public void update(int delta) {
	for (SideBarItemsButton button : this.buttonsHash.values()) {
	    button.setVisible(Main.getInstance().getPlayer().getBase().getProductionQueue().isBuildable(button.getTextureName()));
	}
    }
    
    @Override
    public void buttonClicked(SideBarItemsButton button) {
	super.buttonClicked(button);
	SoundManager.getInstance().playSpeechSoundGlobal("abldgin1"); // "Building"
    }
}
