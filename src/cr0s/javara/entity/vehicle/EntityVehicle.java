package cr0s.javara.entity.vehicle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
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
import cr0s.javara.util.RotationUtil;

public abstract class EntityVehicle extends MobileEntity implements IShroudRevealer {
    public int tileX, tileY;

    public boolean isPrimary = false, isRepairing = false, isInvuln = false, isDestroyed = false;

    private int moveWaitTicks = 0;

    protected int buildingSpeed;

    public EntityVehicle(float posX, float posY, Team team, Player player, int sizeWidth, int sizeHeight) {
	super(posX, posY, team, player, sizeWidth, sizeHeight);
    }

    @Override
    public void updateEntity(int delta) {
	super.updateEntity(delta);
    }

    @Override
    public abstract void renderEntity(Graphics g);

    public static EntityVehicle newInstance(EntityVehicle b) {
	Constructor ctor;
	try {
	    ctor = (b.getClass()).getDeclaredConstructor(Float.class, Float.class, Team.class, Player.class);
	    ctor.setAccessible(true);
	    EntityVehicle newEntityVehicle = ((EntityVehicle)ctor.newInstance(b.posX, b.posY, b.team, b.owner));

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
}
