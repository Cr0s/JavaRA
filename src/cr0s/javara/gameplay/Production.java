package cr0s.javara.gameplay;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.main.Main;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
import cr0s.javara.util.PointsUtil;

public class Production {
    private EntityActor targetActor;
    private String targetActorTexture;

    private Player player;

    private final int DEPLOY_WAIT_TIME_TICKS = 5;
    private int ticksBeforeDeploy;

    private int maxBuildedTicks;
    private int buildedTicks;
    private float currentBuildingProgress;

    private boolean isBuilding;
    private boolean isReady;
    private boolean isDeployed;
    private boolean isOnHold;

    private int buildingCost;
    private int cashSpent;

    private final float COST_TO_TICKS = 0.4f;

    private Color progressHideColor = new Color(0, 0, 0, 128);

    private SideBarItemsButton button;

    private boolean notifiedNoFunds = false;
    private final int NO_FUNDS_INTERVAL = 300;
    private int ticksBeforeNotifyNoFunds = NO_FUNDS_INTERVAL;
    
    private final boolean INSTANT_BUILD = false;
    
    public Production(Player p) {
	this.player = p;
    }

    public void startBuildingActor(EntityActor target, SideBarItemsButton btn) {
	this.targetActor = target;
	this.button = btn;

	if (this.targetActor instanceof IHaveCost) {
	    this.buildingCost = ((IHaveCost) this.targetActor).getBuildingCost();
	} else {
	    this.buildingCost = 0;
	}

	this.buildedTicks = 0;
	this.maxBuildedTicks = this.buildingCost * (20 * 60) / 1000;
	this.maxBuildedTicks = (int) (this.buildingCost * COST_TO_TICKS);

	if (this.INSTANT_BUILD) { 
	    this.maxBuildedTicks = 1;
	}	
	
	this.isBuilding = true;
	this.isReady = false;
	this.isDeployed = false;
    }

    public void update() {
	if (this.isBuilding && !this.isReady) {
	    if (this.isOnHold) {
		return;
	    }

	    this.currentBuildingProgress = (this.buildedTicks * 1f) / this.maxBuildedTicks * 1f;

	    int cashPerTick = (this.buildingCost - this.cashSpent) / (this.maxBuildedTicks - this.buildedTicks);

	    // Insufficient funds
	    if (cashPerTick != 0 && !this.player.getBase().takeCash(cashPerTick)) {
		if (!this.notifiedNoFunds) {
		    SoundManager.getInstance().playSpeechSoundGlobal("nofunds1");
		    this.notifiedNoFunds = true;
		}
		return;
	    }

	    this.notifiedNoFunds = false;
	    this.cashSpent += cashPerTick;
	    this.buildedTicks++;

	    if (this.buildedTicks == this.maxBuildedTicks) {
		this.isReady = true;
		this.isDeployed = false;

		if (!(this.targetActor instanceof EntityBuilding)) {
		    this.ticksBeforeDeploy = DEPLOY_WAIT_TIME_TICKS;
		    
		    SoundManager.getInstance().playSpeechSoundGlobal("unitrdy1");
		    this.isBuilding = false;
		} else {
		    this.isBuilding = false;
		    
		    if (this.player == Main.getInstance().getPlayer()) {
			SoundManager.getInstance().playSpeechSoundGlobal("conscmp1");
		    }
		}
	    }
	} else if (!this.isBuilding && this.isReady && !this.isDeployed) {
	    if (!(this.targetActor instanceof EntityBuilding) && --this.ticksBeforeDeploy <= 0) {
		this.deployCurrentActor();

		this.isDeployed = true;
	    }
	}
    }

    public void deployCurrentActor() {
	if (this.targetActor instanceof EntityBuilding) {
	    this.isDeployed = true;
	    this.isReady = false;
	}
	if (this.targetActor instanceof EntityVehicle) {
	    this.isReady = false;
	    this.isDeployed = true;
	    this.player.getBase().deployBuildedVehicle((EntityVehicle) targetActor);
	} else if (this.targetActor instanceof EntityInfantry) {
	    this.isReady = false;
	    this.isDeployed = true;
	    this.player.getBase().deployTrainedInfantry((EntityInfantry) targetActor);
	}
    }

    public void setOnHold(boolean hold) {
	this.isOnHold = hold;
    }

    public void cancel(boolean moneyback) {
	if (moneyback) {
	    this.player.getBase().gainCash(this.cashSpent);
	}

	resetTargetActor();
    }

    public void resetTargetActor() {
	this.targetActor = null;

	this.isBuilding = false;
	this.isReady = false;
	this.isOnHold = false;
	this.maxBuildedTicks = 0;
	this.buildedTicks = 0;
	this.cashSpent = 0;
	this.buildingCost = 0;
    }

    public boolean isBuilding() {
	return isBuilding;
    }

    public boolean isReady() {
	return this.isReady;
    }

    public boolean isOnHold() {
	return this.isOnHold;
    }

    public void drawProductionButton(Graphics g, float x, float y, Color filterColor, boolean withProgress) {
	this.button.getTexture().draw(x, y, filterColor);

	if (withProgress) { // Draw progress rect
	    Color pColor = g.getColor();

	    g.setColor(this.progressHideColor.multiply(filterColor));
	    g.fillRect(x, y, 64, 48 - 48 * currentBuildingProgress);
	    g.setColor(Color.white.multiply(filterColor));

	    // Draw status
	    if (this.isReady && !this.isDeployed) {
		g.drawString("ready", x + (32 - g.getFont().getWidth("ready") / 2), y + 46 - g.getFont().getLineHeight());
	    } else if (this.isOnHold) {
		g.drawString("hold", x + (32 - g.getFont().getWidth("hold") / 2), y + 46 - g.getFont().getLineHeight());
	    }

	    g.setColor(pColor);
	}
    }

    public EntityActor getTargetActor() {
	return this.targetActor;
    }

    public void restartBuilding() {
	this.isBuilding = false;
	this.isReady = false;
	this.isOnHold = false;
	this.maxBuildedTicks = 0;
	this.buildedTicks = 0;
	this.cashSpent = 0;
	this.buildingCost = 0;
	
	startBuildingActor(this.targetActor, this.button);
    }
    
    public boolean isDeployed() {
	return this.isDeployed;
    }
}
