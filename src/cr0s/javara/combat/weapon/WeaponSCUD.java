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

public class WeaponSCUD extends Weapon {

    private final static int ROF = 240;
    private final static float WEAPON_RANGE = 10f;
    
    private final static int TEXTURE_WIDTH = 41;
    private final static int TEXTURE_HEIGHT = 41;
    
    private final static String BULLET_TEXTURE = "v2.shp";
    
    private final static String MUZZLE_FLASH = "";
    private final static int MUZZLE_FLASH_FACINGS = 0;
    private final static int MUZZLE_FLASH_LENGTH = 0;
    
    private final static int BURST = 1;
    private final static int BURST_DELAY = 3;
    
    private final static int DAMAGE = 450;
    private final static float SPREAD = 0.333f;
    private static final float PROJECTILE_SPEED = 10.0f;
    private static final int BULLET_ANGLE = 45;
    private static final int NUM_FACINGS = 32;
    
    public WeaponSCUD() {
	super(ROF, WEAPON_RANGE);
	
	this.minRange = 3;
	Warhead wh = new Warhead();
	
	wh.damage = DAMAGE;
	wh.spread = SPREAD;
	wh.explosion = "veh-hit1.shp";
	wh.impactSound = "kaboom25";
	
	wh.waterExplosion = "h2o_exp1.shp";
	wh.waterImpactSound = "splash9";
	
	this.firingSound = "missile1";
	
	wh.infDeath = "3";
	wh.leavesSmudge = true;
	wh.isCraterSmudge = true;
	
	wh.canAttackOre = true;
	
	wh.effectiveness.put(ArmorType.CONCRETE, 50);
	wh.effectiveness.put(ArmorType.NONE, 20);
	wh.effectiveness.put(ArmorType.LIGHT, 75);
	
	this.warheads.add(wh);
	
	this.validTargets.add(TargetType.GROUND);
	this.validTargets.add(TargetType.WATER);
	
	this.burst = BURST;
	this.burstDelay = BURST_DELAY;
	
	
    }
    
    protected WeaponSCUD(int rof, float weaponRange) {
	super(rof, weaponRange);
    }

    @Override
    public Projectile createProjectile(int fcng, Pos muzzlePosition,
	    EntityActor srcActor, Pos centerPosition, Target tgt) {
	// Spawn bullet projectile
	Bullet blt = new Bullet(srcActor, muzzlePosition, centerPosition, (EntityActor) tgt.getTargetEntity(), TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, 0, BULLET_ANGLE, BULLET_TEXTURE, this, PROJECTILE_SPEED, NUM_FACINGS);
	blt.trail = "smokey.shp";
	blt.trailInterval = 5;
	blt.trailDelay = 1;
	
	blt.currentFacing = srcActor.currentFacing;
	
	// Spawn muzzle flash
	if (this.MUZZLE_FLASH != null && !this.MUZZLE_FLASH.isEmpty()) {
	    MuzzleFlash mz = new MuzzleFlash(muzzlePosition, this.MUZZLE_FLASH, fcng, MUZZLE_FLASH_FACINGS, MUZZLE_FLASH_LENGTH);
	    mz.isVisible = true;
	    mz.setWorld(srcActor.world);
	    
	    srcActor.world.spawnEntityInWorld(mz);
	}
	
	return blt;
    }

}
