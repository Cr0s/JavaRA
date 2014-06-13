package cr0s.javara.combat;

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
	
	boolean isInWater = (pos.getZ() == 0 && world.getMap().getSurfaceIdAt(targetTile) == world.getMap().getTileSet().SURFACE_WATER_ID);
	String explosionType = isInWater ? warhead.waterExplosion : warhead.explosion;
	
	if (explosionType != null && !explosionType.isEmpty()) {
	    world.spawnExplosionAt(pos, explosionType);
	}
	
	SoundManager.getInstance().playSfxAt(getImpactSound(warhead, isInWater), pos);
    }
}
