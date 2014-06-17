package cr0s.javara.gameplay;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.INotifySelected;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.entity.vehicle.tank.EntityHeavyTank;
import cr0s.javara.gameplay.Team.Alignment;
import cr0s.javara.order.OrderTargeter;
import cr0s.javara.order.Target;
import cr0s.javara.render.World;
import cr0s.javara.render.shrouds.Shroud;
import cr0s.javara.util.Pos;

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

    public Pos getPlayerSpawnPoint() {
	return new Pos(this.spawnX, this.spawnY);
    }

    public void spawn() {
	EntityMcv mcv = new EntityMcv(24.0f * this.spawnX, 24.0f * this.spawnY, team, this);
	mcv.isVisible = true;

	this.world.spawnEntityInWorld(mcv);

	/*EntityHeavyTank eht = new EntityHeavyTank(24.0f * this.spawnX + 3 * 24, 24.0f * this.spawnY + 3 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(eht);

	int n = this.world.getRandomInt(15, 50);

	for (int i = 0; i < n; i++) {
	    int rx = this.spawnX + (this.world.getRandomInt(-10, 10));
	    int ry = this.spawnY + (this.world.getRandomInt(-10, 10));

	    Pos randomPoint = new Pos(rx, ry);

	    if (!world.isCellPassable(randomPoint)) {
		continue;
	    }
	    
	    EntityInfantry e = null;
	    switch (this.world.getRandomInt(0, 3)) {
	    case 0:
		e = new EntityRiffleTrooper(randomPoint.getX() * 24, randomPoint.getY() * 24, team, this, world.blockingEntityMap.getFreeSubCell(randomPoint, SubCell.CENTER));
		break;
		
	    case 1:
		e = new EntityRocketTrooper(randomPoint.getX() * 24, randomPoint.getY() * 24, team, this, world.blockingEntityMap.getFreeSubCell(randomPoint, SubCell.CENTER));
		break;
		
	    case 2:
		e = new EntityGrenadeTrooper(randomPoint.getX() * 24, randomPoint.getY() * 24, team, this, world.blockingEntityMap.getFreeSubCell(randomPoint, SubCell.CENTER));
		break;
	    }
	    
	    e.currentFacing = this.world.getRandomInt(0, EntityInfantry.MAX_FACING);
	    e.isVisible = true; this.world.spawnEntityInWorld(e);
	}*/

	Player other = new Player(world, "", Alignment.SOVIET, new Color(200, 0, 0));
	EntityHeavyTank eht = new EntityHeavyTank(24.0f * this.spawnX + 3 * 24, 24.0f * this.spawnY + 3 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(eht);

	EntityHeavyTank eht2 = new EntityHeavyTank(24.0f * this.spawnX + 4 * 24, 24.0f * this.spawnY + 3 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(eht2);
	
	EntityHeavyTank eht3 = new EntityHeavyTank(24.0f * this.spawnX + 3 * 24, 24.0f * this.spawnY + 2 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(eht3);	
	
	EntityConstructionYard y = new EntityConstructionYard(24.0f * this.spawnX + 0 * 24, 24.0f * this.spawnY + 0 * 24, team, other);
	y.isVisible = true;
	this.world.spawnEntityInWorld(y);
	
	this.base.gainCash(5000);
    }

    public OrderTargeter getBestOrderTargeterForTarget(Target target) {
	if (this.selectedEntities.isEmpty()) {
	    return null;
	}

	OrderTargeter bestTargeter = null;
	for (Entity e : this.selectedEntities) {
	    if (!(e instanceof EntityActor) || e.isDead() || !e.isSelected || e.owner != this) {
		continue;
	    }

	    EntityActor actor = (EntityActor) e;

	    for (OrderTargeter ot : actor.getOrders()) {
		if (bestTargeter == null || (ot.canTarget(e, target) && ot.priority > bestTargeter.priority)) {
		    bestTargeter = ot;
		}
	    }
	}

	return bestTargeter;
    }

    public void selectOneEntity(Entity e) {
	this.selectedEntities.clear();

	if (e instanceof EntityActor && !e.isDead() && e.isVisible) {
	    e.isSelected = true;
	    this.selectedEntities.addFirst(e);

	    if (e instanceof INotifySelected) {
		((INotifySelected) e).notifySelected();
	    }

	    if (e.owner == this) { 
		((EntityActor) e).playSelectedSound();
	    }
	}
    }    

    public boolean isAnyActorEntitySelected() {
	boolean isAnySelected = false;

	for (Entity e : this.selectedEntities) {
	    if (!e.isSelected && (e instanceof EntityActor && !e.isDead() && e.isVisible)) {
		isAnySelected = true;
	    }
	}

	return isAnySelected;
    }

    public void removeNotActuallySelectedEntities() {
	LinkedList<Entity> list = new LinkedList<>();
	for (Entity e : this.selectedEntities) {
	    if (e.isSelected && !e.isDead() && e.isVisible) {
		list.add(e);
	    }
	}

	this.selectedEntities = list;
    }

    public Team getTeam() {
	return this.team;
    }
}
