package cr0s.javara.combat.weapon;

import cr0s.javara.combat.ArmorType;
import cr0s.javara.combat.Projectile;
import cr0s.javara.combat.TargetType;
import cr0s.javara.combat.Warhead;
import cr0s.javara.combat.Weapon;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.effect.Bullet;
import cr0s.javara.entity.effect.Missile;
import cr0s.javara.entity.effect.MuzzleFlash;
import cr0s.javara.order.Target;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public class WeaponDragon extends Weapon {

    private final static int ROF = 50;
    private final static int ROT = 5;
    private final static int RANGE_LIMIT = 35; // in ticks
    private final static float WEAPON_RANGE = 5f;
    
    private final static int TEXTURE_WIDTH = 15;
    private final static int TEXTURE_HEIGHT = 15;
    
    private final static String MISSILE_TEXTURE = "dragon.shp";
    
    private final static String MUZZLE_FLASH = "";
    private final static int MUZZLE_FLASH_FACINGS = 0;
    private final static int MUZZLE_FLASH_LENGTH = 0;
    
    private final static int BURST = 1;
    private final static int BURST_DELAY = 3;
    
    private final static int DAMAGE = 50;
    private final static float SPREAD = 2.5f;
    private static final float PROJECTILE_SPEED = 10.0f;
    
    private static final int MISSILE_ANGLE = 0;
    public static final int NUM_FACINGS = 32;
    
    private static final float INACCURACY = 0.2f;
    
    public WeaponDragon() {
	super(ROF, WEAPON_RANGE);
	
	this.firingSound = "missile6";	
	this.validTargets.add(TargetType.WATER);
	this.validTargets.add(TargetType.GROUND);
	
	Warhead wh = new Warhead();
	
	wh.damage = DAMAGE;
	wh.spread = SPREAD;
	wh.explosion = "veh-hit2.shp";
	wh.impactSound = "kaboom25";
	
	wh.waterExplosion = "h2o_exp2.shp";
	wh.waterImpactSound = "splash9";
		
	wh.infDeath = 4;
	wh.leavesSmudge = true;
	wh.isCraterSmudge = true;
	
	wh.canAttackOre = true;
	
	wh.effectiveness.put(ArmorType.NONE, 10);
	wh.effectiveness.put(ArmorType.WOOD, 75);
	wh.effectiveness.put(ArmorType.LIGHT, 35);
	wh.effectiveness.put(ArmorType.CONCRETE, 20);
	
	this.warheads.add(wh);
	
	this.validTargets.add(TargetType.GROUND);
	this.validTargets.add(TargetType.WATER);
	
	this.burst = BURST;
	this.burstDelay = BURST_DELAY;
	
    }
    
    protected WeaponDragon(int rof, float weaponRange) {
	super(rof, weaponRange);
    }

    @Override
    public Projectile createProjectile(int fcng, Pos muzzlePosition, EntityActor srcActor, Pos centerPosition, Target tgt) {
	// Spawn missile projectile
	// EntityActor srcActor, Pos srcPos, Pos passivePos,
	    //EntityActor targetActor, int width, int height, int ang, String img, Weapon weap, float spd, int numFacings, int rot
	Missile msl = new Missile(srcActor, muzzlePosition, tgt.centerPosition(), (EntityActor) tgt.getTargetEntity(), this.TEXTURE_WIDTH, this.TEXTURE_HEIGHT, this.MISSILE_ANGLE,
		this.MISSILE_TEXTURE, this, this.PROJECTILE_SPEED, this.NUM_FACINGS, this.ROT, this.INACCURACY);

	float angle = RotationUtil.facingToAngle(fcng, srcActor.getMaxFacings());
	fcng = RotationUtil.angleToFacing(angle);
	
	msl.currentFacing = fcng;
	msl.trail = "smokey.shp";
	msl.jammable = false;
	msl.rangeLimit = this.RANGE_LIMIT;
	
	// Spawn muzzle flash
	if (this.MUZZLE_FLASH != null && !this.MUZZLE_FLASH.isEmpty()) {
	    MuzzleFlash mz = new MuzzleFlash(muzzlePosition, this.MUZZLE_FLASH, fcng, MUZZLE_FLASH_FACINGS, MUZZLE_FLASH_LENGTH);
	    mz.isVisible = true;
	    mz.setWorld(srcActor.world);
	    
	    srcActor.world.spawnEntityInWorld(mz);
	}
	
	return msl;
    }

}
