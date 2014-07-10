package cr0s.javara.entity.building.soviet;

import cr0s.javara.entity.building.BibType;
import cr0s.javara.entity.building.common.EntityWall;
import cr0s.javara.gameplay.Player;
import cr0s.javara.gameplay.Team;
import cr0s.javara.gameplay.Team.Alignment;

public class EntityBarredWireWall extends EntityWall {

    private static final int BUILDING_COST = 30;

    public EntityBarredWireWall(Float aTileX, Float aTileY, Team aTeam,
	    Player aPlayer, float aSizeWidth, float aSizeHeight,
	    String aFootprint) {
	super(aTileX, aTileY, aTeam, aPlayer, aSizeWidth, aSizeHeight, "x");
	
	this.textureName = "fenc.shp";
	loadTextures();
	
	setBibType(BibType.NONE);
	setProgressValue(-1);

	setMaxHp(5);
	setHp(getMaxHp());

	this.makeTextureName = "";
	this.unitProductionAlingment = Alignment.NEUTRAL;
	
	this.setName("fenc");
    }

    public EntityBarredWireWall(Float aTileX, Float aTileY, Team aTeam, Player aPlayer) {
	this(aTileX, aTileY, aTeam, aPlayer, 24, 24, "x");
    }

    @Override
    public int getBuildingCost() {
	return this.BUILDING_COST;
    }
}
