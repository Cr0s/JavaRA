package cr0s.javara.gameplay;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;

import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.entity.building.EntityConstructionYard;
import cr0s.javara.entity.building.EntityWarFactory;
import cr0s.javara.main.Main;
import cr0s.javara.render.World;
import cr0s.javara.render.map.TileSet;
import cr0s.javara.resources.SoundManager;

public class BuildingOverlay {
    private Player player;
    private World world;

    private EntityBuilding targetBuilding;

    private int cellX, cellY;
    private Color filterColor = new Color(255, 255, 255, 128);
    private Color blockedCellColor = new Color(255, 0, 0, 64);
    private Color freeCellColor = new Color(128, 128, 128, 64);

    public BuildingOverlay(Player p, World w) {
	this.player = p;
	this.world = w;
    }

    public void render(Graphics g) {
	Color pColor = g.getColor();
	
	if (this.targetBuilding != null) {	    
	    drawCYCircles(g);
	    g.setColor(pColor);
		
	    for (int bX = 0; bX < this.targetBuilding.getWidthInTiles(); bX++) {
		for (int bY = 0; bY < this.targetBuilding.getHeightInTiles(); bY++) {
		    if (!this.player.getBase().checkBuildingDistance(cellX, cellY)) {
			    g.setColor(blockedCellColor);
			    g.fillRect((cellX + bX) * 24 , (cellY + bY) * 24, 24, 24);			
		    } else if (this.targetBuilding.getBlockingCells()[bX][bY] != TileSet.SURFACE_CLEAR_ID) {
			if (!world.isCellBuildable(cellX + bX , cellY + bY)) {
			    g.setColor(blockedCellColor);
			    g.fillRect((cellX + bX) * 24 , (cellY + bY) * 24, 24, 24);
			} else if (this.targetBuilding.getBlockingCells()[bX][bY] != TileSet.SURFACE_CLEAR_ID){
			    g.setColor(freeCellColor);
			    g.fillRect((cellX + bX) * 24 , (cellY + bY) * 24, 24, 24);
			}
		    }
		}
	    }

	    g.setColor(pColor);
	    
	    // War Factory workaround: WF texture consist from two textures: bottom and top, we need draw both to get full image of WF
	    if (this.targetBuilding instanceof EntityWarFactory) { 
		((EntityWarFactory) this.targetBuilding).getBottomTexture().draw(cellX * 24, cellY * 24, filterColor);
	    }
	    
	    this.targetBuilding.getTexture().draw(cellX * 24, cellY * 24, filterColor);	    
	}
    }

    public void drawCYCircles(Graphics g) {
	g.setColor(Color.gray);
	g.setLineWidth(2);
	for (EntityBuilding eb : this.player.getBase().getBuildings()) {
	    if (eb instanceof EntityConstructionYard) {		
		Circle c = new Circle(eb.getTileX() + (eb.sizeWidth / 2), eb.getTileY() + (eb.sizeHeight / 2), Base.BUILDING_CY_RANGE * 24);
		g.draw(c);
	    }
	}
	
	g.setLineWidth(1);
    }
    
    public void update(int delta) {
	cellX = (int) (-Main.getInstance().getCamera().getOffsetX() + Main.getInstance().getContainer().getInput().getMouseX()) / 24;
	cellY = (int) (-Main.getInstance().getCamera().getOffsetY() + Main.getInstance().getContainer().getInput().getMouseY()) / 24;
	
	if (!Main.getInstance().getPlayer().getBase().getProductionQueue().canBuild(this.targetBuilding)) {
	    resetBuildingMode();
	}
    }

    public void setBuildingMode(EntityBuilding eb) {
	this.targetBuilding = eb;
    }
    
    public void resetBuildingMode() {
	this.targetBuilding = null;
    }
    
    public void mouseClick(int button) {
	if (button == 0 && targetBuilding != null) {
	    boolean result = player.getBase().tryToBuild(cellX, cellY, targetBuilding);
	    
	    if (result) {
		this.resetBuildingMode();
	    } else {
		// "Cannot deploy here"
		SoundManager.getInstance().playSpeechSoundGlobal("nodeply1");
	    }
	} else if (button == 1 && targetBuilding != null) {
	    this.resetBuildingMode();
	}
    }
    
    public boolean isInBuildingMode() {
	return this.targetBuilding != null;
    }
}
