package cr0s.javara.combat.weapon;

import cr0s.javara.combat.ArmorType;
import cr0s.javara.combat.Projectile;
import cr0s.javara.combat.TargetType;
import cr0s.javara.combat.Warhead;
import cr0s.javara.combat.Weapon;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.effect.Bullet;
import cr0s.javara.entity.effect.MuzzleFlash;
import cr0s.javara.order.Target;
import cr0s.javara.util.Pos;

public class Weapon120mm extends Weapon {

    private final static int ROF = 90;
    private final static float WEAPON_RANGE = 4.8f;
    
    private final static int TEXTURE_WIDTH = 24;
    private final static int TEXTURE_HEIGHT = 24;
    
    private final static String BULLET_TEXTURE = "120mm.shp";
    
    private final static String MUZZLE_FLASH = "flak.shp"; // TODO: fix white color in "gunfire.shp";
    private final static int MUZZLE_FLASH_FACINGS = 0;
    private final static int MUZZLE_FLASH_LENGTH = 3;
    
    private final static int BURST = 2;
    private final static int BURST_DELAY = 3;
    
    private final static int DAMAGE = 60;
    private final static float SPREAD = 2f;
    private static final float PROJECTILE_SPEED = 15.0f;
    private static final int BULLET_ANGLE = 0;
    
    public Weapon120mm() {
	super(ROF, WEAPON_RANGE);
	
	Warhead wh = new Warhead();
	
	wh.damage = DAMAGE;
	wh.spread = SPREAD;
	wh.explosion = "flak.shp";
	wh.impactSound = "kaboom12";
	
	wh.waterExplosion = "h2o_exp3.shp";
	wh.waterImpactSound = "splash9";
	
	this.firingSound = "cannon1";
	
	wh.infDeath = "4";
	wh.leavesSmudge = true;
	wh.isCraterSmudge = true;
	
	wh.canAttackOre = true;
	
	wh.effectiveness.put(ArmorType.CONCRETE, 50);
	wh.effectiveness.put(ArmorType.NONE, 20);
	wh.effectiveness.put(ArmorType.LIGHT, 75);
	wh.effectiveness.put(ArmorType.WOOD, 75);
	wh.effectiveness.put(ArmorType.HEAVY, 75);
	
	this.warheads.add(wh);
	
	this.validTargets.add(TargetType.GROUND);
	this.validTargets.add(TargetType.WATER);
	
	this.burst = BURST;
	this.burstDelay = BURST_DELAY;
	
	
    }
    
    protected Weapon120mm(int rof, float weaponRange) {
	super(rof, weaponRange);
    }

    @Override
    public Projectile createProjectile(int fcng, Pos muzzlePosition,
	    EntityActor srcActor, Pos centerPosition, Target tgt) {
	// Spawn bullet projectile
	Bullet blt = new Bullet(srcActor, muzzlePosition, centerPosition, (EntityActor) tgt.getTargetEntity(), TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, 0, BULLET_ANGLE, BULLET_TEXTURE, this, PROJECTILE_SPEED);
	//blt.trail = "smokey.shp";
	//blt.trailInterval = 5;
	//blt.trailDelay = 3;
	
	// Spawn muzzle flash
	if (this.MUZZLE_FLASH != null) {
	    MuzzleFlash mz = new MuzzleFlash(muzzlePosition, this.MUZZLE_FLASH, fcng, MUZZLE_FLASH_FACINGS, MUZZLE_FLASH_LENGTH);
	    mz.isVisible = true;
	    mz.setWorld(srcActor.world);
	    
	    srcActor.world.spawnEntityInWorld(mz);
	}
	
	return blt;
    }

}
