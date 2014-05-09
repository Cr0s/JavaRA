package cr0s.javara.entity.actor.activity.activities.harvester;

import java.util.ArrayList;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.pathfinding.Path;

import cr0s.javara.entity.MobileEntity;
import cr0s.javara.entity.actor.EntityActor;
import cr0s.javara.entity.actor.activity.Activity;
import cr0s.javara.entity.actor.activity.activities.Move;
import cr0s.javara.entity.vehicle.common.EntityHarvester;
import cr0s.javara.util.CellChooser;

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
	    moveActivity = new Move(me, pathToResource, new Point(pathToResource.getX(pathToResource.getLength() - 1), pathToResource.getY(pathToResource.getLength() - 1)), null);
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
	Point centerPos = (harv.lastOrderPoint != null) ? harv.lastOrderPoint : (harv.lastHarvestedPoint != null) ? harv.lastHarvestedPoint : harv.getCellPos();
	
	ArrayList<Path> pathesToResources = new ArrayList<Path>();
	for (int range = 1; range <= searchRadius; range++) {
	    Point resourcePos = null;
	    ArrayList<Point> resourcePoints = harv.world.chooseTilesInCircle(centerPos, range, new CellChooser() {

		@Override
		public boolean isCellChoosable(Point cellPos) {
		    if (harv.world.isCellPassable((int) cellPos.getX(), (int) cellPos.getY()) && !harv.world.getMap().getResourcesLayer().isCellEmpty((int) cellPos.getX(), (int) cellPos.getY())) {
			return true;
		    }
		    
		    return false;
		}
		
	    });
	    
	    // We found some resource points
	    if (resourcePoints.size() != 0) {
		for (int i = 0; i < resourcePoints.size(); i++) {
		    resourcePos = resourcePoints.get(i);
		    Path pathToResource = harv.findPathFromTo(harv, (int) resourcePos.getX(), (int) resourcePos.getY());
		    
		    if (pathToResource != null) {
			return pathToResource; // FIXME: get path to closest cell is ignoring obstacles and actual path length
			//pathesToResources.add(pathToResource);
			//break;
		    }
		}
	    }
	}
	
	Path pathToResource = null;
	/* FIXME: // We found some paths, select shortest
	if (pathesToResources.size() != 0) {
	    pathToResource = pathesToResources.get(0);
	    for (Path path : pathesToResources) {
		if (path.getLength() < pathToResource.getLength()) {
		    pathToResource = path;
		}
	    }
	}*/
	
	return pathToResource;
    }
}
