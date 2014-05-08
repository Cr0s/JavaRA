package cr0s.javara.gameplay;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.IMovable;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.entity.vehicle.tank.EntityHeavyTank;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.render.World;
import cr0s.javara.render.shrouds.Shroud;

public class Player {
    public String name;
    private Alignment side;

    public Color playerColor;

    public int powerLevel = 0;
    public int powerConsumptionLevel = 0;

    public boolean canBuild, canTrainInfantry, canBuildVehicles, canBuildAirUnits, canBuildNavalUnits;

    public LinkedList<Entity> selectedEntities = new LinkedList<>();

    private int spawnX, spawnY;

    private Base base;
    private Team team;

    private Shroud playerShroud;
    private World world;
    
    public Player(World w, String name, Alignment side, Color color) {
	this.name = name;
	this.side = side;
	this.playerColor = color;

	this.world = w;
	this.base = new Base(team, this);
	this.playerShroud = new Shroud(w, this);
    }

    public void postMoveOrder(float destX, float destY) {
	for (Entity e : this.selectedEntities) {
	    if (!e.isDead() && (e instanceof IMovable)) {
		((IMovable)e).moveTo((int)destX, (int)destY);
	    }
	}
    }

    public Alignment getAlignment() {
	return this.side;
    }

    public Base getBase() {
	return this.base;
    }

    public void setTeam(Team t) {
	t.addPlayer(this);
	this.team = t;
    }

    public Shroud getShroud() {
	return this.playerShroud;
    }
    
    public void setShroud(Shroud s) {
	this.playerShroud = s;
    }

    public void setSpawn(int x, int y) {
	this.spawnX = x;
	this.spawnY = y;
    }
    
    public Point getPlayerSpawnPoint() {
	return new Point(this.spawnX, this.spawnY);
    }

    public void spawn() {
	EntityMcv mcv = new EntityMcv(24.0f * this.spawnX, 24.0f * this.spawnY, team, this);
	mcv.isVisible = true;
	
	this.world.spawnEntityInWorld(mcv);
	
	EntityHeavyTank eht = new EntityHeavyTank(24.0f * this.spawnX + 3 * 24, 24.0f * this.spawnY + 3 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(eht);
    }
}
