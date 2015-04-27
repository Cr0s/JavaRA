package cr0s.javara.gameplay;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import cr0s.javara.combat.Warhead;
import cr0s.javara.entity.Entity;
import cr0s.javara.entity.INotifySelected;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.building.soviet.EntityFireTurret;
import cr0s.javara.entity.building.soviet.EntityTeslaCoil;
import cr0s.javara.entity.vehicle.common.EntityMcv;
import cr0s.javara.entity.vehicle.soviet.EntityMammothTank;
import cr0s.javara.entity.vehicle.soviet.EntityV2Launcher;
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
		e = new EntityRifleTrooper(randomPoint.getX() * 24, randomPoint.getY() * 24, team, this, world.blockingEntityMap.getFreeSubCell(randomPoint, SubCell.CENTER));
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

	Player other = new Player(world, "", Alignment.SOVIET, new Color(0, 200, 0));
	/*EntityMammothTank eht = new EntityMammothTank(24.0f * this.spawnX + 12 * 24, 24.0f * this.spawnY + 7 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(eht);

	EntityMammothTank eht2 = new EntityMammothTank(24.0f * this.spawnX + 14 * 24, 24.0f * this.spawnY + 7 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(eht2);
	
	EntityMammothTank emt3 = new EntityMammothTank(24.0f * this.spawnX + 13 * 24, 24.0f * this.spawnY + 6 * 24, team, this);
	eht.isVisible = true;
	this.world.spawnEntityInWorld(emt3);	
		*/
	/*EntityConstructionYard m = new EntityConstructionYard(24.0f * this.spawnX, 24.0f * this.spawnY, team, other);
	m.isVisible = true;
	*/
	/*EntityFireTurret eft = new EntityFireTurret(24.0f * this.spawnX + 24 * 10, 24.0f * this.spawnY + 24 * 5, team, other);
	eft.isVisible = true;
	//this.world.spawnEntityInWorld(eft);
	
	EntityTeslaCoil etc = new EntityTeslaCoil(24.0f * this.spawnX + 24 * 10, 24.0f * this.spawnY + 24 * 4, team, other);
	etc.isVisible = true;
	this.world.spawnEntityInWorld(etc);	*/
/*
	this.world.spawnEntityInWorld(m);	
	*/
	this.base.gainCash(10000);
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
    
    public void update(int delta) {
	this.base.update();
    }

    public void notifyDamaged(Entity entity, EntityActor firedBy, int amount,
	    Warhead warhead) {

    }

    public boolean isEnemyFor(Player player) {
	return this.team != player.team;
    }
}
