package cr0s.javara.ui.sbpages;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.building.EntityBuilding;

public class BuildingSidebarButton extends SideBarItemsButton {

    private EntityBuilding targetBuilding;
    
    public BuildingSidebarButton(String description, String textureName,
	    Point pagePos, int posX, int posY, boolean aIsVisible, EntityBuilding aTargetBuilding) {
	super(description, textureName, pagePos, posX, posY, aIsVisible);
	
	this.targetBuilding = aTargetBuilding;
    }

    public EntityBuilding getTargetBuilding() {
	return this.targetBuilding;
    }
    
}
