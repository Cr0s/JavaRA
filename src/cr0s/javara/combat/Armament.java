package cr0s.javara.combat;

import java.util.ArrayList;
import java.util.LinkedList;

import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.turreted.IHaveTurret;
import cr0s.javara.entity.turreted.Turret;
import cr0s.javara.order.Target;
import cr0s.javara.render.Sequence;
import cr0s.javara.util.Action;
import cr0s.javara.util.Pos;
import cr0s.javara.util.RotationUtil;

public class Armament {
    public class Barrel {
	public Pos offset;
	public float yaw;

	public Barrel(Pos o, float y) {
	    this.offset = o;
	    this.yaw = y;
	}
    }

    public String name = "primary";
    private EntityActor self;
    private Weapon weapon;
    private Sequence muzzleSequence;

    private ArrayList<Barrel> barrels;

    private int fireDelay;

    private int burst;

    private LinkedList<Action> delayedActions;

    public Armament(EntityActor s, Weapon weap) {
	this.self = s;
	this.weapon = weap;
	this.burst = weap.burst;

	this.barrels = new ArrayList<Barrel>();
	this.delayedActions = new LinkedList<Action>();
    }

    public void addBarrel(Pos offset, float yaw) {
	this.barrels.add(new Barrel(offset, yaw));
    }

    public void update(int delta) {
	if (fireDelay > 0) {
	    fireDelay--;
	}

	for (Action a : this.delayedActions) {
	    if (--a.delay <= 0) {
		a.execute();
	    }
	}

	if (this.delayedActions.size() > 0) {
	    LinkedList<Action> list = new LinkedList<Action>();
	    for (Action a : this.delayedActions) {
		if (a.delay > 0) {
		    list.add(a);
		}
	    }

	    this.delayedActions = list;
	}
    }

    private void scheduleDelayedAction(int delay, Action a) {
	if (delay > 0) {
	    a.delay = delay;

	    this.delayedActions.add(a);
	} else {
	    a.execute();
	}
    }

    public Pos getMuzzlePos(Barrel b) {
	Pos actorCenter = (this.self instanceof IHaveTurret)
		? ((IHaveTurret) this.self).getTurrets().get(0).getCenterPos()
		: this.self.getPosition();

		int sourceFacing = (this.self instanceof IHaveTurret) 
			? ((IHaveTurret) this.self).getTurrets().get(0).getCurrentFacing() 
			: this.self.currentFacing;

			float angle = RotationUtil.facingToAngle(sourceFacing, this.self.getMaxFacings());

			// Offset muzzle to required length from center
			float offsetFromCenter = b.offset.getX();
			float muzzleX = (float) (actorCenter.getX() - (offsetFromCenter * Math.sin(angle)));
			float muzzleY = (float) (actorCenter.getY() - (offsetFromCenter * Math.cos(angle)));

			// Offset muzzle left or right from center axis
			float offsetSide = b.offset.getY();
			muzzleX -= offsetSide * Math.cos(angle);
			muzzleY -= offsetSide * -Math.sin(angle);

			return new Pos(muzzleX, muzzleY);
    }

    public ArrayList<Barrel> getBarrels() {
	return this.barrels;
    }

    public boolean isReloading() {
	return this.fireDelay > 0;
    }

    public boolean isShouldExplode() {
	return !isReloading();
    }

    public Barrel checkFire(int facing, final Target tgt) {
	if (this.fireDelay > 0) {
	    return null;
	}

	Pos actorCenter = (this.self instanceof IHaveTurret)
		? ((IHaveTurret) this.self).getTurrets().get(0).getCenterPos()
			: this.self.getPosition();

		if (!tgt.isInRange(actorCenter, this.weapon.range)) {
		    return null;
		}

		if (this.weapon.minRange != 0 && tgt.isInRange(actorCenter, this.weapon.minRange)) {
		    return null;
		}

		if (!this.weapon.isValidAgainst(tgt)) {
		    return null;
		}
		
		Barrel brl = this.barrels.get(this.burst % this.barrels.size());
		final Pos muzzlePosition = this.getMuzzlePos(brl);
		final int fcng = this.getMuzzleFacing(brl);

		this.scheduleDelayedAction(this.fireDelay, new Action() {
		    @Override
		    public void execute() {
			Projectile prj = Armament.this.weapon.createProjectile(fcng, muzzlePosition, Armament.this.self, tgt.centerPosition(), tgt);
			prj.isVisible = true;
			prj.setWorld(Armament.this.self.world);

			Armament.this.self.world.spawnEntityInWorld(prj);
			Armament.this.weapon.playReportSound(Armament.this.self.getPosition());
		    }
		});


		if (this.self instanceof IHaveTurret) {
		    for (Turret t : ((IHaveTurret) this.self).getTurrets()) {
			t.recoil();
		    }
		}

		if (--this.burst > 0) {
		    this.fireDelay = this.weapon.burstDelay;
		} else {
		    this.fireDelay = this.weapon.rateOfFire;
		    this.burst = this.weapon.burst;
		}

		return brl;
    }

    private int getMuzzleFacing(Barrel brl) {
	Pos actorCenter = (this.self instanceof IHaveTurret)
		? ((IHaveTurret) this.self).getTurrets().get(0).getCenterPos()
			: this.self.getPosition();

		int sourceFacing = (this.self instanceof IHaveTurret) 
			? ((IHaveTurret) this.self).getTurrets().get(0).getCurrentFacing() 
				: this.self.currentFacing;

			float angle = RotationUtil.facingToAngle(sourceFacing, this.self.getMaxFacings());
			angle += brl.yaw;

			return RotationUtil.angleToFacing(angle);
    }

    public Weapon getWeapon() {
	return this.weapon;
    }
}
