package entity;

import balance.Balance;
import pathfinding.PathFinder;

import java.util.Collections;
import java.util.List;

public final class EnemyNavigator {

    private List<int[]> currentPath;
    private int pathIndex;

    private final EnemyBody body;
    private final String id;

    public EnemyNavigator(final EnemyBody body, final String id) {
        this.body        = body;
        this.id          = id;
        this.currentPath = Collections.emptyList();
        this.pathIndex   = 0;
    }

    public void recalculatePath(final int goalTileX, final int goalTileY) {
        final int myTileX = body.toTileX(body.centerX());
        final int myTileY = body.toTileY(body.centerY());

        if (myTileX == goalTileX && myTileY == goalTileY) {
            currentPath = Collections.emptyList();
            pathIndex = 0;
            return;
        }

        final List<int[]> path = PathFinder.findPath(myTileX, myTileY, goalTileX, goalTileY);

        if (!path.isEmpty()) {
            currentPath = path;
            pathIndex = 1;
            log("path recalculated, " + path.size() + " steps to (" + goalTileX + ", " + goalTileY + ")");
        }
    }

    public void followPath() {
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            return;
        }

        final int[] target   = currentPath.get(pathIndex);
        final float targetCX = target[0] * body.getTileSize() + body.getTileSize() / 2f;
        final float targetCY = target[1] * body.getTileSize() + body.getTileSize() / 2f;
        final float dx       = targetCX - body.centerX();
        final float dy       = targetCY - body.centerY();
        final float dist     = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < Balance.ENEMY_WAYPOINT_REACH_DIST) {
            pathIndex++;
            if (pathIndex < currentPath.size()) {
                final int[] next = currentPath.get(pathIndex);
                log("waypoint reached, next=(" + next[0] + ", " + next[1] + ")");
            }
            return;
        }

        body.accelerate(
                (dx / dist) * Balance.ENEMY_ACCELERATION,
                (dy / dist) * Balance.ENEMY_ACCELERATION
        );
    }

    public void clearPath() {
        currentPath = Collections.emptyList();
        pathIndex   = 0;
    }

    public boolean isPathEmpty() {
        return currentPath.isEmpty() || pathIndex >= currentPath.size();
    }

    private void log(final String msg) {
        System.out.println("[" + id + "] Nav: " + msg);
    }
}