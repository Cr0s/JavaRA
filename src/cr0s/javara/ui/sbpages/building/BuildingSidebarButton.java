package cr0s.javara.ui.sbpages.building;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.ui.sbpages.SideBarItemsButton;

public class BuildingSidebarButton extends SideBarItemsButton {
    public BuildingSidebarButton(String description, String textureName,
	    Point pagePos, int posX, int posY, boolean aIsVisible) {
	super(description, textureName, pagePos, posX, posY, aIsVisible);
    }
}
