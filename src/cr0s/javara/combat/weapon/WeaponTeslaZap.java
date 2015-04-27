package cr0s.javara.combat.weapon;

import cr0s.javara.combat.ArmorType;
import cr0s.javara.combat.Projectile;
import cr0s.javara.combat.TargetType;
import cr0s.javara.combat.Warhead;
import cr0s.javara.combat.Weapon;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.effect.Bullet;
import cr0s.javara.entity.effect.MuzzleFlash;
import cr0s.javara.entity.effect.TeslaZap;
import cr0s.javara.order.Target;
import cr0s.javara.util.Pos;

public class WeaponTeslaZap extends Weapon {

    private final static int ROF = 1;
    private final static float WEAPON_RANGE = 8f;
    
    private final static int TEXTURE_WIDTH = 24;
    private final static int TEXTURE_HEIGHT = 24;

    private final static String MUZZLE_FLASH = null;
    private final static int MUZZLE_FLASH_FACINGS = 0;
    private final static int MUZZLE_FLASH_LENGTH = 3;
    
    private final static int BURST = 1;
    private final static int BURST_DELAY = 20;
    
    private final static int DAMAGE = 400;
    private final static float SPREAD = 1f;
    private static final float PROJECTILE_SPEED = 2.0f;
    private static final int BULLET_ANGLE = 0;
    
    public WeaponTeslaZap() {
	super(ROF, WEAPON_RANGE);
	
	Warhead wh = new Warhead();
	
	wh.damage = DAMAGE;
	wh.spread = SPREAD;
	wh.explosion = "";
	wh.impactSound = "";
	
	wh.waterExplosion = "";
	wh.waterImpactSound = "";
	
	this.firingSound = "tesla1";
	
	wh.infDeath = 6;
	wh.leavesSmudge = true;
	wh.isCraterSmudge = false;
	
	wh.canAttackOre = false;
	
	wh.effectiveness.put(ArmorType.WOOD, 60);
	
	this.warheads.add(wh);
	
	this.validTargets.add(TargetType.GROUND);
	this.validTargets.add(TargetType.WATER);
    }
    
    protected WeaponTeslaZap(int rof, float weaponRange) {
	super(rof, weaponRange);
    }

    @Override
    public Projectile createProjectile(int fcng, Pos muzzlePosition,
	    EntityActor srcActor, Pos centerPosition, Target tgt) {
	// Spawn bullet projectile
	TeslaZap blt = new TeslaZap(srcActor, muzzlePosition, centerPosition, (EntityActor) tgt.getTargetEntity(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
	blt.weapon = this;
	
	return blt;
    }

}
