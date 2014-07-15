package cr0s.javara.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.newdawn.slick.Color;
import org.yaml.snakeyaml.Yaml;

import cr0s.javara.combat.Warhead;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityBuildingProgress;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.ProductionQueue;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.main.Main;
import cr0s.javara.order.Order;
import cr0s.javara.perfomance.Profiler;
import cr0s.javara.render.World;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.util.CellChooser;
import cr0s.javara.util.Pos;

public class AIPlayer extends Player {
    private int squadSize = 8;

    private class Enemy {
	public float aggro;

	public Enemy(float aggro) {
	    this.aggro = aggro;
	}
    }

    public enum BuildingType { BUILDING, DEFENSE, REFINERY };

    private boolean enabled;
    private int ticks;

    private ArrayList<Squad> squads = new ArrayList<Squad>();
    private ArrayList<EntityActor> unitsHangingAroundTheBase = new ArrayList<EntityActor>();
    private ArrayList<EntityActor> activeUnits = new ArrayList<EntityActor>();

    private int assignRolesInterval = 20;
    private int rushInterval = 600;
    private int attackForceInterval = 30;

    public int structureProductionInactiveDelay = 125;
    public int structureProductionActiveDelay = 10;

    public int minimumDefenseRadius = 5;
    public int maximumDefenseRadius = 20;

    public int newProductionCashThreshold = 5000;

    public int idleBaseUnitsMaximum = 12;

    public int rushAttackScanRadius = 15;
    public int protectUnitScanRadius = 15;

    public int maxBaseRadius = 20;

    public boolean shoudlRepairBuildings = true;

    // Tables
    HashMap<String, Float> unitsToBuild;
    HashMap<String, Float> buildingFractions;
    HashMap<String, String[]> unitCommonNames;
    HashMap<String, String[]> buildingCommonNames;
    HashMap<String, Integer> buildingLimits;

    Random rnd;

    private Pos defenseCenter;

    private BaseBuilder bb;
    
    public AIPlayer(World w, String name, Alignment side, Color color) {
	super(w, name, side, color);

	this.rnd = new Random();

	loadAIRules(name);
	
	this.bb = new BaseBuilder(this);
    }

    private void loadAIRules(String name) {
	this.buildingCommonNames = new HashMap<String, String[]>();
	this.unitCommonNames = new HashMap<String, String[]>();
	this.buildingLimits = new HashMap<String, Integer>();
	this.buildingFractions = new HashMap<String, Float>();
	this.unitsToBuild = new HashMap<String, Float>();

	InputStream input = null;
	try {
	    input = new FileInputStream(new File(ResourceManager.AI_FOLDER + name + ".yaml"));

	    Yaml yaml = new Yaml();
	    Map<String, Object> map = (Map) yaml.load(input);	   

	    // Load buildings common names
	    Map<String, Object> buildingsCommonNamesMap = (Map) map.get("BuildingCommonNames");
	    for (Entry e : buildingsCommonNamesMap.entrySet()) {
		String[] names = ((String) e.getValue()).split(",");

		this.buildingCommonNames.put((String) e.getKey(), names);
	    }

	    // Load units common names
	    Map<String, Object> unitsCommonNamesMap = (Map) map.get("UnitsCommonNames");
	    for (Entry e : unitsCommonNamesMap.entrySet()) {
		String[] names = ((String) e.getValue()).split(",");

		this.buildingCommonNames.put((String) e.getKey(), names);
	    }	 

	    // Load building limits
	    Map<String, Object> buildingLimitsMap = (Map) map.get("BuildingLimits");
	    for (Entry e : buildingLimitsMap.entrySet()) {
		Integer limit = (Integer) e.getValue();

		this.buildingLimits.put((String) e.getKey(), limit);
	    }		    

	    // Load building fractions
	    Map<String, Object> buildingFractionsMap = (Map) map.get("BuildingFractions");
	    for (Entry e : buildingFractionsMap.entrySet()) {
		String percentageStr = (String) e.getValue();
		Float percentageValue = Float.valueOf(percentageStr.replace("%", ""));

		this.buildingFractions.put((String) e.getKey(), percentageValue / 100.0f);
	    }	

	    // Load units building fractions
	    Map<String, Object> unitsToBuildMap = (Map) map.get("UnitsToBuild");
	    for (Entry e : unitsToBuildMap .entrySet()) {
		String percentageStr = (String) e.getValue();
		Float percentageValue = Float.valueOf(percentageStr.replace("%", ""));

		this.unitsToBuild.put((String) e.getKey(), percentageValue / 100.0f);
	    }		    

	    // Load squad size
	    this.squadSize = (Integer) map.get("SquadSize");
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (input == null) {
		return;
	    }

	    try {
		input.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    @Override
    public void spawn() {
	super.spawn();
	
	this.defenseCenter = this.getPlayerSpawnPoint();
	this.enabled = true;
    }
    
    @Override
    public void update(int delta) {
	super.update(delta);

	Profiler.getInstance().startForSection("AI");
	if (!this.enabled) {
	    return;
	}

	this.ticks++;

	if (this.ticks == 1) {
	    this.findAndDeployAllMcv();
	}
	
	this.bb.update();
	Profiler.getInstance().stopForSection("AI");
    }

    public void findAndDeployAllMcv() {
	for (Entity e : Main.getInstance().getWorld().getEntitiesList()) {
	    if (e.isDead() || !(e instanceof EntityMcv)) {
		continue;
	    }
	    
	    EntityMcv mcv = (EntityMcv) e;

	    // It's our, deploy
	    if (mcv.owner == this) {
		if (!mcv.canDeploy()) { // cant deploy in current location, choose another
		    Pos desiredLocation = this.chooseBuildLocation("fact", false, BuildingType.BUILDING);
		    mcv.resolveOrder(new Order("Move", mcv, desiredLocation, true));
		}

		mcv.resolveOrder(new Order("Deploy", mcv, null, true));
	    }
	}	
    }

    public EntityActor chooseRandomUnitToBuild(ProductionQueue queue) {
	int index = this.rnd.nextInt(queue.getBuildables().values().size());

	return (EntityActor) queue.getBuildables().values().toArray()[index];
    }

    public int countBuildings(String frac, Player owner) {
	int result = 0;

	for (EntityBuilding b : owner.getBase().getBuildings()) {
	    if (b.owner == owner 
		    && b.getName().equals(frac)) {
		result++;
	    }
	}
	
	// Also check current building progress
	for (EntityBuildingProgress b : owner.getBase().getCurrentlyBuilding()) {
	    if (b.getTargetBuilding().getName().equals(frac)) {
		result++;
	    }
	}

	return result;
    }

    private int countUnits(String unit, Player owner) {
	int result = 0;

	for (Entity e : Main.getInstance().getWorld().getEntitiesList()) {
	    if (!(e instanceof EntityActor)) {
		continue;
	    }

	    EntityActor a = (EntityActor) e;

	    if (a.owner == owner && a.getName().equals(unit)) {
		result++;
	    }
	}

	return result;
    }    

    private Integer countBuildingByCommonName(String commonName, Player owner) {
	if (!this.buildingCommonNames.containsKey(commonName)) {
	    return null;
	}

	int result = 0;

	for (EntityBuilding b : owner.getBase().getBuildings()) {
	    if (b.owner == owner) {
		for (String name : this.buildingCommonNames.get(commonName)) {
		    if (name.equals(b.getName())) {
			result++;
		    }
		}
	    }
	}

	return result;
    }

    private boolean hasAdequateFact() {
	return (this.getBase().isAlliedCYPresent || this.getBase().isSovietCYPresent) 
		|| (!this.getBase().isAlliedWarFactoryPresent && !this.getBase().isSovietWarFactoryPresent);
    }

    public boolean hasAdequateProc() {
	return this.countBuildingByCommonName("Refinery", this) > 0 ||
		this.countBuildingByCommonName("Power", this) == 0;
    }

    public boolean hasMinimumProc() {
	return this.countBuildingByCommonName("Refinery", this) >= 2 ||
		this.countBuildingByCommonName("Power", this) == 0 ||
		this.countBuildingByCommonName("Barracks", this) == 0;
    }

    public boolean hasAdequateAirUnits(EntityActor a) {
	// TODO: finish it after air units being added
	return true;
    }

    private Pos findPosForBuilding(String actorType, final Pos center, final Pos target, final int minRange, final int maxRange, final boolean distanceToBaseIsImportant) {
	ArrayList<Pos> cells = Main.getInstance().getWorld().chooseTilesInAnnulus(center, minRange, maxRange);

	if (!center.equals(target)) {
	    Collections.sort(cells, new Comparator<Pos>() {

		@Override
		public int compare(Pos p1, Pos p2) {
		    return p1.distanceToSq(target) - p2.distanceToSq(target);
		}

	    });
	} else {
	    // Shuffling is needed to make sure bot place building in random location at the base
	    Collections.shuffle(cells, this.rnd);
	}

	EntityActor b = this.getBase().getProductionQueue().getBuildableActorByName(actorType);
	if (b == null || !(b instanceof EntityBuilding)) {
	    return null;
	}
	
	
	for (Pos cell : cells) {
	    if (!this.getBase().isPossibleToBuildHere((int) cell.getX(), (int) cell.getY(), (EntityBuilding) b)) {
		continue;
	    }

	    if (distanceToBaseIsImportant && !this.getBase().checkBuildingDistance((int) cell.getX(), (int) cell.getY(), false)) {
		continue;
	    }

	    return cell;
	}

	return null;
    }

    private EntityActor findClosestEnemy(final Pos center) {
	EntityActor closest = null;

	for (Entity e : Main.getInstance().getWorld().getEntitiesList()) { 
	    if (e.isDead() || !(e instanceof EntityActor) || (e.owner != this)) {
		continue;
	    }

	    EntityActor a = (EntityActor) e;

	    if (closest == null 
		    || a.getPosition().getCellPos().distanceToSq(center) < closest.getPosition().getCellPos().distanceToSq(center)) {
		closest = a;
	    }
	}

	return closest;
    }

    public Pos chooseBuildLocation(String actorType, boolean distanceToBaseIsImportant, BuildingType type) {
	switch (type) {
	case DEFENSE:
	    if (this.rnd.nextInt(100) >= 30) { // In ~70% of cases we build defensive structures as close as possible to the enemy
		EntityActor closestEnemy = this.findClosestEnemy(this.defenseCenter);
		Pos targetCell = (closestEnemy != null) ? closestEnemy.getCellPosition() : this.getPlayerSpawnPoint();
		
		System.out.println("[AI] Closest enemy: " + closestEnemy.getClass().getSimpleName());
		return this.findPosForBuilding(actorType, this.defenseCenter, targetCell, this.minimumDefenseRadius, this.maximumDefenseRadius, distanceToBaseIsImportant);
	    } else { // In other cases place build somewhere in base
		return this.findPosForBuilding(actorType, this.getPlayerSpawnPoint(), this.getPlayerSpawnPoint(), 0, this.maxBaseRadius, false);
	    }
	
	case BUILDING:
	    return this.findPosForBuilding(actorType, this.getPlayerSpawnPoint(), this.getPlayerSpawnPoint(), 0, distanceToBaseIsImportant ? this.maxBaseRadius : Main.getInstance().getWorld().MAX_RANGE, distanceToBaseIsImportant);

	case REFINERY:
	    // Choose set of cells with resources nearby the base inside buildable area
	    ArrayList<Pos> resourceTiles = Main.getInstance().getWorld().chooseTilesInCircle(this.getPlayerSpawnPoint(), this.maxBaseRadius, new CellChooser() {

		@Override
		public boolean isCellChoosable(Pos cellPos) {
		    return !Main.getInstance().getWorld().getMap().getResourcesLayer().isCellEmpty(cellPos);
		}

	    });

	    for (Pos c : resourceTiles) {
		Pos found = this.findPosForBuilding(actorType, c, this.getPlayerSpawnPoint(), 0, this.maxBaseRadius, false);

		if (found != null) {
		    return found;
		}
	    }

	    // Try to find a free spot somewhere else in the base
	    return this.findPosForBuilding(actorType, this.getPlayerSpawnPoint(), this.getPlayerSpawnPoint(), 0, this.maxBaseRadius, false);

	default:
	    break;
	}

	return null;
    }

    @Override
    public void notifyDamaged(Entity who, EntityActor by, int amount, Warhead warhead) {
	if (!who.isDead() && who.owner == this && by != null && amount > 0 && warhead != null) {
	    if (who instanceof EntityBuilding) { // Defense our buildings
		if (who instanceof EntityBuildingProgress) {
		    who = ((EntityBuildingProgress) who).getTargetBuilding();
		}
		
		this.defenseCenter = ((EntityBuilding) who).getCellPosition();
		
		// Try to repair building
		this.getBase().repairBuilding((EntityBuilding) who);
	    }
	}
    }
}
