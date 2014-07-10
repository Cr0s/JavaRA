package cr0s.javara.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.newdawn.slick.Color;
import org.yaml.snakeyaml.Yaml;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.ProductionQueue;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.render.World;
import cr0s.javara.resources.ResourceManager;

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
    private HashMap<String, Float> unitsToBuild;
    private HashMap<String, Float> buildingFractions;
    private HashMap<String, String[]> unitCommonNames;
    private HashMap<String, String[]> buildingCommonNames;
    private HashMap<String, Integer> buildingLimits;
    
    private Random rnd;
    
    public AIPlayer(World w, String name, Alignment side, Color color) {
	super(w, name, side, color);
	
	this.enabled = true;
	
	this.rnd = new Random();
	
	loadAIRules(name);
    }
  
    private void loadAIRules(String name) {
	this.buildingCommonNames = new HashMap<String, String[]>();
	this.unitCommonNames = new HashMap<String, String[]>();
	this.buildingLimits = new HashMap<String, Integer>();
	this.buildingFractions = new HashMap<String, Float>();
	this.unitsToBuild = new HashMap<String, Float>();
	
	InputStream input = null;
	try {
	    input = new FileInputStream(new File((ResourceManager.AI_FOLDER + name + ".yaml").toString().toLowerCase()));

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
    public void update(int delta) {
	super.update(delta);
    }
    
    public EntityActor chooseRandomUnitToBuild(ProductionQueue queue) {
	int index = this.rnd.nextInt(queue.getBuildables().values().size());

	return (EntityActor) queue.getBuildables().values().toArray()[index];
    }
    
    
}
