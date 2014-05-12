package cr0s.javara.entity.vehicle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Path.Step;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.main.Main;
import cr0s.javara.order.Order;
import cr0s.javara.resources.SoundManager;
import cr0s.javara.util.RotationUtil;

public abstract class EntityVehicle extends MobileEntity implements IShroudRevealer {
    public int tileX, tileY;

    public boolean isPrimary = false, isRepairing = false, isInvuln = false, isDestroyed = false;

    private int moveWaitTicks = 0;

    protected int buildingSpeed;
    Color nextColor = new Color(0, 255, 0, 64);
    
    private final String SELECTED_SOUND = "vehic1";
    private HashMap<String, Integer[]> orderSounds;
    private final int MAX_VERSIONS = 4;
    
    public EntityVehicle(float posX, float posY, Team team, Player player, int sizeWidth, int sizeHeight) {
	super(posX, posY, team, player, sizeWidth, sizeHeight);
	
	this.selectedSounds.put(SELECTED_SOUND, new Integer[] { 0, 2 } );
	this.selectedSounds.put("await1", new Integer[] { 0, 1, 2, 3 } );
	this.selectedSounds.put("yessir1", new Integer[] { 0, 1, 2, 3 } );
	
	this.orderSounds = new HashMap<>();
	this.orderSounds.put("ackno", new Integer[] { 0, 1, 2, 3 });
	this.orderSounds.put("affirm1", new Integer[] { 0, 1, 2, 3 });
	this.orderSounds.put("noprob", new Integer[] { 1, 3 });
	this.orderSounds.put("overout", new Integer[] { 1, 3 });
	this.orderSounds.put("ritaway", new Integer[] { 1, 3 });
	this.orderSounds.put("roger", new Integer[] { 1, 3 });
	this.orderSounds.put("ugotit", new Integer[] { 1, 3 });
	
	this.unitVersion = SoundManager.getInstance().r.nextInt(4); // from 0 to 3
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
    }

    
    @Override
    public void renderEntity(Graphics g) {
	super.renderEntity(g);
	
	//if (this.isMovingToCell) {
	//    g.setColor(this.nextColor);
	//    g.fillRect(this.targetCellX * 24, this.targetCellY * 24, 24, 24);
	//}
    }

    public static EntityVehicle newInstance(EntityVehicle b) {
	Constructor ctor;
	try {
	    ctor = (b.getClass()).getDeclaredConstructor(Float.class, Float.class, Team.class, Player.class);
	    ctor.setAccessible(true);
	    EntityVehicle newEntityVehicle = ((EntityVehicle) ctor.newInstance(b.posX, b.posY, b.team, b.owner));

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
    public abstract boolean canEnterCell(Point cellPos);
    
    @Override
    public void notifySelected() {
    }
    
    public String getSelectSound() {
	return SELECTED_SOUND;
    }
    
    @Override
    public void playSelectedSound() {
	for (String s : this.selectedSounds.keySet()) {
	    Integer[] versions = this.selectedSounds.get(s);
	    
	    boolean canPlay = false;
	    for (int i = 0; i < Math.min(MAX_VERSIONS, versions.length); i++) {
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
	
	SoundManager.getInstance().playUnitSoundGlobal(this, SELECTED_SOUND, 0);
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
	    for (int i = 0; i < Math.min(MAX_VERSIONS, versions.length); i++) {
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
}
