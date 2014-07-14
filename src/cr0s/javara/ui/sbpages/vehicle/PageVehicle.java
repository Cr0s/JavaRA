package cr0s.javara.ui.sbpages.vehicle;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.entity.vehicle.soviet.EntityHeavyTank;
import cr0s.javara.entity.vehicle.soviet.EntityMammothTank;
import cr0s.javara.entity.vehicle.soviet.EntityV2Launcher;
import cr0s.javara.main.Main;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.GameSideBar;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
import cr0s.javara.ui.sbpages.SideBarPage;
import cr0s.javara.ui.sbpages.building.BuildingSidebarButton;

public class PageVehicle extends SideBarPage {

    public PageVehicle(Point pos) {
	super(pos);

	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 0, false));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 0, false));	
	
	addButton(new VehicleSidebarButton("Demolition truck", "dtrkicon.shp", this.getPosition(), 0, 1, false));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 1, false));
	
	addButton(new VehicleSidebarButton("Tesla tank", "ttnkicon.shp", this.getPosition(), 0, 2, false));
	addButton(new VehicleSidebarButton("MAD tank", "qtnkicon.shp", this.getPosition(), 1, 2, false));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 3, false));
	addButton(new VehicleSidebarButton("Mobile Construction Vehicle", "mcvicon.shp", this.getPosition(), 1, 3, false));
	
	addButton(new VehicleSidebarButton("Mammonth tank", "4tnkicon.shp", this.getPosition(), 0, 4, false));
	addButton(new VehicleSidebarButton("Mine layer", "mnlyicon.shp", this.getPosition(), 1, 4, false));
	
	addButton(new VehicleSidebarButton("Heavy tank", "3tnkicon.shp", this.getPosition(), 0, 5, false));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 5, false));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 6, false));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 6, false));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 7, false));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 7, false));
	
	addButton(new VehicleSidebarButton("V2 Rocket Launcher", "v2rlicon.shp", this.getPosition(), 0, 8, false));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 8, false));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 9, false));
	addButton(new VehicleSidebarButton("Ore truck", "harvicon.shp", this.getPosition(), 1, 9, false));
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
