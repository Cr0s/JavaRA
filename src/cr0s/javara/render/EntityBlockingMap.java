package cr0s.javara.render;

import java.util.Arrays;
import java.util.LinkedList;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.perfomance.Profiler;
import cr0s.javara.render.EntityBlockingMap.SubCell;
import cr0s.javara.render.map.TileSet;
import cr0s.javara.util.Pos;

public class EntityBlockingMap {
    private World world;
    private LinkedList<Influence> blockingMap[][];
    
    public enum SubCell { FULL_CELL, TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT, FULL_CELL_PASSABLE };
    public enum FillsSpace { ONE_OR_MORE_CELLS, ONE_CELL, ONE_SUBCELL, DONT_FILLS }
    
    public EntityBlockingMap(World w) {
	this.world = w;
	
	this.blockingMap = new LinkedList[w.getMap().getWidth()][w.getMap().getHeight()];
    }
    
    private void clearMap() {
	for (int i = 0; i < this.world.getMap().getWidth(); i++) {
	    Arrays.fill(this.blockingMap[i], null);
	}
    }
    
    public void update() {
	Profiler.getInstance().startForSection("World: blocking map clear");
	clearMap();
	Profiler.getInstance().stopForSection("World: blocking map clear");
    }
    
    public void occupyForMobileEntity(MobileEntity me) {
	switch (me.fillsSpace) {
	case ONE_CELL:
		occupyCell(me.getCellPos(), me);
		if (me.isMovingToCell) {
		    occupyCell(new Pos(me.targetCellX, me.targetCellY), me);
		}
	case DONT_FILLS:
	    break;
	case ONE_OR_MORE_CELLS:
	    // TODO:
	    break;
	case ONE_SUBCELL:
		occupySubCell(me.getCellPos(), me.currentSubcell, me);
		if (me.isMovingToCell) {
		    occupySubCell(new Pos(me.targetCellX, me.targetCellY), me.desiredSubcell, me);
		}
	    break;
	default:
	    break;
	}
	
    }
    
    public void freeForMobileEntity(MobileEntity me) {
	switch (me.fillsSpace) {
	case ONE_CELL:
		freeCell(me.getCellPos());
		
		if (me.isMovingToCell) {
		    freeCell(new Pos(me.targetCellX, me.targetCellY));
		}
	case DONT_FILLS:
	    break;
	case ONE_OR_MORE_CELLS:
	    // TODO:
	    break;
	case ONE_SUBCELL:
		freeSubCell(me.getCellPos(), me.currentSubcell);
		if (me.isMovingToCell) {
		    freeSubCell(new Pos(me.targetCellX, me.targetCellY), me.desiredSubcell);
		}
	    break;
	default:
	    break;
	}
	
    }    
    
    public void freeCell(Pos cellPos) {
	freeSubCell(cellPos, SubCell.FULL_CELL);
    }
    
    public void freeSubCell(Pos cellPos, SubCell sc) {
	LinkedList<Influence> infList = this.blockingMap[(int) cellPos.getX()][(int) cellPos.getY()];
	
	if (sc == SubCell.FULL_CELL) {
	    this.blockingMap[(int) cellPos.getX()][(int) cellPos.getY()] = null;
	}
	
	if (infList != null && !infList.isEmpty()) {
	    for (Influence inf : infList) {
		if (inf.subcell == sc) {
		    infList.remove(sc);
		    
		    if (infList.isEmpty()) {
			this.blockingMap[(int) cellPos.getX()][(int) cellPos.getY()] = null;
		    }
		    
		    return;
		}
	    }
	}
    }
    
    public void occupySubCell(Pos cellPos, SubCell sc, Entity e) {
	if (sc == SubCell.FULL_CELL) {
	    occupyCell(cellPos, e);
	    return;
	}
	
	LinkedList<Influence> infList = this.blockingMap[(int) cellPos.getX()][(int) cellPos.getY()];
	
	if (infList == null) {
	    infList = new LinkedList<>();
	    this.blockingMap[(int) cellPos.getX()][(int) cellPos.getY()] = infList;
	}
	
	infList.add(new Influence(sc, e));
    }    
    
    public void occupyCell(Pos cellPos, Entity e) {
	LinkedList<Influence> infList = new LinkedList<>();
	infList.add(new Influence(SubCell.FULL_CELL, e));
	
	this.blockingMap[(int) cellPos.getX()][(int) cellPos.getY()] = infList;
    }
    
    public boolean isSubcellFree(Pos pos, SubCell sub) {
	if (sub == null) {
	    System.out.println("ERR: null subcell!");
	    return false;
	}
	
	if (isFullCellOccupied(pos)) {
	    return false;
	}
	
	if (this.blockingMap[(int) pos.getX()][(int) pos.getY()] == null || this.blockingMap[(int) pos.getX()][(int) pos.getY()].isEmpty()) {
	    return true;
	}
	
	if (sub == SubCell.FULL_CELL) {
	    boolean isFree = false;
	    
	    for (Influence cellInf : this.blockingMap[(int) pos.getX()][(int) pos.getY()]) {
		if (cellInf.subcell != SubCell.FULL_CELL_PASSABLE) {
		     isFree = false;
		     break;
		} else {
		    isFree = true;
		}
	    }
	    
	    return isFree;
	}
	
	for (Influence cellInf : this.blockingMap[(int) pos.getX()][(int) pos.getY()]) {
	    if (cellInf.subcell == sub || cellInf.subcell == SubCell.FULL_CELL) {
		return false;
	    }
	}
	
	return true;
    }
    
    public boolean isFullCellOccupied(Pos pos) {
	// Whole cell is free
	if (this.blockingMap[(int) pos.getX()][(int) pos.getY()] == null || this.blockingMap[(int) pos.getX()][(int) pos.getY()].isEmpty()) {
	    return false;
	}
	
	for (Influence cellInf : this.blockingMap[(int) pos.getX()][(int) pos.getY()]) {
	    if (cellInf.subcell == SubCell.FULL_CELL) {
		return true;
	    } 
	}
	
	return false;
    }    
    
    public boolean isAnyInfluenceInCell(Pos pos) {
	return this.blockingMap[(int) pos.getX()][(int) pos.getY()] != null;
    }
    
    public class Influence {
	public SubCell subcell;
	public Entity entity;
	
	public Influence(SubCell sc, Entity e) {
	    this.subcell = sc;
	    this.entity = e;
	}
    }

    public SubCell getFreeSubCell(Pos pos, SubCell currentSubcell) {
	if (this.isSubcellFree(pos, currentSubcell)) {
	    return currentSubcell;
	}
	
	if (isFullCellOccupied(pos)) {
	    return null;
	}
	
	for (SubCell sc : SubCell.values()) {
	    if (sc == SubCell.FULL_CELL || sc == SubCell.FULL_CELL_PASSABLE) {
		continue;
	    }
	    
	    if (this.isSubcellFree(pos, sc)) {
		return sc;
	    }
	}
	
	return null;
    }

    public LinkedList<Influence> getCellInfluences(Pos pos) {
	return this.blockingMap[(int) pos.getX()][(int) pos.getY()];
    }

    public void occupyForBuilding(EntityBuilding eb) {
	for (int by = 0; by < eb.getHeightInTiles(); by++) {
	    for (int bx = 0; bx < eb.getWidthInTiles(); bx++) {
		int x = ((eb.getTileX() + 12) / 24) + bx;
		int y = ((eb.getTileY() + 12) / 24) + by;
		int blockType = eb.getBlockingCells()[bx][by];
		
		switch (blockType) {
		case TileSet.SURFACE_BUILDING:
		    occupySubCell(new Pos(x, y), SubCell.FULL_CELL, eb);
		    break;
		    
		case TileSet.SURFACE_BUILDING_CLEAR_ID:
		//case TileSet.SURFACE_CLEAR_ID:
		    occupySubCell(new Pos(x, y), SubCell.FULL_CELL_PASSABLE, eb);
		    break;
		}
	    }
	}
    }
    
    public boolean isEntityInCell(Pos pos, Entity e) {
	if (!isAnyInfluenceInCell(pos)) {
	    return false;
	}
	
	for (Influence cellInf : this.blockingMap[(int) pos.getX()][(int) pos.getY()]) {
	    if (cellInf.entity == e) {
		return true;
	    }
	}
	
	return false;
    }

    /**
     * Returns true if any entity except building is have influence to specified cell
     * @param cellPos cell position
     * @return result
     */
    public boolean isAnyUnitInCell(Pos cellPos) {
	if (!this.isAnyInfluenceInCell(cellPos)) {
	    return false;
	}
	
	for (Influence e : this.getCellInfluences(cellPos)) {
	    if (e.entity != null && !(e.entity instanceof EntityBuilding)) {
		return true;
	    }
	}
	
	return false;
    }
}
