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

public class WeaponGrenade extends Weapon {

    private final static int ROF = 60;
    private final static float WEAPON_RANGE = 4f;
    
    private final static int TEXTURE_WIDTH = 8;
    private final static int TEXTURE_HEIGHT = 8;
    
    private final static String BULLET_TEXTURE = "bomb.shp";
    
    private final static String MUZZLE_FLASH = null;
    private final static int MUZZLE_FLASH_FACINGS = 0;
    private final static int MUZZLE_FLASH_LENGTH = 0;
    
    private final static int BURST = 1;
    private final static int BURST_DELAY = 3;
    
    private final static int DAMAGE = 60;
    private final static float SPREAD = 1.5f;
    private static final float PROJECTILE_SPEED = 10.0f;
    private static final int BULLET_ANGLE = 45;
    private static final int NUM_FACINGS = 8;
    private static final float INACCURACY = 0.5f;
    
    public WeaponGrenade() {
	super(ROF, WEAPON_RANGE);
	
	this.minRange = 0;
	Warhead wh = new Warhead();
	
	wh.damage = DAMAGE;
	wh.spread = SPREAD;
	wh.explosion = "veh-hit2.shp";
	wh.impactSound = "kaboom25";
	
	wh.waterExplosion = "h2o_exp3.shp";
	wh.waterImpactSound = "splash9";
	
	this.firingSound = "grenade1";
	
	wh.infDeath = 3;
	wh.leavesSmudge = true;
	wh.isCraterSmudge = true;
	
	wh.canAttackOre = true;
	
	wh.effectiveness.put(ArmorType.NONE, 50);
	wh.effectiveness.put(ArmorType.WOOD, 100);
	wh.effectiveness.put(ArmorType.LIGHT, 25);
	wh.effectiveness.put(ArmorType.HEAVY, 5);

	this.warheads.add(wh);
	
	this.validTargets.add(TargetType.GROUND);
	this.validTargets.add(TargetType.WATER);
	
	this.burst = BURST;
	this.burstDelay = BURST_DELAY;
	
    }
    
    protected WeaponGrenade(int rof, float weaponRange) {
	super(rof, weaponRange);
    }

    @Override
    public Projectile createProjectile(int fcng, Pos muzzlePosition,
	    EntityActor srcActor, Pos centerPosition, Target tgt) {
	// Spawn bullet projectile
	Bullet blt = new Bullet(srcActor, muzzlePosition, centerPosition, (EntityActor) tgt.getTargetEntity(), TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, 0, BULLET_ANGLE, BULLET_TEXTURE, this, PROJECTILE_SPEED, NUM_FACINGS);
	
	blt.currentFacing = fcng;
	blt.inaccuracy = INACCURACY;
	
	return blt;
    }

}
