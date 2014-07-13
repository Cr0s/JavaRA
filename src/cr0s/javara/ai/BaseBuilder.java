package cr0s.javara.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cr0s.javara.ai.AIPlayer.BuildingType;
import cr0s.javara.entity.IDefense;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.building.common.EntityProc;
import cr0s.javara.util.Pos;

public class BaseBuilder {
    private AIPlayer ai;

    private int waitTicks;
    private boolean isActive = false;

    public BaseBuilder(AIPlayer ai) {
	this.ai = ai;
    }

    public void update() {
	if (--this.waitTicks > 0) {
	    return;
	}

	this.isActive = tickQueue(); 
	this.waitTicks = (this.isActive) ? this.ai.structureProductionActiveDelay : this.ai.structureProductionInactiveDelay;
    }

    private boolean tickQueue() {
	EntityActor currentBuilding = this.ai.getBase().getProductionQueue().getCurrentProducingBuilding();

	// We can build something
	if (currentBuilding == null) {
	    EntityActor item = chooseBuildingToBuild();

	    if (item == null) {
		return false;
	    }

	    this.ai.getBase().getProductionQueue().getProductionForBuilding(item).startBuildingActor(item, null);
	} else if (this.ai.getBase().getProductionQueue().getProductionForBuilding(currentBuilding).isReady())  {
	    // We can place ready building somewhere
	    BuildingType type = BuildingType.BUILDING;

	    if (currentBuilding instanceof IDefense) {
		type = BuildingType.DEFENSE;
	    } else if (currentBuilding instanceof EntityProc) {
		type = BuildingType.REFINERY;
	    }

	    Pos location = this.ai.chooseBuildLocation(currentBuilding.getName(), true, type);
	    if (location == null) { // Nowhere to place, cancel
		this.ai.getBase().getProductionQueue().getProductionForBuilding(currentBuilding).cancel(true);
	    } else {
		if (this.ai.getBase().tryToBuild(location, (EntityBuilding) currentBuilding)) {
		    this.ai.getBase().getProductionQueue().getProductionForBuilding(currentBuilding).resetTargetActor();
		    return true;
		}
	    }
	}

	return false;
    }

    private EntityBuilding getProducibleBuilding(String commonName) {
	String[] actorNames = this.ai.buildingCommonNames.get(commonName);	
	if (actorNames == null) {
	    System.err.println("[AI] Cannot find common names for: " + commonName);
	    return null;
	}

	List<String> actors = Arrays.asList(actorNames); 
	ArrayList<EntityBuilding> available = new ArrayList<EntityBuilding>();

	for (EntityActor a : this.ai.getBase().getProductionQueue().getBuildables().values()) {
	    if (!(a instanceof EntityBuilding)) {
		continue;
	    }

	    if (actors.contains(a.getName()) && checkBuildingLimit(a.getName())) {
		available.add((EntityBuilding) a);
	    }
	}

	// Get random building from availables
	if (!available.isEmpty()) {
	    return available.get(this.ai.rnd.nextInt(available.size()));
	} else {
	    return null;
	}

    }

    /**
     * Checks that number of buildings of specified type does not exceeds a specified limit
     * If limit is not specified at all, check returns true
     * @param building name of building to check
     * @return result of checking
     */
    private boolean checkBuildingLimit(String building) {
	if (this.ai.buildingLimits.containsKey(building)) {
	    Integer limit = this.ai.buildingLimits.get(building);

	    if (this.ai.countBuildings(building, this.ai) < limit) {
		return true;
	    } else {
		return false;
	    }
	}	
	
	return true;
    }
    
    private EntityActor chooseBuildingToBuild() {
	// At first we need to out from low power situation
	if (this.ai.getBase().isLowPower() || this.ai.getBase().getPowerLevel() == 0) {
	    EntityBuilding power = this.getProducibleBuilding("Power");

	    if (power != null) {
		System.out.println("[AI] Building power plant: " + power.getName());
		return power;
	    }
	}

	// Next is a build up a strong economy
	if (!this.ai.hasAdequateProc() || !this.ai.hasMinimumProc()) {
	    EntityBuilding proc = this.getProducibleBuilding("Refinery");
	    if (proc != null) {
		System.out.println("[AI] Building ore refinery");
		return proc;
	    }
	}

	// Make sure that we can can spend as fast as we are earning
	if (this.ai.newProductionCashThreshold > 0 && this.ai.getBase().getCash() > this.ai.newProductionCashThreshold) {
	    EntityBuilding production = this.getProducibleBuilding("Production");

	    if (production != null) {
		System.out.println("[AI] Building production: " + production.getName());
		return production;
	    }
	}

	// Create some head room for resource storage
	if (this.ai.getBase().isSilosNeeded()) {
	    EntityBuilding silo = this.getProducibleBuilding("Silo");

	    if (silo != null) {
		System.out.println("[AI] Building ore silo");
		return silo;
	    }
	}

	// Build everything else
	ArrayList<String> keys = new ArrayList(this.ai.buildingFractions.keySet());
	Collections.shuffle(keys, this.ai.rnd);
	for (String key : keys) {
	    // Can we build this structure?
	    if (!this.ai.getBase().getProductionQueue().isBuildable(key + "icon.shp")) {
		continue;
	    }

	    // Do we want to build this structure?
	    int count = this.ai.countBuildings(key, this.ai);
	    if (count > this.ai.buildingFractions.get(key) * ai.getBase().getBuildings().size() 
		    || !checkBuildingLimit(key)) {
		continue;
	    }

	    // Will this put us into low power?
	    EntityBuilding a = (EntityBuilding) this.ai.getBase().getProductionQueue().getBuildableActorByName(key);
	    if ((a instanceof IPowerConsumer) 
		    && (this.ai.getBase().isLowPower() || this.ai.getBase().getPowerLevel() - this.ai.getBase().getConsumptionLevel() < ((IPowerConsumer) a).getConsumptionLevel())) {
		// Try to build a power plant instead
		EntityBuilding power = this.getProducibleBuilding("Power");

		if (power != null) {
		    System.out.println("[AI] Building power plant: " + power.getName());
		    return power;
		}		
	    }
	    
	    // Lets build this
	    System.out.println("[AI] Building: " + a.getName());
	    return a;
	}

	//System.out.println("[AI] Can't decide what to build!");
	return null;
    }
}
