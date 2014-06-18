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

public class WeaponM1Carabine extends Weapon {

    private final static int ROF = 20;
    private final static float WEAPON_RANGE = 5f;
    
    private final static int TEXTURE_WIDTH = 24;
    private final static int TEXTURE_HEIGHT = 24;
    
    private final static String BULLET_TEXTURE = "";
    
    private final static String MUZZLE_FLASH = null;
    private final static int MUZZLE_FLASH_FACINGS = 0;
    private final static int MUZZLE_FLASH_LENGTH = 3;
    
    private final static int BURST = 1;
    private final static int BURST_DELAY = 3;
    
    private final static int DAMAGE = 15;
    private final static float SPREAD = 0.125f;
    private static final float PROJECTILE_SPEED = 36f;
    private static final int BULLET_ANGLE = 0;
    
    public WeaponM1Carabine() {
	super(ROF, WEAPON_RANGE);
	
	Warhead wh = new Warhead();
	
	wh.damage = DAMAGE;
	wh.spread = SPREAD;
	wh.explosion = "piffpiff.shp";
	wh.impactSound = "";
	
	wh.waterExplosion = "wpifpif.shp";
	wh.waterImpactSound = "";
	
	this.firingSound = "gun11";
	
	wh.infDeath = "2";
	wh.leavesSmudge = false;
	
	wh.effectiveness.put(ArmorType.WOOD, 25);
	wh.effectiveness.put(ArmorType.CONCRETE, 10);
	wh.effectiveness.put(ArmorType.HEAVY, 10);
	wh.effectiveness.put(ArmorType.LIGHT, 30);
	
	this.warheads.add(wh);
	
	this.validTargets.add(TargetType.GROUND);
	this.validTargets.add(TargetType.WATER);
	
	this.burst = BURST;
	this.burstDelay = BURST_DELAY;
	
	
    }

    @Override
    public Projectile createProjectile(int fcng, Pos muzzlePosition,
	    EntityActor srcActor, Pos centerPosition, Target tgt) {
	// Spawn bullet projectile
	Bullet blt = new Bullet(srcActor, muzzlePosition, centerPosition, (EntityActor) tgt.getTargetEntity(), TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, 0, BULLET_ANGLE, BULLET_TEXTURE, this, PROJECTILE_SPEED);
	
	return blt;
    }

}
