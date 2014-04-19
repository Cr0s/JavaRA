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
/**
 * Describes player's base.
 * @author Cr0s
 */
public class Base {
    private ArrayList<EntityBuilding> buildings = new ArrayList<>();
    
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
}
