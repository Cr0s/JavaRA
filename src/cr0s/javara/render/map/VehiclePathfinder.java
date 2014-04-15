package cr0s.javara.render.map;

import org.newdawn.slick.util.pathfinding.AStarHeuristic;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.TileBasedMap;
import org.newdawn.slick.util.pathfinding.heuristics.ClosestHeuristic;
import org.newdawn.slick.util.pathfinding.heuristics.ClosestSquaredHeuristic;
import org.newdawn.slick.util.pathfinding.heuristics.ManhattanHeuristic;

import cr0s.javara.entity.vehicle.EntityVehicle;
import cr0s.javara.render.World;

/**
 * A* pathfinding class for vehicles.
 * @author Cr0s
 */
public class VehiclePathfinder {
    private AStarPathFinder pathfinder;
    private static final int MAX_SEARCH_DISTANCE = 512;
    
    public VehiclePathfinder(World world) {
	this.pathfinder = new AStarPathFinder(world, MAX_SEARCH_DISTANCE,
		true, new ClosestHeuristic());
    }
    
    public Path findPathFromTo(EntityVehicle me, int goalX, int goalY) {
	return this.pathfinder.findPath(me, (int) me.posX / 24, (int) me.posY / 24, goalX, goalY);
    }
}

class VehicleHeuristic implements AStarHeuristic {
    public static final float ADJACENT_COST = 1f;
    public static final float DIAGONAL_COST = (float)Math.sqrt(2);
    
    
    @Override
    public float getCost(TileBasedMap ctx, Mover mover, int x, int y,
	    int goalX, int goalY) {
	return Math.max(Math.abs(x - goalX), Math.abs(y - goalY));
	/*
	      float diagonal = Math.min(Math.abs(x - goalX), Math.abs(y - goalY));
	      float straight = (Math.abs(x - goalX) + Math.abs(y - goalY));
	      float h = (DIAGONAL_COST * diagonal) + (ADJACENT_COST * (straight - (2f * diagonal)));

	      return h;*/
    }
    

}