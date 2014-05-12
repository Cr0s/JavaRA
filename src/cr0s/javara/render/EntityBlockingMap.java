package cr0s.javara.render;

import java.util.Arrays;
import java.util.LinkedList;

import org.newdawn.slick.geom.Point;

import cr0s.javara.entity.Entity;
import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.building.EntityBuilding;
import cr0s.javara.render.EntityBlockingMap.SubCell;

public class EntityBlockingMap {
    private World world;
    private LinkedList<Influence> blockingMap[][];
    
    public enum SubCell { FULL_CELL, TOP_LEFT, TOP_RIGHT, CENTER, BOTTOM_LEFT, BOTTOM_RIGHT };
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
	clearMap();
    }
    
    public void occupyForMobileEntity(MobileEntity me) {
	switch (me.fillsSpace) {
	case ONE_CELL:
		occupyCell(me.getCellPos(), me);
		if (me.isMovingToCell) {
		    occupyCell(new Point(me.targetCellX, me.targetCellY), me);
		}
	case DONT_FILLS:
	    break;
	case ONE_OR_MORE_CELLS:
	    // TODO:
	    break;
	case ONE_SUBCELL:
		occupySubCell(me.getCellPos(), me.currentSubcell, me);
		if (me.isMovingToCell) {
		    occupySubCell(new Point(me.targetCellX, me.targetCellY), me.desiredSubcell, me);
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
		    freeCell(new Point(me.targetCellX, me.targetCellY));
		}
	case DONT_FILLS:
	    break;
	case ONE_OR_MORE_CELLS:
	    // TODO:
	    break;
	case ONE_SUBCELL:
		freeSubCell(me.getCellPos(), me.currentSubcell);
		if (me.isMovingToCell) {
		    freeSubCell(new Point(me.targetCellX, me.targetCellY), me.desiredSubcell);
		}
	    break;
	default:
	    break;
	}
	
    }    
    
    public void freeCell(Point cellPos) {
	freeSubCell(cellPos, SubCell.FULL_CELL);
    }
    
    public void freeSubCell(Point cellPos, SubCell sc) {
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
    
    public void occupySubCell(Point cellPos, SubCell sc, Entity e) {
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
    
    public void occupyCell(Point cellPos, Entity e) {
	LinkedList<Influence> infList = new LinkedList<>();
	infList.add(new Influence(SubCell.FULL_CELL, e));
	
	this.blockingMap[(int) cellPos.getX()][(int) cellPos.getY()] = infList;
    }
    
    public boolean isSubcellFree(Point pos, SubCell sub) {
	if (sub == null) {
	    System.out.println("ERR: null subcell!");
	    return false;
	}
	
	// Whole cell is free
	if (this.blockingMap[(int) pos.getX()][(int) pos.getY()] == null) {
	    return true;
	}
	
	// Cell 100% occupied, but we need whole cell, so this cell absolutely is not free
	if (!this.blockingMap[(int) pos.getX()][(int) pos.getY()].isEmpty() && sub == SubCell.FULL_CELL) {
	    return false;
	}
	
	for (Influence cellInf : this.blockingMap[(int) pos.getX()][(int) pos.getY()]) {
	    if (cellInf.subcell == sub || cellInf.subcell == SubCell.FULL_CELL) {
		return false;
	    }
	}
	
	return true;
    }
    
    public boolean isFullCellOccupied(Point pos) {
	// Whole cell is free
	if (this.blockingMap[(int) pos.getX()][(int) pos.getY()] == null) {
	    return false;
	}
	
	for (Influence cellInf : this.blockingMap[(int) pos.getX()][(int) pos.getY()]) {
	    if (cellInf.subcell == SubCell.FULL_CELL) {
		return true;
	    } 
	}
	
	return false;
    }    
    
    public class Influence {
	public SubCell subcell;
	public Entity entity;
	
	public Influence(SubCell sc, Entity e) {
	    this.subcell = sc;
	    this.entity = e;
	}
    }

    public SubCell getFreeSubCell(Point pos, SubCell currentSubcell) {
	if (this.isSubcellFree(pos, currentSubcell)) {
	    return currentSubcell;
	}
	
	if (isFullCellOccupied(pos)) {
	    return null;
	}
	
	for (SubCell sc : SubCell.values()) {
	    if (this.isSubcellFree(pos, sc)) {
		return sc;
	    }
	}
	
	return null;
    }

    public LinkedList<Influence> getCellInfluences(Point pos) {
	return this.blockingMap[(int) pos.getX()][(int) pos.getY()];
    }
}
