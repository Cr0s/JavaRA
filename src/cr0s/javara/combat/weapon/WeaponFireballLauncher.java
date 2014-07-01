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

public class WeaponFireballLauncher extends Weapon {

    private final static int ROF = 65;
    private final static float WEAPON_RANGE = 5f;
    
    private final static int TEXTURE_WIDTH = 24;
    private final static int TEXTURE_HEIGHT = 24;
    
    private final static String BULLET_TEXTURE = "fb1.shp";
    
    private final static String MUZZLE_FLASH = null;
    private final static int MUZZLE_FLASH_FACINGS = 0;
    private final static int MUZZLE_FLASH_LENGTH = 3;
    
    private final static int BURST = 2;
    private final static int BURST_DELAY = 20;
    
    private final static int DAMAGE = 150;
    private final static float SPREAD = 3f;
    private static final float PROJECTILE_SPEED = 2.0f;
    private static final int BULLET_ANGLE = 0;
    
    public WeaponFireballLauncher() {
	super(ROF, WEAPON_RANGE);
	
	Warhead wh = new Warhead();
	
	wh.damage = DAMAGE;
	wh.spread = SPREAD;
	wh.explosion = "napalm2.shp";
	wh.impactSound = "firebl3";
	
	wh.waterExplosion = "napalm2.shp";
	wh.waterImpactSound = "firebl3";
	
	this.firingSound = "";
	
	wh.infDeath = "5";
	wh.leavesSmudge = true;
	wh.isCraterSmudge = false;
	
	wh.canAttackOre = false;
	
	wh.effectiveness.put(ArmorType.CONCRETE, 50);
	wh.effectiveness.put(ArmorType.NONE, 90);
	wh.effectiveness.put(ArmorType.LIGHT, 60);
	wh.effectiveness.put(ArmorType.WOOD, 50);
	wh.effectiveness.put(ArmorType.HEAVY, 25);
	
	this.warheads.add(wh);
	
	this.validTargets.add(TargetType.GROUND);
	this.validTargets.add(TargetType.WATER);
	
	this.burst = BURST;
	this.burstDelay = BURST_DELAY;
	
	
    }
    
    protected WeaponFireballLauncher(int rof, float weaponRange) {
	super(rof, weaponRange);
    }

    @Override
    public Projectile createProjectile(int fcng, Pos muzzlePosition,
	    EntityActor srcActor, Pos centerPosition, Target tgt) {
	// Spawn bullet projectile
	Bullet blt = new Bullet(srcActor, muzzlePosition, centerPosition, (EntityActor) tgt.getTargetEntity(), TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, 0, BULLET_ANGLE, BULLET_TEXTURE, this, PROJECTILE_SPEED);
	blt.trail = "fb2.shp";
	blt.trailInterval = 3;
	blt.trailDelay = 1;
	
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
