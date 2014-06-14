package cr0s.javara.combat;

import java.util.ArrayList;
import java.util.LinkedList;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.render.World;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.util.Pos;

public class Combat {
    public static String getImpactSound(Warhead warhead, boolean isWater) {
	if (isWater && warhead.waterImpactSound != null)
	    return warhead.waterImpactSound;

	if (warhead.impactSound != null)
	    return warhead.impactSound;

	return null;	
    }

    public static void doImpact(Pos pos, Warhead warhead, Weapon weapon, EntityActor firedBy, float firepowerModifier) {
	World world = firedBy.world;
	Pos targetTile = new Pos(pos.getCellX(), pos.getCellY());

	if (!world.getMap().isInMap(pos)) {
	    return;
	}

	boolean isInWater = pos.getZ() == 0 && world.getMap().getSurfaceIdAt(targetTile) == world.getMap().getTileSet().SURFACE_WATER_ID;
	String explosionType = isInWater ? warhead.waterExplosion : warhead.explosion;

	if (explosionType != null && !explosionType.isEmpty()) {
	    world.spawnExplosionAt(pos, explosionType);
	}

	SoundManager.getInstance().playSfxAt(getImpactSound(warhead, isInWater), pos);

	// Warhead can attack in radius
	if (warhead.explosionSize[0] > 0) {
	    ArrayList<Pos> tiles = world.chooseTilesInCircle(targetTile, warhead.explosionSize[0]);
	    for (Pos tile : tiles) {
		if (!isInWater && warhead.leavesSmudge) {
		    world.getMap().smudges.addSmudge(tile, warhead.isCraterSmudge);
		}

		if (warhead.canAttackOre) {
		    world.getMap().getResourcesLayer().destroy(tile);
		}
	    }
	} else { // Warhead attacks single cell	    
	    if (warhead.leavesSmudge) {
		world.getMap().smudges.addSmudge(targetTile, warhead.isCraterSmudge);
	    }
	}

	if (warhead.canAttackOre) {
	    world.getMap().getResourcesLayer().destroy(targetTile);
	}

	switch (warhead.model) {
	case NORMAL:
	    float maxSpread = warhead.spread * (float) (Math.log(Math.abs(warhead.damage)) / Math.log(2));
	    ArrayList<EntityActor> hitActors = world.getActorsInCircle(pos, maxSpread);

	    for (EntityActor victim : hitActors) {
		int damage = (int) getDamageToInflict(pos, victim, warhead, weapon, firepowerModifier, true);
		victim.giveDamage(firedBy, damage, warhead);
	    }
	    break;
	case PER_CELL:
	    ArrayList<Pos> tiles = world.chooseTilesInCircle(targetTile, warhead.explosionSize[0]);
	    for (Pos tile : tiles) {
		ArrayList<EntityActor> cellActors = world.getActorsInCircle(pos, warhead.explosionSize[0]);

		for (EntityActor victim : cellActors) {
		    int damage = (int) getDamageToInflict(pos, victim, warhead, weapon, firepowerModifier, true);
		    victim.giveDamage(firedBy, damage, warhead);
		}		
	    }
	    break;
	case HP_PERCENTAGE:
	    float range = warhead.explosionSize[0];// * 24; // XXX: convert to world coordinates
	    ArrayList<EntityActor> actorsToHit = world.getActorsInCircle(pos, range);

	    for (EntityActor victim : actorsToHit) {
		int damage = (int) getDamageToInflict(pos, victim, warhead, weapon, firepowerModifier, true);
		if (victim.getMaxHp() != 0) {
		    victim.giveDamage(firedBy, damage / 100 * victim.getMaxHp(), warhead);
		}
	    }	    
	    break;
	default:
	    break;

	}
    }

    public static void doImpacts(Pos pos,  Weapon weapon, EntityActor firedBy, float firepowerModifier) {
	for (Warhead wh : weapon.warheads) {
	    doImpact(pos, wh, weapon, firedBy, firepowerModifier);
	}
    }

    private static float[] falloff =
	{
	1f, 0.3678795f, 0.1353353f, 0.04978707f,
	0.01831564f, 0.006737947f, 0.002478752f, 0.000911882f,
	};

    private static float getDamageFalloff(float x)
    {
	int u = (int) x;
	if (u >= falloff.length - 1) { 
	    return 0;
	}

	float t = x - u;

	return (falloff[u] * (1 - t)) + (falloff[u + 1] * t);
    }

    static float getDamageToInflict(Pos pos, EntityActor target, Warhead warhead, Weapon weapon, float modifier, boolean withFalloff)
    {
	// don't hit air units with splash from ground explosions, etc
	if (!weapon.isValidAgainst(target)) {
	    return 0;
	}

	int maxHp = target.getMaxHp();
	if (maxHp == 0) {
	    return 0;
	}

	float rawDamage = warhead.damage;
	if (withFalloff)
	{
	    float distance = (float) Math.max(0, target.getPosition().distanceTo(pos) - 0.416);
	    float foff = getDamageFalloff(distance * 1f / warhead.spread);
	    rawDamage = foff * rawDamage;
	}
	
	return (float)(rawDamage * modifier * (float)warhead.getEffectivenessFor(target));
    }
}
