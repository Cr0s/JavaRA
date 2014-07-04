package cr0s.javara.entity.actor.activity.activities.harvester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.util.CellChooser;
import cr0s.javara.util.PointsUtil;
import cr0s.javara.util.Pos;

public class FindResources extends Activity {

    public FindResources() {
    }

    @Override
    public Activity tick(EntityActor a) {
	if (isCancelled || !(a instanceof EntityHarvester)) {
	    return nextActivity;
	}
	
	MobileEntity me = (MobileEntity) a;
	EntityHarvester harv = (EntityHarvester) a;
	
	Path pathToResource = findPathToClosestResourceCell(harv);
	Move moveActivity = null;
	
	if (pathToResource != null) {
	    if (harv.isFull()) {
		DeliverResources deliverActivity = new DeliverResources();
		deliverActivity.queueActivity(nextActivity);
		
		return deliverActivity;		
	    }
	    
	    moveActivity = new Move(me, pathToResource, new Pos(pathToResource.getX(pathToResource.getLength() - 1), pathToResource.getY(pathToResource.getLength() - 1)), null);
	} else {
	    if (!harv.isEmpty()) {
		DeliverResources deliverActivity = new DeliverResources();
		deliverActivity.queueActivity(nextActivity);
		
		return deliverActivity;
	    }
	    
	    return nextActivity;
	}
	
	moveActivity.queueActivity(new HarvestResource());
	moveActivity.queueActivity(nextActivity);
	return moveActivity;
    }

    private Path findPathToClosestResourceCell(final EntityHarvester harv) {
	int searchRadius = (harv.lastOrderPoint != null || harv.lastHarvestedPoint != null) ? harv.SEARCH_RADIUS_FROM_ORDER : harv.SEARCH_RADIUS_FROM_PROC;
	final Pos centerPos = (harv.lastOrderPoint != null) ? harv.lastOrderPoint : (harv.lastHarvestedPoint != null) ? harv.lastHarvestedPoint : harv.getCellPos();

	final int MAX_PATHS_PER_POINT = 10;
	Pos resourcePos = null;
	ArrayList<Pos> resourcePoints = harv.world.chooseTilesInCircle(centerPos, searchRadius, new CellChooser() {

	    @Override
	    public boolean isCellChoosable(Pos cellPos) {
		boolean shroudObscures = harv.owner.getShroud() != null && !harv.owner.getShroud().isExplored(cellPos);
		boolean isHarvesterPoint = (int) cellPos.getX() == (int) harv.getCellPos().getX() && (int) cellPos.getY() == (int) harv.getCellPos().getY();
		
		return !shroudObscures && (isHarvesterPoint || harv.world.isCellPassable(cellPos)) && !harv.world.getMap().getResourcesLayer().isCellEmpty(cellPos);
	    }

	});

	ArrayList<Path> pathsToResources = null;
	
	// We found some resource points
	if (resourcePoints.size() != 0) {
	    pathsToResources = new ArrayList<Path>();
	    
	    // Sort by "closest to harvest point goes first"
	    Collections.sort(resourcePoints, new Comparator<Pos>() {
		@Override
		public int compare(Pos p1, Pos p2) {
		    return p1.distanceToSq(centerPos) - p2.distanceToSq(centerPos);
		}
	    });
	    
	    int pointsCount = Math.min(resourcePoints.size(), MAX_PATHS_PER_POINT);
	    
	    for (int i = 0; i < pointsCount; i++) {
		resourcePos = resourcePoints.get(i);
		
		Path pathToResource = harv.findPathFromTo(harv, (int) resourcePos.getX(), (int) resourcePos.getY());

		if (pathToResource != null) {
		    pathsToResources.add(pathToResource);
		}
	    }
	}
	
	Path pathToResource = null;
	
	// We found some paths, select shortest
	if (pathsToResources != null && pathsToResources.size() != 0) {
	    pathToResource = pathsToResources.get(0);
	    for (Path path : pathsToResources) {
		if (path.getLength() < pathToResource.getLength()) {
		    pathToResource = path;
		}
	    }
	}
	
	return pathToResource;
    }
}
