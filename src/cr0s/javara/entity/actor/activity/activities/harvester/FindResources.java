package cr0s.javara.entity.actor.activity.activities.harvester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

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

	    moveActivity = new Move(me, pathToResource, new Pos(
		    pathToResource.getX(pathToResource.getLength() - 1),
		    pathToResource.getY(pathToResource.getLength() - 1)), null);
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

    private Pos getUnvisitedChildNode(Set<Pos> visited, Pos currentNode,
	    EntityHarvester harv) {
	// All possible adjacent cells directions
	int dx[] = { +1, -1, 0, 0, +1, -1, +1, -1 };
	int dy[] = { 0, 0, +1, -1, +1, +1, -1, -1 };

	for (int i = 0; i < 8; i++) {
	    int newCellX = (int) currentNode.getX() + dx[i];
	    int newCellY = (int) currentNode.getY() + dy[i];

	    Pos cell = new Pos(newCellX, newCellY);

	    if (!harv.world.isCellPassable(cell)) {
		continue;
	    }

	    if (!visited.contains(cell)) {
		return cell;
	    }
	}

	return null;
    }

    /**
     * Searches closest cell with resources using Breadth-First Search
     * @param harv
     * @return
     */
    private Pos getClosestResourceCellBfs(final EntityHarvester harv) {
	int searchRadius = (harv.lastOrderPoint != null || harv.lastHarvestedPoint != null) ? harv.SEARCH_RADIUS_FROM_ORDER
		: harv.SEARCH_RADIUS_FROM_PROC;
	final Pos centerPos = (harv.lastOrderPoint != null) ? harv.lastOrderPoint
		: (harv.lastHarvestedPoint != null) ? harv.lastHarvestedPoint
			: harv.getCellPos();

	if (isCellChoosable(harv, centerPos)) {
	    return centerPos;
	}

	Queue<Pos> queue = new LinkedList<Pos>();
	queue.add(centerPos);

	Set<Pos> visited = new HashSet<>();
	visited.add(centerPos);

	while (!queue.isEmpty()) {
	    Pos node = (Pos) queue.remove();
	    Pos child = null;
	    while ((child = getUnvisitedChildNode(visited, node, harv)) != null) {
		visited.add(child);

		if (isCellChoosable(harv, child)) {
		    return child;
		}

		queue.add(child);
	    }
	}

	return null;
    }

    private boolean isCellChoosable(EntityHarvester harv, Pos cellPos) {
	boolean shroudObscures = harv.owner.getShroud() != null
		&& !harv.owner.getShroud().isExplored(cellPos);
	boolean isHarvesterPoint = (int) cellPos.getX() == (int) harv
		.getCellPos().getX()
		&& (int) cellPos.getY() == (int) harv.getCellPos().getY();

	return !shroudObscures
		&& (isHarvesterPoint || harv.world.isCellPassable(cellPos))
		&& !harv.world.getMap().getResourcesLayer()
			.isCellEmpty(cellPos);
    }

    private Path findPathToClosestResourceCell(final EntityHarvester harv) {
	Pos resourcePos = getClosestResourceCellBfs(harv);
	if (resourcePos != null) {
	    Path pathToResource = harv.findPathFromTo(harv,
		    (int) resourcePos.getX(), (int) resourcePos.getY());

	    return pathToResource;
	}

	return null;
    }
}
