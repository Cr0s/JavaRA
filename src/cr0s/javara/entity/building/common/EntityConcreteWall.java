package cr0s.javara.entity.building.common;

import cr0s.javara.entity.building.BibType;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;

public class EntityConcreteWall extends EntityWall {

    private static final int BUILDING_COST = 100;

    public EntityConcreteWall(Float aTileX, Float aTileY, Team aTeam, Player aPlayer) {
	this(aTileX, aTileY, aTeam, aPlayer, 24, 24, "x");
    }    
    
    public EntityConcreteWall(Float aTileX, Float aTileY, Team aTeam,
	    Player aPlayer, float aSizeWidth, float aSizeHeight,
	    String aFootprint) {
	super(aTileX, aTileY, aTeam, aPlayer, aSizeWidth, aSizeHeight, "x");
	
	this.textureName = "brik.shp";
	loadTextures();
	
	setBibType(BibType.NONE);
	setProgressValue(-1);

	setMaxHp(10);
	setHp(getMaxHp());

	this.makeTextureName = "";
	this.unitProductionAlingment = Alignment.NEUTRAL;	
    }

    @Override
    public int getBuildingCost() {
	return this.BUILDING_COST;
    }
}
