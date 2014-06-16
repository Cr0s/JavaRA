package cr0s.javara.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeSet;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.main.Main;
import cr0s.javara.order.Target;
import cr0s.javara.render.World;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.util.Pos;

public abstract class Weapon {
    public int rateOfFire = 1;
    public int burst = 1;
    public boolean charges = false;
    public ArrayList<TargetType> validTargets = new ArrayList<TargetType>();
    public TreeSet<TargetType> invalidTargets = new TreeSet<TargetType>();

    public float range = 5;
    public float minRange = 0;

    public int burstDelay = 5;

    public Projectile projectile;
    public LinkedList<Warhead> warheads;

    public String firingSound;

    public Weapon(int rof, float weaponRange) {
	this.rateOfFire = rof;
	this.range = weaponRange;

	this.warheads = new LinkedList<Warhead>();
    }

    public boolean isValidAgainst(EntityActor e) {
	boolean isEffective = false;

	for (Warhead w : this.warheads) {
	    if (w.getEffectivenessFor(e) > 0) {
		isEffective = true;
		break;
	    }
	}

	if (!isEffective) {
	    return false;
	}

	if (!e.targetTypes.isEmpty()) {
	    boolean anyValidTarget = false;

	    for (TargetType tt : this.validTargets) {
		anyValidTarget = e.targetTypes.contains(tt) && !this.invalidTargets.contains(tt);

		if (anyValidTarget) {
		    break;
		}
	    }

	    if (!anyValidTarget) {
		return false;
	    }
	} else {
	    return false;
	}

	return true;
    }


    public boolean isValidAgainst(Pos cellPos, World w) {
	TargetType tt = w.getCellTargetType(cellPos);

	boolean anyValidTarget = false;

	for (TargetType weaponTargetType : this.validTargets) {
	    anyValidTarget = (weaponTargetType == tt) && !this.invalidTargets.contains(tt);

	    if (anyValidTarget) {
		return true;
	    }
	}

	return false;
    }

    public boolean isValidAgainst(Target t) {
	if (t.isEntityTarget()) {
	    return this.isValidAgainst((EntityActor) t.getTargetEntity());
	} else if (t.isCellTarget()) {
	    if (!Main.getInstance().getWorld().getMap().isInMap(t.getTargetCell())) {
		return false;
	    }
	    
	    return this.isValidAgainst(t.getTargetCell(), Main.getInstance().getWorld());
	}

	return false;
    }

    public abstract Projectile createProjectile(int fcng, Pos muzzlePosition,
	    EntityActor srcActor, Pos centerPosition, Target tgt);

    public void playReportSound(Pos pos) {
	if (this.firingSound != null && !this.firingSound.isEmpty()) {
	    SoundManager.getInstance().playSfxAt(this.firingSound, pos);
	}
    }
}
