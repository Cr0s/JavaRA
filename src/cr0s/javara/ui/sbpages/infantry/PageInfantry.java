package cr0s.javara.ui.sbpages.infantry;

import org.newdawn.slick.geom.Point;

import cr0s.javara.main.Main;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.GameSideBar;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
import cr0s.javara.ui.sbpages.SideBarPage;
import cr0s.javara.ui.sbpages.building.BuildingSidebarButton;

public class PageInfantry extends SideBarPage {

    public PageInfantry(Point pos) {
	super(pos);

	addButton(new InfantrySidebarButton("Shock Trooper", "shokicon.shp", this.getPosition(), 0, 4, false, null));

	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 5, false, null));
	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 5, false, null));

	addButton(new InfantrySidebarButton("Flamethrower", "e4icon.shp", this.getPosition(), 0, 6, false, null));
	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 6, false, null));

	addButton(new InfantrySidebarButton("Rocket Soldier", "e3icon.shp", this.getPosition(), 0, 7, false, null));
	addButton(new InfantrySidebarButton("Tanya", "e7icon.shp", this.getPosition(), 1, 7, false, null));

	addButton(new InfantrySidebarButton("Grenade Trooper", "e2icon.shp", this.getPosition(), 0, 8, false, null));
	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 8, false, null));

	addButton(new InfantrySidebarButton("Riffle Trooper", "e1icon.shp", this.getPosition(), 0, 9, false, null));
	addButton(new InfantrySidebarButton("Engineer", "e6icon.shp", this.getPosition(), 1, 9, false, null));
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
	SoundManager.getInstance().playSpeechSoundGlobal("train1");
    }

}
