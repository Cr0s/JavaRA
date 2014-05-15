package cr0s.javara.gameplay;

import java.util.HashMap;
import java.util.LinkedList;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityAdvPowerPlant;
import cr0s.javara.entity.building.EntityBarracks;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityOreSilo;
import cr0s.javara.entity.building.EntityPowerPlant;
import cr0s.javara.entity.building.EntityProc;
import cr0s.javara.entity.building.EntityRadarDome;
import cr0s.javara.entity.building.EntityWarFactory;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
import cr0s.javara.ui.sbpages.building.BuildingSidebarButton;

public class ProductionQueue {
    private Base base;
    private Player player;

    private HashMap<String, EntityActor> sovietBuildings = new HashMap<>();
    private HashMap<String, EntityActor> alliedBuildings = new HashMap<>();

    private HashMap<String, EntityActor> sovietVehicles = new HashMap<>();
    private HashMap<String, EntityActor> alliedVehicles = new HashMap<>();

    private HashMap<String, EntityActor> sovietInfantry = new HashMap<>();
    private HashMap<String, EntityActor> alliedInfantry = new HashMap<>();

    private HashMap<String, EntityActor> buildables = new HashMap<>();

    private Production currentSovietBuilding, currentAlliedBuilding, currentNeutralBuilding, currentVehicle, currentInfantry;

    public ProductionQueue(Player p) {
	this.player = p;
	this.base = p.getBase();

	this.currentNeutralBuilding = new Production(this.player);
	this.currentSovietBuilding = new Production(this.player);
	this.currentAlliedBuilding = new Production(this.player);
	this.currentVehicle = new Production(this.player);
	this.currentInfantry = new Production(this.player);

	/*
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
	 */
	//this.sovietBuildings.put("fencicon.shp", ...);
	//this.sovietBuildings.put("brikicon.shp", ...);

	this.sovietBuildings.put("powricon.shp", new EntityPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("barricon.shp", new EntityBarracks(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("procicon.shp", new EntityProc(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("apwricon.shp", new EntityAdvPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("siloicon.shp", new EntityOreSilo(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("weapicon.shp", new EntityWarFactory(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("domeicon.shp", new EntityRadarDome(0f, 0f, this.player.getTeam(), this.player));
	
	this.alliedBuildings.put("powricon.shp", new EntityPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	//this.sovietBuildings.put("tenticon.shp", new EntityTent(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("procicon.shp", new EntityProc(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("apwricon.shp", new EntityAdvPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("siloicon.shp", new EntityOreSilo(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("weapicon.shp", new EntityWarFactory(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("domeicon.shp", new EntityRadarDome(0f, 0f, this.player.getTeam(), this.player));
	
	this.buildables.put("powricon.shp", this.sovietBuildings.get("powricon.shp"));
    }

    public EntityActor getBuildableActor(SideBarItemsButton texture) {
	return this.buildables.get(texture.getTextureName());
    }

    public void startBuildingActor(EntityActor target, SideBarItemsButton texture) {
	if (target instanceof EntityBuilding) {
	    getProductionForBuilding(target).startBuildingActor(target, texture);
	} else if (target instanceof EntityVehicle) {
	    this.currentVehicle.startBuildingActor(target, texture);
	} else if (target instanceof EntityInfantry) {
	    this.currentInfantry.startBuildingActor(target, texture);
	}
    }
    
    public EntityActor getCurrentProducingBuilding() {
	if (this.currentAlliedBuilding.getTargetActor() != null) {
	    return this.currentAlliedBuilding.getTargetActor();
	} else if (this.currentSovietBuilding.getTargetActor() != null) {
	    return this.currentSovietBuilding.getTargetActor();
	} else if (this.currentNeutralBuilding.getTargetActor() != null) {
	    return this.currentNeutralBuilding.getTargetActor();
	}
	
	return null;
    }

    public Production getProductionForBuilding(EntityActor target) {
	switch (target.unitProductionAlingment) {
	case NEUTRAL:
	    return this.currentNeutralBuilding;

	case SOVIET:
	    return this.currentSovietBuilding;

	case ALLIED:
	    return this.currentAlliedBuilding;
	}	

	return null;
    }

    public void update() {
	updateBuildables();
	
	this.currentNeutralBuilding.update();
	this.currentSovietBuilding.update();
	this.currentAlliedBuilding.update();

	this.currentVehicle.update();
	this.currentInfantry.update();
    }

    private void updateBuildables() {
	this.buildables.clear();
	
	addToBuildablesFrom(this.alliedBuildings);
	addToBuildablesFrom(this.sovietBuildings);
	
	addToBuildablesFrom(this.sovietVehicles);
	addToBuildablesFrom(this.alliedVehicles);
	
	addToBuildablesFrom(this.sovietInfantry);
	addToBuildablesFrom(this.sovietInfantry);
    }

    private void addToBuildablesFrom(HashMap<String, EntityActor> map) {
	if (map.isEmpty()) {
	    return;
	}
	
	for (String key : map.keySet()) {
	    EntityActor value = map.get(key);
	
	    if (this.isPrerequisitesSatisfiedFor(value)) {
		this.buildables.put(key, value);
	    }
	}	
    }
    
    public Production getCurrentSovietBuildingProduction() {
	return this.currentSovietBuilding;
    }

    public Production getCurrentAlliedBuildingProduction() {
	return this.currentAlliedBuilding;
    }    

    public Production getCurrentVehicleProduction() {
	return this.currentVehicle;
    }

    public Production getCurrentInfantryProduction() {
	return this.currentInfantry;
    }    

    public Production getCurrentNeutralBuildingProduction() {
	return this.currentNeutralBuilding;
    }

    public boolean isPrerequisitesSatisfiedFor(EntityActor e) {
	for (Class c : e.requiredToBuild) {
	    
	    if (!this.player.getBase().getBuildingClasses().contains(c)) {
		return false;
	    }
	}
	
	return true;
    }
    
    public boolean canBuild(EntityActor targetBuilding) {
	if (targetBuilding == null || this.buildables == null) {
	    return false;
	}
	
	for (EntityActor e : this.buildables.values()) {
	    if (targetBuilding.getClass().equals(e.getClass())) {
		return true;
	    }
	}
	
	return false;
    }
    
    public boolean isBuildable(String name) {
	if ( this.buildables == null || this.buildables.isEmpty()) {
	    return false;
	}
	
	return this.buildables.containsKey(name);
    }    
    
    public EntityActor getCurrentProducingVehicle() {
	return this.currentVehicle.getTargetActor();
    }
}
