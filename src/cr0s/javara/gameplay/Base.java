package cr0s.javara.gameplay;

import java.util.ArrayList;

import cr0s.javara.entity.building.EntityBarracks;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.building.EntityPowerPlant;
import cr0s.javara.entity.building.EntityProc;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.building.IPowerProducer;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.render.map.TileSet;
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
    public boolean isWarFactoryPresent = false;
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
    
    private boolean isProcPresent = false;

    public boolean isAnySuperPowerPresent;    
    
    private boolean isLowPower = false;
    
    private int powerLevel = 0;
    private int powerConsumptionLevel = 0;
    
    private int currentOreValue = 0;
    private int maxOreValue = 0;
    
    public Base(Team team, Player owner) {
	
    }
    
    public void update() {
	updateBuildings();
    }
    
    private void updateBuildings() {
	isSovietCYPresent = false;
	isAlliedCYPresent = false;
	isBarracksPresent = false;
	isTentPresent = false;
	isWarFactoryPresent = false;
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
		this.powerConsumptionLevel += ((IPowerConsumer)b).getConsumptionLevel();
	    } else if (b instanceof IPowerProducer) {
		this.powerLevel += ((IPowerProducer)b).getPowerProductionLevel();
	    }	    
	    
	    if (b instanceof EntityConstructionYard) {
		if (((EntityConstructionYard)b).getAlignment() == Alignment.ALLIED) {
		    this.isAlliedCYPresent = true;
		} else if (((EntityConstructionYard)b).getAlignment() == Alignment.SOVIET) {
		    this.isSovietCYPresent = true;
		}
	    } else if (b instanceof EntityBarracks) {
		this.isBarracksPresent = true;
	    } else if (b instanceof EntityProc) {
		this.isProcPresent = true;
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
	    } else {
		if (minDistanceToOtherBuildingsSq == 0 || distanceSq < minDistanceToOtherBuildingsSq) {
		    minDistanceToOtherBuildingsSq = distanceSq;
		}		
	    }
	}
	
	if (minDistanceToOtherBuildingsSq == 0) {
	    minDistanceToOtherBuildingsSq = minDistanceToCYSq;
	}
	
	return (minDistanceToCYSq <= (this.BUILDING_CY_RANGE * this.BUILDING_CY_RANGE) && minDistanceToOtherBuildingsSq <= (this.BUILDING_NEAREST_BUILDING_DISTANCE * this.BUILDING_NEAREST_BUILDING_DISTANCE));
    }
    
    public ArrayList<EntityBuilding> getBuildings() {
	return this.buildings;
    }
}
