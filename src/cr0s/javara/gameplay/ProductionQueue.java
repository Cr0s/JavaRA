package cr0s.javara.gameplay;

import java.util.HashMap;
import java.util.LinkedList;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.common.EntityAdvPowerPlant;
import cr0s.javara.entity.building.common.EntityConcreteWall;
import cr0s.javara.entity.building.common.EntityOreSilo;
import cr0s.javara.entity.building.common.EntityPowerPlant;
import cr0s.javara.entity.building.common.EntityProc;
import cr0s.javara.entity.building.common.EntityRadarDome;
import cr0s.javara.entity.building.common.EntityWarFactory;
import cr0s.javara.entity.building.soviet.EntityBarracks;
import cr0s.javara.entity.building.soviet.EntityBarredWireWall;
import cr0s.javara.entity.building.soviet.EntityFireTurret;
import cr0s.javara.entity.building.soviet.EntityTeslaCoil;
import cr0s.javara.entity.infantry.EntityGrenadeTrooper;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.entity.infantry.EntityRifleTrooper;
import cr0s.javara.entity.infantry.EntityRocketTrooper;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.entity.vehicle.soviet.EntityHeavyTank;
import cr0s.javara.entity.vehicle.soviet.EntityMammothTank;
import cr0s.javara.entity.vehicle.soviet.EntityV2Launcher;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
import cr0s.javara.ui.sbpages.building.BuildingSidebarButton;
import cr0s.javara.ui.sbpages.infantry.InfantrySidebarButton;
import cr0s.javara.ui.sbpages.vehicle.VehicleSidebarButton;

public class ProductionQueue {
    private Base base;
    private Player player;

    public HashMap<String, EntityActor> sovietBuildings = new HashMap<>();
    public HashMap<String, EntityActor> alliedBuildings = new HashMap<>();

    public HashMap<String, EntityActor> neutralVehicles = new HashMap<>();
    public HashMap<String, EntityActor> sovietVehicles = new HashMap<>();
    public HashMap<String, EntityActor> alliedVehicles = new HashMap<>();

    public HashMap<String, EntityActor> sovietInfantry = new HashMap<>();
    public HashMap<String, EntityActor> alliedInfantry = new HashMap<>();

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
	this.sovietBuildings.put("fencicon.shp", new EntityBarredWireWall(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("brikicon.shp", new EntityConcreteWall(0f, 0f, this.player.getTeam(), this.player));

	this.sovietBuildings.put("powricon.shp", new EntityPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("barricon.shp", new EntityBarracks(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("procicon.shp", new EntityProc(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("apwricon.shp", new EntityAdvPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("siloicon.shp", new EntityOreSilo(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("weapicon.shp", new EntityWarFactory(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("domeicon.shp", new EntityRadarDome(0f, 0f, this.player.getTeam(), this.player));

	this.sovietBuildings.put("fturicon.shp", new EntityFireTurret(0f, 0f, this.player.getTeam(), this.player));
	this.sovietBuildings.put("tslaicon.shp", new EntityTeslaCoil(0f, 0f, this.player.getTeam(), this.player));	
	
	this.alliedBuildings.put("powricon.shp", new EntityPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	//this.sovietBuildings.put("tenticon.shp", new EntityTent(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("procicon.shp", new EntityProc(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("apwricon.shp", new EntityAdvPowerPlant(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("siloicon.shp", new EntityOreSilo(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("weapicon.shp", new EntityWarFactory(0f, 0f, this.player.getTeam(), this.player));
	this.alliedBuildings.put("domeicon.shp", new EntityRadarDome(0f, 0f, this.player.getTeam(), this.player));
	
	
	/*
	 	addButton(new InfantrySidebarButton("", "shokicon.shp", this.getPosition(), 0, 4, false, null));
	
	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 5, false, null));
	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 5, false, null));
	
	addButton(new InfantrySidebarButton("", "e4icon.shp", this.getPosition(), 0, 6, false, null));
	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 6, false, null));
	
	addButton(new InfantrySidebarButton("", "e3icon.shp", this.getPosition(), 0, 7, false, null));
	addButton(new InfantrySidebarButton("", "e7icon.shp", this.getPosition(), 1, 7, false, null));
	
	addButton(new InfantrySidebarButton("", "e2icon.shp", this.getPosition(), 0, 8, false, null));
	//addButton(new InfantrySidebarButton("", "icon.shp", this.getPosition(), 1, 8, false, null));
	
	addButton(new InfantrySidebarButton("", "e1icon.shp", this.getPosition(), 0, 9, false, null));
	addButton(new InfantrySidebarButton("", "e6icon.shp", this.getPosition(), 1, 9, false, null));
	 */
	this.sovietInfantry.put("e1icon.shp", new EntityRifleTrooper(0.0f, 0.0f, this.player.getTeam(), this.player, SubCell.CENTER));
	//this.sovietInfantry.put("e6icon.shp", new EntityEngineer(0.0f, 0.0f, this.player.getTeam(), this.player, SubCell.CENTER));
	this.sovietInfantry.put("e2icon.shp", new EntityGrenadeTrooper(0.0f, 0.0f, this.player.getTeam(), this.player, SubCell.CENTER));
	this.sovietInfantry.put("e3icon.shp", new EntityRocketTrooper(0.0f, 0.0f, this.player.getTeam(), this.player, SubCell.CENTER));
		
	// Put neutral common vehicles
	this.neutralVehicles.put("mcvicon.shp", new EntityMcv(0.0f, 0.0f, this.player.getTeam(), this.player));
	this.neutralVehicles.put("harvicon.shp", new EntityHarvester(0.0f, 0.0f, this.player.getTeam(), this.player));
	
	// Put soviet vehicles
	this.sovietVehicles.put("4tnkicon.shp", new EntityMammothTank(0.0f, 0.0f, this.player.getTeam(), this.player));
	this.sovietVehicles.put("3tnkicon.shp", new EntityHeavyTank(0.0f, 0.0f, this.player.getTeam(), this.player));
	this.sovietVehicles.put("v2rlicon.shp", new EntityV2Launcher(0.0f, 0.0f, this.player.getTeam(), this.player));

    }

    public EntityActor getBuildableActor(SideBarItemsButton texture) {
	return this.buildables.get(texture.getTextureName());
    }

    /**
     * Replicates a new copy of buildable actor by this name
     * @param name name of actor
     * @return new instance of actor
     */
    public EntityActor getBuildableActorByName(String name) {
	EntityActor a = this.buildables.get(name + "icon.shp");
	
	if (a != null) {
	    return a.newInstance();
	}
	
	return null;
    }

    public void startBuildingActor(String name, SideBarItemsButton texture) {
	EntityActor target = this.buildables.get(name);
	
	if (target instanceof EntityBuilding) {
	    getProductionForBuilding(target).startBuildingActor(target, texture);
	} else if (target instanceof EntityVehicle) {
	    this.currentVehicle.startBuildingActor(target, texture);
	} else if (target instanceof EntityInfantry) {
	    this.currentInfantry.startBuildingActor(target, texture);
	}
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
	addToBuildablesFrom(this.neutralVehicles);
	
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
	
	return this.buildables.containsKey(targetBuilding.getName() + "icon.shp");
    }
    
    public boolean isBuildable(String name) {
	if (this.buildables == null || this.buildables.isEmpty()) {
	    return false;
	}
	
	return this.buildables.containsKey(name);
    }    
    
    public HashMap<String, EntityActor> getBuildables() {
	return this.buildables;
    }
    
    public EntityActor getCurrentProducingVehicle() {
	return this.currentVehicle.getTargetActor();
    }
}
