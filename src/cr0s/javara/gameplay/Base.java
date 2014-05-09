package cr0s.javara.gameplay;

import java.util.ArrayList;

import cr0s.javara.entity.building.EntityBarracks;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.building.EntityPowerPlant;
import cr0s.javara.entity.building.EntityProc;
import cr0s.javara.entity.building.EntityWarFactory;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.building.IPowerProducer;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.render.map.TileSet;
import cr0s.javara.ui.sbpages.vehicle.VehicleSidebarButton;
/**
 * Describes player's base.
 * @author Cr0s
 */
public class Base {
    private ArrayList<EntityBuilding> buildings = new ArrayList<>();

    public static final int BUILDING_CY_RANGE = 20;
    public static final int BUILDING_NEAREST_BUILDING_DISTANCE = 5;

    public boolean isSovietCYPresent = false;
    public boolean isAlliedCYPresent = false;

    public boolean isBarracksPresent = false;
    public boolean isTentPresent = false;

    public boolean isSovietWarFactoryPresent = false;
    public boolean isAlliedWarFactoryPresent = false;

    public boolean isSubPenPresent = false;
    public boolean isShipYardPresent = false;
    public boolean isHelipadPresent = false;
    public boolean isAirLinePresent = false;

    public boolean isChronoSpherePresent = false;
    public boolean isNukeSiloPresent = false;
    public boolean isIronCurtainPresent = false;

    public boolean isSovietTechPresent = false;
    public boolean isAlliedTechPresent = false;

    public boolean isRadarDomePresent = false;
    public boolean isPowerPlantPresent = false;
    public boolean isFlameTowerPresent = false;

    public boolean isProcPresent = false;

    public boolean isAnySuperPowerPresent;    

    private boolean isLowPower = false;

    private int powerLevel = 0;
    private int powerConsumptionLevel = 0;

    private int currentOreValue = 0;
    private int maxOreValue = 0;

    private VehicleSidebarButton currentVehicleBuilding;
    private int currentVehicleProgress;
    public static final int VEHICLE_MAX_PROGRESS = 48;
    private boolean isCurrentVehicleReady;
    private boolean isCurrentVehicleHold;
    private int currentVehicleProgressTicks;
    
    public static final int WAIT_BEFORE_DEPLOY_VEHICLE = 5;
    private int waitDeployVehicleTicks;
    
    public int oreCapacity, oreValue;
    
    public Base(Team team, Player owner) {

    }

    public void update() {
	updateBuildings();
	updateCurrentVehicleBuilding();
    }

    public VehicleSidebarButton getCurrentVehicleButton() {
	return this.currentVehicleBuilding;
    }
    
    public boolean isCurrentVehicleBuilding() {
	return (this.currentVehicleBuilding != null);
    }
    
    public boolean isCurrentVehicleHold() {
	return this.isCurrentVehicleHold;
    }
    
    public boolean isCurrentVehicleReady() {
	return this.isCurrentVehicleReady;
    }
    
    public int getCurrentVehicleProgress() {
	return this.currentVehicleProgress;
    }
    
    
    public void startBuildVehicle(VehicleSidebarButton v) {
	this.currentVehicleProgress = 0;
	this.currentVehicleBuilding = v;
    }
    
    private void updateCurrentVehicleBuilding() {
	if (this.currentVehicleBuilding != null) {
	    if (!this.isCurrentVehicleHold && !this.isCurrentVehicleReady && this.currentVehicleProgressTicks++ > 80 - currentVehicleBuilding.getTargetVehicle().getBuildingSpeed()) {
		this.currentVehicleProgressTicks = 0;
		this.currentVehicleProgress++;
	    }
	    
	    if (this.currentVehicleProgress == VEHICLE_MAX_PROGRESS) {
		this.isCurrentVehicleReady = true;
	    }
	    
	    if (this.isCurrentVehicleReady && this.waitDeployVehicleTicks++ > this.WAIT_BEFORE_DEPLOY_VEHICLE) {
		this.waitDeployVehicleTicks = 0;
		
		this.deployBuildedVehicle(this.currentVehicleBuilding.getTargetVehicle());
		cancelCurrentVehicle(false);
	    }
	}
    }

    private void updateBuildings() {
	isSovietCYPresent = isAlliedCYPresent = false;
	isBarracksPresent = false;
	isTentPresent = false;
	isSovietWarFactoryPresent = isAlliedWarFactoryPresent = false;
	isSubPenPresent = false;
	isShipYardPresent = false;
	isHelipadPresent = false;
	isAirLinePresent = false;
	isChronoSpherePresent = false;
	isNukeSiloPresent = false;
	isIronCurtainPresent = false;
	isSovietTechPresent = false;
	isAlliedTechPresent = false;
	isRadarDomePresent = false;
	isProcPresent = false;

	for (EntityBuilding b : this.buildings) {
	    // Update power levels
	    if (b instanceof IPowerConsumer) {
		this.powerConsumptionLevel += ((IPowerConsumer) b).getConsumptionLevel();
	    } else if (b instanceof IPowerProducer) {
		this.powerLevel += ((IPowerProducer) b).getPowerProductionLevel();
	    }	    

	    if (b instanceof EntityConstructionYard) {
		if (((EntityConstructionYard) b).getAlignment() == Alignment.ALLIED) {
		    this.isAlliedCYPresent = true;
		} else if (((EntityConstructionYard) b).getAlignment() == Alignment.SOVIET) {
		    this.isSovietCYPresent = true;
		}
	    } else if (b instanceof EntityBarracks) {
		this.isBarracksPresent = true;
	    //} else if (b instanceof EntityTent) {
		//this.isTentPresent = true;
	    } else if (b instanceof EntityWarFactory) {
		if (((EntityWarFactory) b).getAlignment() == Alignment.ALLIED) {
		    this.isAlliedWarFactoryPresent = true;
		} else if (((EntityWarFactory) b).getAlignment() == Alignment.SOVIET) {
		    this.isSovietWarFactoryPresent = true;
		}
	    } else if (b instanceof EntityProc) {
		this.isProcPresent = true;
		
		this.oreCapacity += EntityProc.MAX_CAPACITY;
	    } else if (b instanceof EntityPowerPlant) {
		this.isPowerPlantPresent = true;
	    }


	}

	this.isLowPower = (this.powerConsumptionLevel > this.powerLevel);
    }

    public boolean isLowPower() {
	return this.isLowPower();
    }

    public int getPowerLevel() {
	return this.powerLevel;
    }

    public int getConsumptionLevel() {
	return this.powerConsumptionLevel;
    }

    public void addBuilding(EntityBuilding building) {
	this.buildings.add(building);
	
	if (building instanceof EntityWarFactory) {
	    building.setPrimary(!isMoreThanOneWarFactory());
	}
    }

    public void removeBuilding(EntityBuilding building) {
	this.buildings.remove(building);
    }

    public boolean isPossibleToBuildHere(int cellX, int cellY, EntityBuilding targetBuilding) {
	for (int bX = 0; bX < targetBuilding.getWidthInTiles(); bX++) {
	    for (int bY = 0; bY < targetBuilding.getHeightInTiles(); bY++) {
		if (targetBuilding.getBlockingCells()[bX][bY] != TileSet.SURFACE_CLEAR_ID) {
		    if (!Main.getInstance().getWorld().isCellBuildable(cellX + bX , cellY + bY)) {
			return false;
		    }
		}
	    }
	}

	return true;
    }



    public boolean tryToBuild(int cellX, int cellY,
	    EntityBuilding targetBuilding) {
	if (!isPossibleToBuildHere(cellX, cellY, targetBuilding)) {
	    return false;
	}

	if (!this.isAlliedCYPresent && !this.isSovietCYPresent) {
	    return false;
	}

	if (checkBuildingDistance(cellX, cellY)) {
	    EntityBuilding b = EntityBuilding.newInstance(targetBuilding);
	    b.changeCellPos(cellX, cellY);
	    
	    Main.getInstance().getWorld().addBuildingTo(b);

	    return true;
	} else {
	    return false;
	}
    }

    public boolean checkBuildingDistance(int cellX, int cellY) {
	// Find minimal distance to all construction yards
	int minDistanceToCYSq = 0;
	int minDistanceToOtherBuildingsSq = 0;

	for (EntityBuilding eb : this.buildings) {
	    int dx = eb.getTileX() / 24 - cellX;
	    int dy = eb.getTileY() / 24 - cellY;
	    int distanceSq = dx * dx + dy * dy;

	    if (eb instanceof EntityConstructionYard) {
		if (minDistanceToCYSq == 0 || distanceSq < minDistanceToCYSq) {
		    minDistanceToCYSq = distanceSq;
		}
	    }

	    if (minDistanceToOtherBuildingsSq == 0 || distanceSq < minDistanceToOtherBuildingsSq) {
		minDistanceToOtherBuildingsSq = distanceSq;
	    }
	}

	return (minDistanceToCYSq <= (this.BUILDING_CY_RANGE * this.BUILDING_CY_RANGE) && minDistanceToOtherBuildingsSq <= (this.BUILDING_NEAREST_BUILDING_DISTANCE * this.BUILDING_NEAREST_BUILDING_DISTANCE));
    }

    public ArrayList<EntityBuilding> getBuildings() {
	return this.buildings;
    }

    public void deployBuildedVehicle(EntityVehicle v) {
	// Find primary war factory
	for (EntityBuilding b : this.buildings) {
	    if (b instanceof EntityWarFactory) {
		if (b.isPrimary()) {
		    ((EntityWarFactory)b).deployEntity(EntityVehicle.newInstance(v));
		}
	    }
	}
    }

    public void setPrimaryWarFactory(EntityWarFactory entityWarFactory) {
	for (EntityBuilding eb : this.buildings) {
	    if (eb instanceof EntityWarFactory) {
		eb.setPrimary(eb == entityWarFactory);
	    }
	}
    }

    public boolean isMoreThanOneWarFactory() {
	int count = 0;

	for (EntityBuilding eb : this.buildings) {
	    if (eb instanceof EntityWarFactory) {
		count++;
		if (count > 1) {
		    return true;
		}
	    }
	}	

	return false;
    }

    public void setCurrentVehicleHold(boolean hold) {
	this.isCurrentVehicleHold = hold;
    }

    public void cancelCurrentVehicle(boolean moneyBack) {
	// TODO: finish moneyback
	this.currentVehicleBuilding = null;
	this.isCurrentVehicleHold = false;
	this.isCurrentVehicleReady = false;
	this.currentVehicleProgress = 0;
	this.currentVehicleProgressTicks = 0;
    }

    public void giveOre(int aCapacity) {
	// TODO: silos and overflow
	//int overflow = 0;
	//if (this.oreValue + aCapacity > this.oreCapacity) {
	//    overflow = this.oreValue + aCapacity - this.oreCapacity;
	//}
	
	this.oreValue += aCapacity;// - overflow;
    }
}
