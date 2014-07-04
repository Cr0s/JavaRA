package cr0s.javara.entity.vehicle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import cr0s.javara.combat.TargetType;
import cr0s.javara.combat.Warhead;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.effect.Explosion;
import cr0s.javara.entity.effect.SmokeOnUnit;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.order.Order;
import cr0s.javara.render.EntityBlockingMap.FillsSpace;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.util.Pos;

public abstract class EntityVehicle extends MobileEntity implements IShroudRevealer {
    public int tileX, tileY;

    public boolean isPrimary = false, isRepairing = false, isInvuln = false, isDestroyed = false;

    private int moveWaitTicks = 0;

    protected int buildingSpeed;
    Color nextColor = new Color(0, 255, 0, 64);

    private final String SELECTED_SOUND = "vehic1";
    private HashMap<String, Integer[]> orderSounds;
    private final int MAX_VERSIONS = 4;

    private String explosionType = "veh-hit3.shp";
    private String explosionSound = "kaboom30";
    
    public EntityVehicle(float posX, float posY, Team team, Player player, int sizeWidth, int sizeHeight) {
	super(posX, posY, team, player, sizeWidth, sizeHeight);

	this.selectedSounds.put(this.SELECTED_SOUND, new Integer[] { 0, 2 });
	this.selectedSounds.put("await1", new Integer[] { 0, 1, 2, 3 });
	this.selectedSounds.put("yessir1", new Integer[] { 0, 1, 2, 3 });

	this.orderSounds = new HashMap<>();
	this.orderSounds.put("ackno", new Integer[] { 0, 1, 2, 3 });
	this.orderSounds.put("affirm1", new Integer[] { 0, 1, 2, 3 });
	this.orderSounds.put("noprob", new Integer[] { 1, 3 });
	this.orderSounds.put("overout", new Integer[] { 1, 3 });
	this.orderSounds.put("ritaway", new Integer[] { 1, 3 });
	this.orderSounds.put("roger", new Integer[] { 1, 3 });
	this.orderSounds.put("ugotit", new Integer[] { 1, 3 });

	this.unitVersion = SoundManager.getInstance().r.nextInt(4); // from 0 to 3

	this.fillsSpace = FillsSpace.ONE_CELL;

	this.targetTypes.add(TargetType.GROUND);
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
    }


    @Override
    public void renderEntity(Graphics g) {
	super.renderEntity(g);
    }

    public static EntityVehicle newInstance(EntityVehicle b) {
	Constructor ctor;
	try {
	    ctor = (b.getClass()).getDeclaredConstructor(Float.class, Float.class, Team.class, Player.class);
	    ctor.setAccessible(true);
	    EntityVehicle newEntityVehicle = (EntityVehicle) ctor.newInstance(b.posX, b.posY, b.team, b.owner);

	    return newEntityVehicle;
	} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
		| IllegalArgumentException | InvocationTargetException e) {
	    e.printStackTrace();
	}

	return null;
    }

    public int getBuildingSpeed() {
	return this.buildingSpeed;
    }	

    @Override
    public void notifySelected() {
    }

    public String getSelectSound() {
	return this.SELECTED_SOUND;
    }

    @Override
    public void playSelectedSound() {
	for (String s : this.selectedSounds.keySet()) {
	    Integer[] versions = this.selectedSounds.get(s);

	    boolean canPlay = false;
	    for (int i = 0; i < Math.min(this.MAX_VERSIONS, versions.length); i++) {
		if (versions[i] == this.unitVersion) {
		    canPlay = true;
		    break;
		}
	    }

	    if (SoundManager.getInstance().r.nextBoolean() && canPlay) {
		SoundManager.getInstance().playUnitSoundGlobal(this, s, this.unitVersion);
		return;
	    }
	}

	SoundManager.getInstance().playUnitSoundGlobal(this, this.SELECTED_SOUND, 0);
    }

    @Override
    public void resolveOrder(Order order) {
	super.resolveOrder(order);
    }    

    @Override
    public void playOrderSound() {
	// Play order sound
	for (String s : this.orderSounds.keySet()) {
	    Integer[] versions = this.orderSounds.get(s);

	    if (this.unitVersion >= versions.length) {
		continue;
	    }

	    boolean canPlay = false;
	    for (int i = 0; i < Math.min(this.MAX_VERSIONS, versions.length); i++) {
		if (versions[i] == this.unitVersion) {
		    canPlay = true;
		    break;
		}
	    }

	    if (SoundManager.getInstance().r.nextBoolean() && canPlay) {
		SoundManager.getInstance().playUnitSoundGlobal(this, s, this.unitVersion);
		return;
	    }
	}

	if (SoundManager.getInstance().r.nextBoolean()) {
	    SoundManager.getInstance().playUnitSoundGlobal(this, "ackno", this.unitVersion);
	} else {
	    SoundManager.getInstance().playUnitSoundGlobal(this, "affirm1", this.unitVersion);
	}	
    }

    @Override
    public boolean canEnterCell(Pos cellPos) {
	return world.blockingEntityMap.isEntityInCell(cellPos, this) || world.isCellPassable(cellPos);
    }

    @Override
    public Activity moveToRange(Pos cellPos, int range) {
	Move move = new Move(this, cellPos, range);
	move.forceRange = true;

	return move;
    }    

    @Override
    public void giveDamage(EntityActor firedBy, int amount, Warhead warhead) {	
	if (this.isInvuln) {
	    return;
	}
	
	if (this.isDead() || this.getHp() <= 0) {
	    return;
	}
	
	if (this.getHp() > 0 && this.getHp() - amount <= 0) { // prevent overkill and spawn unit explosion
	    this.world.spawnExplosionAt(this.getPosition(), this.explosionType);
	    SoundManager.getInstance().playSfxAt(this.explosionSound, this.getPosition());
	    
	    this.setHp(0);
	    this.setDead();
	    
	    return;
	}
		
	// Unit is half damaged, spawn following smoke
	if (this.getHp() >= this.getMaxHp() / 2 && this.getHp() - amount < this.getMaxHp() / 2) {
	    SmokeOnUnit sou = new SmokeOnUnit(this, "smoke_m.shp");
	    sou.isVisible = true;
	    this.world.spawnEntityInWorld(sou);
	}
	
	this.setHp(this.getHp() - amount);	
    }    
}
