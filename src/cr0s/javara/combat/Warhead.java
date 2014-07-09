package cr0s.javara.combat;

import java.util.TreeMap;

import cr0s.javara.entity.actor.EntityActor;

public class Warhead {

    public TreeMap<ArmorType, Integer> effectiveness = new TreeMap<ArmorType, Integer>();

    public float spread = 0.5f;
    public boolean canAttackOre = false;
    public String explosion;
    public String waterExplosion;

    public int explosionSize[] = { 0, 0 }; // inner and outer ranges

    public int infDeath;
    public DamageModel model = DamageModel.NORMAL;

    public String impactSound = "";
    public String waterImpactSound = "";
    
    public int damage = 0;
    public int delay = 0;    

    public boolean preventProne = false;
    
    public boolean leavesSmudge = true;
    public boolean isCraterSmudge = false;
    
    public Warhead() {

    }

    public int getEffectivenessFor(EntityActor e) {
	if (!this.effectiveness.containsKey(e.armorType)) {
	    return 100;
	}
	
	return this.effectiveness.get(e.armorType);
    }

}
