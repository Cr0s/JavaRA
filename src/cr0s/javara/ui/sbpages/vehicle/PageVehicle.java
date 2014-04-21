package cr0s.javara.ui.sbpages.vehicle;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.entity.vehicle.tank.EntityHeavyTank;
import cr0s.javara.main.Main;
import cr0s.javara.ui.GameSideBar;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
import cr0s.javara.ui.sbpages.SideBarPage;
import cr0s.javara.ui.sbpages.building.BuildingSidebarButton;

public class PageVehicle extends SideBarPage {

    public PageVehicle(Point pos) {
	super(pos);

	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 0, false, null));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 0, false, null));	
	
	addButton(new VehicleSidebarButton("Demolition truck", "dtrkicon.shp", this.getPosition(), 0, 1, false, null));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 1, false, null));
	
	addButton(new VehicleSidebarButton("Tesla tank", "ttnkicon.shp", this.getPosition(), 0, 2, false, null));
	addButton(new VehicleSidebarButton("MAD tank", "qtnkicon.shp", this.getPosition(), 1, 2, false, null));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 3, false, null));
	addButton(new VehicleSidebarButton("Mobile Construction Vehicle", "mcvicon.shp", this.getPosition(), 1, 3, true, new EntityMcv(0.0f, 0.0f, Main.getInstance().getTeam(), Main.getInstance().getPlayer())));
	
	addButton(new VehicleSidebarButton("Mammonth tank", "4tnkicon.shp", this.getPosition(), 0, 4, false, null));
	addButton(new VehicleSidebarButton("Mine layer", "mnlyicon.shp", this.getPosition(), 1, 4, false, null));
	
	addButton(new VehicleSidebarButton("Heavy tank", "3tnkicon.shp", this.getPosition(), 0, 5, true, new EntityHeavyTank(0f, 0f, Main.getInstance().getTeam(), Main.getInstance().getPlayer())));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 5, false, null));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 6, false, null));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 6, false, null));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 7, false, null));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 7, false, null));
	
	addButton(new VehicleSidebarButton("V2 Rocket Launcher", "v2rlicon.shp", this.getPosition(), 0, 8, false, null));
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 1, 8, false, null));
	
	//addButton(new VehicleSidebarButton("", "icon.shp", this.getPosition(), 0, 9, false, null));
	addButton(new VehicleSidebarButton("Ore truck", "harvicon.shp", this.getPosition(), 1, 9, true, new EntityHarvester(0.0f, 0.0f, Main.getInstance().getTeam(), Main.getInstance().getPlayer())));
    }

    @Override
    public void update(int delta) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void buttonClicked(SideBarItemsButton button) {
	if (button instanceof VehicleSidebarButton) {
	    VehicleSidebarButton vsb = (VehicleSidebarButton) button;
	    
	    if (vsb.getTargetVehicle() != null) {
		if (!Main.getInstance().getPlayer().getBase().isCurrentVehicleBuilding()) {
		    Main.getInstance().getPlayer().getBase().startBuildVehicle(vsb);
		    
		    Main.getInstance().getSideBar().switchPage(GameSideBar.START_PAGE_NAME);
		}
	    }
	}
    }

}
