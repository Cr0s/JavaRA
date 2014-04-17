package cr0s.javara.entity.building;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import cr0s.javara.entity.ISelectable;
import cr0s.javara.resources.ResourceManager;
import cr0s.javara.resources.ShpTexture;

public class EntityBuildingProgress extends EntityBuilding implements ISelectable {

    private EntityBuilding targetBuilding;
    private ShpTexture makeTexture;

    private final int FRAME_DELAY_TICKS = 10; // In ticks, will be multiplied by 1/buildSpeed
    private int updateTicks = 0;
    
    private Image currentFrameImage;
    
    public EntityBuildingProgress(EntityBuilding aTargetBuilding) {
	super(aTargetBuilding.getTileX(), aTargetBuilding.getTileY(), aTargetBuilding.team, aTargetBuilding.owner, aTargetBuilding.getWidth(), aTargetBuilding.getHeight(), aTargetBuilding.getFootprint());
    
	this.targetBuilding = aTargetBuilding;
	makeTexture = ResourceManager.getInstance().getConquerTexture(targetBuilding.makeTextureName);
	this.currentFrameImage = makeTexture.getAsImage(0, this.owner.playerColor);
	
	setBibType(this.targetBuilding.getBibType());
	
	this.setMaxHp(10);
	this.setHp(10);
	
	this.setMaxProgress(makeTexture.numImages - 1);
	this.setProgressValue(0);
    }

    @Override
    public void updateEntity(int delta) {
	if (updateTicks++ < FRAME_DELAY_TICKS * (1.0f / Math.max(1, targetBuilding.buildingSpeed))) { 
	    return;
	}
	
	updateTicks = 0;
	this.setProgressValue(this.getProgressValue() + 1);
	
	this.currentFrameImage = makeTexture.getAsImage(this.getProgressValue(), this.owner.playerColor);
	
	if (this.getProgressValue() == this.getMaxProgress()) {
	    setDead();
	    
	    this.targetBuilding.isVisible = true;
	    world.spawnEntityInWorld(this.targetBuilding);
	}
    }

    @Override
    public void renderEntity(Graphics g) {
	this.currentFrameImage.draw(this.posX, this.posY);
    }

    @Override
    public boolean shouldRenderedInPass(int passNum) {
	return (passNum == 0);
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
    public float getHeightInTiles() {
	return this.tileHeight;
    }

    @Override
    public float getWidthInTiles() {
	return this.tileWidth;
    }

}
