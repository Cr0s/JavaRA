package cr0s.javara.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityBarracks;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.IOreCapacitor;
import cr0s.javara.entity.building.IPowerConsumer;
import cr0s.javara.entity.building.IPowerProducer;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.building.common.EntityPowerPlant;
import cr0s.javara.entity.building.common.EntityProc;
import cr0s.javara.entity.building.common.EntityRadarDome;
import cr0s.javara.entity.building.common.EntityWarFactory;
import cr0s.javara.entity.infantry.EntityInfantry;
import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.render.map.TileSet;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.ui.sbpages.SideBarItemsButton;
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
    
    public int oreCapacity, ore, displayOre;
    
    private ProductionQueue queue;
    
    private int cash;
    private int displayCash;
    
    private final int TICKS_WAIT_CASH = 2;
    private int ticksWaitCash = 0;

    private final int TICKS_WAIT_ORE = 2;
    private int ticksWaitOre = 0;    
    
    private Player owner;
    
    private float DISPLAY_FRAC_CASH_PER_TICK = 0.07f;
    private int DISPLAY_CASH_DELTA_PER_TICK = 37;
    
    private HashSet<Class> buildingClasses = new HashSet<>();
    
    public Base(Team team, Player aOwner) {
	this.owner = aOwner;
	
	this.queue = new ProductionQueue(this.owner);
    }

    public void update() {
	updateBuildings();
	this.queue.update();
	
	this.updateDisplayedCash();
    }
    
    public void updateDisplayedCash() {
	if (this.ticksWaitCash > 0) {
	    this.ticksWaitCash--;
	}
	
	if (this.ticksWaitOre > 0) {
	    this.ticksWaitOre--;
	}	
	
	// For cash
	int diff = Math.abs(this.cash - this.displayCash);
	int move = Math.min(Math.max((int)(diff * DISPLAY_FRAC_CASH_PER_TICK), DISPLAY_CASH_DELTA_PER_TICK), diff);


	if (this.displayCash < this.cash)
	{
	    this.displayCash += move;
	    SoundManager.getInstance().playSfxGlobal("cashup1", 0.8f);
	}
	else if (this.displayCash > this.cash)
	{
	    this.displayCash -= move;
	    if (this.ticksWaitCash == 0) { 
		SoundManager.getInstance().playSfxGlobal("cashdn1", 0.8f);
		this.ticksWaitCash = TICKS_WAIT_CASH;
		
		this.displayCash = this.cash;
	    }
	}
	
	// For ore
	diff = Math.abs(this.ore - this.displayOre);
	move = Math.min(Math.max((int) (diff * DISPLAY_FRAC_CASH_PER_TICK), DISPLAY_CASH_DELTA_PER_TICK), diff);

	if (this.displayOre < this.ore)
	{
	    this.displayOre += move;
	    SoundManager.getInstance().playSfxGlobal("cashup1", 0.8f);
	}
	else if (this.displayOre > this.ore)
	{
	    this.displayOre -= move;
	    
	    if (this.ticksWaitOre == 0) { 
		SoundManager.getInstance().playSfxGlobal("cashdn1", 0.8f);
		this.ticksWaitOre = TICKS_WAIT_CASH;
		
		this.displayOre = this.ore;
	    }
	}	
    }

    private void updateBuildings() {
	isSovietCYPresent = isAlliedCYPresent = false;
	isRadarDomePresent = false;
	isProcPresent = false;

	this.oreCapacity = 0;
	this.powerConsumptionLevel = this.powerLevel = 0;
	
	this.buildingClasses.clear();
	
	for (EntityBuilding b : this.buildings) {
	    if (!this.buildingClasses.contains(b.getClass())) {
		this.buildingClasses.add(b.getClass());
	    }
	    
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
	    } else if (b instanceof EntityRadarDome) {
		this.isRadarDomePresent = true;
	    }

	    if (b instanceof IOreCapacitor) {
		this.oreCapacity += ((IOreCapacitor) b).getOreCapacityValue();
	    }
	}

	this.isLowPower = (this.powerConsumptionLevel > this.powerLevel);
    }

    public boolean isLowPower() {
	return this.isLowPower;
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
	} else if (building instanceof EntityBarracks /*|| building instanceof EntityTent*/) {
	    building.setPrimary(!isMoreThanOneTentOrBarrack());
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

	if (!this.queue.canBuild(targetBuilding)) {
	    return false;
	}

	if (checkBuildingDistance(cellX, cellY)) {
	    EntityBuilding b = (EntityBuilding) targetBuilding.newInstance();
	    b.changeCellPos(cellX, cellY);
	    
	    Main.getInstance().getWorld().addBuildingTo(b);

	    queue.getProductionForBuilding(targetBuilding).deployCurrentActor();
	    
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
	getPrimaryWarFactory().deployEntity(EntityVehicle.newInstance(v));
    }
    
    public EntityBuilding getPrimaryBarrackOrTent() {
	for (EntityBuilding b : this.buildings) {
	    if (b instanceof EntityBarracks /*|| b instanceof EntityTent*/) {
		if (b.isPrimary()) {
		    return b;
		}
	    }
	}	
	
	return null;
    }

    public void setPrimaryTentOrBarrack(EntityBuilding b) {
	for (EntityBuilding eb : this.buildings) {
	    if (eb instanceof EntityBarracks/* || eb instanceof EntityTent*/) {
		eb.setPrimary(eb == b);
	    }
	}
    }

    public boolean isMoreThanOneTentOrBarrack() {
	int count = 0;

	for (EntityBuilding eb : this.buildings) {
	    if (eb instanceof EntityBarracks/* || eb instanceof EntityTent*/) {
		count++;
		if (count > 1) {
		    return true;
		}
	    }
	}	

	return false;
    }

    public void deployTrainedInfantry(EntityInfantry i) {
	EntityBuilding b = getPrimaryBarrackOrTent();
	if (b != null) {
	    if (b instanceof EntityBarracks) {
		((EntityBarracks) b).deployEntity(i.newInstance());
	    }/* else if (b instanceof EntityTent) {
		((EntityTent) b).deployEntity(i.newInstance());
	    }*/
	}
    }
    
    public EntityWarFactory getPrimaryWarFactory() {
	for (EntityBuilding b : this.buildings) {
	    if (b instanceof EntityWarFactory) {
		if (b.isPrimary()) {
		    return (EntityWarFactory) b;
		}
	    }
	}	
	
	return null;
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
    
    
    public void giveOre(int aCapacity) {
	if (this.ore + aCapacity > 0.8f * this.oreCapacity) {
	    if (this.owner == Main.getInstance().getPlayer()) {
		// "Silos needed"
		SoundManager.getInstance().playSpeechSoundGlobal("silond1");
	    }
	}
	
	if (this.ore + aCapacity > this.oreCapacity) {
	    return; // don't accept exceeding ore
	}
	
	int overflow = 0;
	if (this.ore + aCapacity > this.oreCapacity) {
	    overflow = this.ore + aCapacity - this.oreCapacity;
	}
	
	this.ore += aCapacity - overflow;
    }
    
    public void takeOre(int value) {
	this.ore -= value;
	
	if (this.ore < 0) {
	    this.ore = 0;
	}
    }
    
    public void gainCash(int amount) {
	this.cash += amount;
    }
    
    public boolean takeCash(int amount) {
	if (this.cash + this.ore < amount) {
	    return false;
	}
	
	// Spent ore first
	this.ore -= amount;
	if (this.ore < 0) { // we spent all ore
	    this.cash += this.ore; // spent cash
	    this.ore = 0;
	}
	
	return true;
    }
    
    public ProductionQueue getProductionQueue() {
	return this.queue;
    }

    public void productButtonItem(SideBarItemsButton texture) {
	EntityActor target = queue.getBuildableActor(texture);
	queue.startBuildingActor(target, texture);
    }

    public int getCash() {
	return this.cash;
    }
    
    public int getDisplayCash() {
	return this.displayCash;
    }
    
    public int getDisplayOre() {
	return this.displayOre;
    }    

    public HashSet<Class> getBuildingClasses() {
	return this.buildingClasses;
    }
}
