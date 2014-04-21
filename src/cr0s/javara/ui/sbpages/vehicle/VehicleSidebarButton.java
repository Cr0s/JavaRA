package cr0s.javara.ui.sbpages.vehicle;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.ui.sbpages.SideBarItemsButton;

public class VehicleSidebarButton extends SideBarItemsButton {

    private EntityVehicle targetVehicle;
    
    public VehicleSidebarButton(String aDescription, String textureName,
	    Point pagePos, int aPosX, int aPosY, boolean aIsVisible, EntityVehicle aTargetVehicle) {
	super(aDescription, textureName, pagePos, aPosX, aPosY, aIsVisible);
	
	this.targetVehicle = aTargetVehicle;
    }

    public EntityVehicle getTargetVehicle() {
	return this.targetVehicle;
    }
}
