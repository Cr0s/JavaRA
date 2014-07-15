package cr0s.javara.entity.building;

import java.io.File;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import cr0s.javara.entity.ISelectable;
import cr0s.javara.entity.IShroudRevealer;
import cr0s.javara.entity.building.common.EntityConstructionYard;
import cr0s.javara.entity.building.common.EntityWall;
import cr0s.javara.main.Main;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;
import cr0s.javara.resources.SoundManager;

public class EntityBuildingProgress extends EntityBuilding implements IShroudRevealer {

    private EntityBuilding targetBuilding;
    private ShpTexture makeTexture;

    private int ticksRemaining;

    private int currentFrame;

    public EntityBuildingProgress(EntityBuilding aTargetBuilding) {
	super((float) aTargetBuilding.getTileX(), (float) aTargetBuilding.getTileY(), aTargetBuilding.team, aTargetBuilding.owner, aTargetBuilding.getWidth(), aTargetBuilding.getHeight(), aTargetBuilding.getFootprint());

	this.targetBuilding = aTargetBuilding;
	this.targetBuilding.posX = this.posX;
	this.targetBuilding.posY = this.posY;

	if (!targetBuilding.makeTextureName.isEmpty()) {
	    makeTexture = ResourceManager.getInstance().getConquerTexture(targetBuilding.makeTextureName);
	    this.ticksRemaining = makeTexture.numImages - 1;
	} else {
	    this.ticksRemaining = 1;
	}
	
	setBibType(this.targetBuilding.getBibType());

	this.setMaxHp(10);
	this.setHp(10);

	// Set building progress is invulnerable to avoid glitches
	this.setInvuln(true);
	
	// Play "building" sound
	if (this.owner == Main.getInstance().getPlayer()) {
	    SoundManager.getInstance().playSfxGlobal("placbldg", 0.7f);
	    
	    if (!(this.targetBuilding instanceof EntityWall)) {
		SoundManager.getInstance().playSfxGlobal("build5", 0.7f);
	    }
	}
	
	this.owner.getBase().addToCurrentlyBuilding(this);
    }

    @Override
    public void updateEntity(int delta) {
	this.currentFrame++;
	
	if (--this.ticksRemaining <= 0) {
	    setDead();

	    this.owner.getBase().addBuilding(this.targetBuilding);
	    this.owner.getBase().getCurrentlyBuilding().remove(this);
	    
	    this.targetBuilding.isVisible = true;
	    world.spawnEntityInWorld(this.targetBuilding);

	    this.targetBuilding.onBuildFinished();
	}
    }

    @Override
    public void renderEntity(Graphics g) {
	if (this.makeTexture == null) {
	    return;
	}
	
	this.makeTexture.getAsImage(this.currentFrame, this.owner.playerColor).draw(this.posX, this.posY);
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return this.targetBuilding.shouldRenderedInPass(passNum);
    }

    @Override
    public float getHeightInTiles() {
	return this.tileHeight;
    }

    @Override
    public float getWidthInTiles() {
	return this.tileWidth;
    }

    @Override
    public int getRevealingRange() {
	if (this.targetBuilding instanceof IShroudRevealer) {
	    return ((IShroudRevealer) targetBuilding).getRevealingRange() / 2;
	} else {
	    return 0;
	}
    }

    @Override
    public Image getTexture() {
	return this.targetBuilding.getTexture();
    }

    public EntityBuilding getTargetBuilding() {
	return this.targetBuilding;
    }  
}
