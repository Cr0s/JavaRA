package cr0s.javara.ui.sbpages;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.main.Main;
import cr0s.javara.ui.GameSideBar;
import cr0s.javara.ui.SideBarToolTip;
import cr0s.javara.util.Pos;

public abstract class SideBarPage {
    private ArrayList<SideBarItemsButton> buttons;
    protected HashMap<String, SideBarItemsButton> buttonsHash;

    private SideBarItemsButton currentButton;

    private boolean isCurrentButtonReady;

    private Point position;

    protected SideBarPage(Point pos) {
	this.buttons = new ArrayList<>();
	this.buttonsHash = new HashMap<>();

	this.position = pos;
    }

    public void render(Graphics g, Color filterColor) {
	for (SideBarItemsButton button : this.buttons) {
	    if (button.isVisible()) {
		button.render(g, filterColor);
	    } else {
		button.renderDisabled(g, filterColor);
	    }

	    // Get mouse position
	    int mouseX = Main.getInstance().getContainer().getInput().getMouseX();
	    int mouseY = Main.getInstance().getContainer().getInput().getMouseY();

	    // Transform absolute mouse coordinates to sidebar-relative
	    int barX = Main.getInstance().getContainer().getWidth() - mouseX - GameSideBar.BAR_SPACING_W;
	    int barY = mouseY - GameSideBar.BAR_SPACING_H;

	    int buttonX = 1 - (barX / 64);
	    int buttonY = barY / 48;

	    // Check if mouse over current button
	    if (button.posX == buttonX && button.posY == buttonY) {
		// Draw tooltip
		EntityActor a = Main.getInstance().getPlayer().getBase().getProductionQueue().getBuildableActor(button);
		
		if (a != null && (a instanceof IHaveCost)) {
		    int cost = ((IHaveCost) a).getBuildingCost();

		    SideBarToolTip.renderTooltipAt(g, new Pos(this.getPosition().getX(), this.getPosition().getY() + button.posY * 48), button.getDescription() + "\n" + "Cost: " + cost + "$");
		} else {
		    SideBarToolTip.renderTooltipAt(g, new Pos(this.getPosition().getX(), this.getPosition().getY() + button.posY * 48), button.getDescription() + "\n");
		}
	    }
	}
    }
    public abstract void update(int delta);

    public void mouseClick(int x, int y) {
	System.out.println("[SBP] Button pos: " + x + "; " + y);

	for (SideBarItemsButton btn : this.buttons) {
	    if (btn.posX == x && btn.posY == y) {
		if (btn.isVisible()) {
		    System.out.println("[SBP] Found button: " + btn.getDescription());
		    buttonClicked(btn);
		    return;
		}
	    }
	}
    }

    public SideBarItemsButton getCurrentButton() {
	return this.currentButton;
    }

    protected void addButton(SideBarItemsButton button) {
	this.buttons.add(button);
	this.buttonsHash.put(button.getTextureName(), button);
    }

    public boolean isCurrentButtonReady() {
	return this.isCurrentButtonReady();
    }

    public Point getPosition() {
	return this.position;
    }

    public SideBarItemsButton getButton(String name) {
	return this.buttonsHash.get(name);
    }

    public void buttonClicked(SideBarItemsButton button) {
	Main.getInstance().getPlayer().getBase().productButtonItem(button);	

	Main.getInstance().getSideBar().switchPage("start");
    }
}
