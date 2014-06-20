package cr0s.javara.entity.infantry;

import java.util.ArrayList;

import org.newdawn.slick.BigImage;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import cr0s.javara.combat.Armament;
import cr0s.javara.combat.attack.AttackFrontal;
import cr0s.javara.combat.weapon.WeaponM1Carabine;
import cr0s.javara.entity.IHaveCost;
import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.actor.activity.activities.MoveInfantry;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.render.Sequence;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.util.Pos;

public class EntityRifleTrooper extends EntityInfantry implements ISelectable, IHaveCost {

    private final int BUILD_COST = 100;
    
    public EntityRifleTrooper(Float posX, Float posY, Team team, Player owner) {
	this(posX, posY, team, owner, SubCell.CENTER);
    }    
    
    public EntityRifleTrooper(Float posX, Float posY, Team team, Player owner,
	    SubCell sub) {
	super(posX, posY, team, owner, sub);

	this.texture = ResourceManager.getInstance().getInfantryTexture("e1.shp");

	this.setMaxHp(15);
	this.setHp(15);

	this.currentFrame = 0;

	this.standSequence = new Sequence(texture, 0, 8, 0, 0, owner.playerColor);
	this.runSequence = new Sequence(texture, 16, 8, 6, 2, owner.playerColor);
	this.runSequence.setIsLoop(true);

	this.attackingSequence = new Sequence(texture, 64, 8, 8, 2, owner.playerColor);
	//this.attackingSequence.setIsLoop(true);
	
	this.idleSequences.add(new Sequence(texture, 256, 0, 16, 2, owner.playerColor));
	this.idleSequences.add(new Sequence(texture, 272, 0, 16, 2, owner.playerColor));
	
	this.attack = new AttackFrontal(this);
	Armament arma = new Armament(this, new WeaponM1Carabine());
	arma.addBarrel(new Pos(0, 0), 0);
	this.attack.addArmament(arma);
	
	this.ordersList.addAll(this.attack.getOrders());
    }

    @Override 
    public void updateEntity(int delta) {	
	super.updateEntity(delta);
    }

    @Override
    public int getMinimumEnoughRange() {
	return 3;
    }

    @Override
    public int getWaitAverageTime() {
	// TODO Auto-generated method stub
	return 50;
    }

    @Override
    public int getWaitSpreadTime() {
	// TODO Auto-generated method stub
	return 10;
    }

    @Override
    public int getRevealingRange() {
	return 4;
    }

    @Override
    public void select() {
	this.isSelected = true;
    }

    @Override
    public void cancelSelect() {
	this.isSelected = false;
    }

    @Override
    public boolean isSelected() {
	return this.isSelected;
    }

    @Override
    public int getBuildingCost() {
	return BUILD_COST;
    }
}
