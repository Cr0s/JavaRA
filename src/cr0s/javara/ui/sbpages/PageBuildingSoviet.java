package cr0s.javara.ui.sbpages;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.building.EntityBarracks;
import cr0s.javara.entity.building.EntityPowerPlant;
import cr0s.javara.entity.building.EntityProc;
import cr0s.javara.main.Main;

public class PageBuildingSoviet extends SideBarPage {
    
    public PageBuildingSoviet(Point pos) {
	super(pos);
	
	//String description, String textureName, Point pagePos, int posX, int posY, boolean aIsVisible
	addButton(new BuildingSidebarButton("SAM Site", "samicon.shp", this.getPosition(), 0, 0, false, null));
	addButton(new BuildingSidebarButton("Atom Bomb Silo", "msloicon.shp", this.getPosition(), 1, 0, false, null));
	
	addButton(new BuildingSidebarButton("Tesla Coil", "tslaicon.shp", this.getPosition(), 0, 1, false, null));
	addButton(new BuildingSidebarButton("Iron Curtain", "ironicon.shp", this.getPosition(), 1, 1, false, null));
	
	addButton(new BuildingSidebarButton("Flame Tower", "fturicon.shp", this.getPosition(), 0, 2, false, null));
	addButton(new BuildingSidebarButton("Soviet Tech Center", "stekicon.shp", this.getPosition(), 1, 2, false, null));
	
	addButton(new BuildingSidebarButton("Air Field", "afldicon.shp", this.getPosition(), 0, 3, false, null));
	addButton(new BuildingSidebarButton("Kennel", "kennicon.shp", this.getPosition(), 1, 3, false, null));
	
	addButton(new BuildingSidebarButton("Helipad", "hpadicon.shp", this.getPosition(), 0, 4, false, null));
	addButton(new BuildingSidebarButton("Radar Dome", "domeicon.shp", this.getPosition(), 1, 4, false, null));
	
	addButton(new BuildingSidebarButton("Sub Pen", "spenicon.shp", this.getPosition(), 0, 5, false, null));
	addButton(new BuildingSidebarButton("Service Depot", "fixicon.shp", this.getPosition(), 1, 5, false, null));
	
	addButton(new BuildingSidebarButton("War Factory", "weapicon.shp", this.getPosition(), 0, 6, false, null));
	addButton(new BuildingSidebarButton("Ore Silo", "siloicon.shp", this.getPosition(), 1, 6, false, null));
	
	addButton(new BuildingSidebarButton("Advanced Power Plant", "apwricon.shp", this.getPosition(), 0, 7, false, null));
	addButton(new BuildingSidebarButton("Ore Refinery", "procicon.shp", this.getPosition(), 1, 7, true, new EntityProc(0, 0, Main.getInstance().getTeam(), Main.getInstance().getPlayer())));
	
	addButton(new BuildingSidebarButton("Power Plant", "powricon.shp", this.getPosition(), 0, 8, true, new EntityPowerPlant(0, 0, Main.getInstance().getTeam(), Main.getInstance().getPlayer())));
	addButton(new BuildingSidebarButton("Barracks", "barricon.shp", this.getPosition(), 1, 8, true, new EntityBarracks(0, 0, Main.getInstance().getTeam(), Main.getInstance().getPlayer())));
	
	addButton(new BuildingSidebarButton("Wired Fence", "fencicon.shp", this.getPosition(), 0, 9, true, null));
	addButton(new BuildingSidebarButton("Concrete Wall", "brikicon.shp", this.getPosition(), 1, 9, true, null));
    }

    @Override
    public void render(Graphics g, Color filterColor) {
	super.render(g, filterColor);
    }

    @Override
    public void update(int delta) {
	// TODO Auto-generated method stub

    }

    @Override
    public void buttonClicked(SideBarItemsButton button) {
	System.out.println("[PageBuildingSoviet] Button clicked: " + button.getDescription());
	BuildingSidebarButton bsb = ((BuildingSidebarButton)button);
	
	if (bsb.getTargetBuilding() != null) {
	    Main.getInstance().getBuildingOverlay().setBuildingMode(bsb.getTargetBuilding());
	}
    }

}