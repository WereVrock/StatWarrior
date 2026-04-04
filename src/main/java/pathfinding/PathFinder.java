package pathfinding;

import dungeon.DungeonCell;
import main.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public final class PathFinder {

    private static final int[][] DIRS = {
        {0, -1}, {0, 1}, {-1, 0}, {1, 0}
    };

    private PathFinder() {}

    public static List<int[]> findPath(final int startX, final int startY,
                                        final int goalX,  final int goalY) {

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int rows = grid.length;
        final int cols = grid[0].length;

        if (!inBounds(startX, startY, cols, rows) || !inBounds(goalX, goalY, cols, rows)) {
            return Collections.emptyList();
        }

        final PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
        final Set<Integer> closed = new HashSet<>();

        open.add(new Node(startX, startY, null, 0f, heuristic(startX, startY, goalX, goalY)));

        while (!open.isEmpty()) {
            final Node current = open.poll();
            final int key = current.y * cols + current.x;

            if (closed.contains(key)) continue;
            closed.add(key);

            if (current.x == goalX && current.y == goalY) {
                return buildPath(current);
            }

            for (final int[] dir : DIRS) {
                final int nx = current.x + dir[0];
                final int ny = current.y + dir[1];

                if (!inBounds(nx, ny, cols, rows)) continue;
                if (!grid[ny][nx].isActive())       continue;
                if (closed.contains(ny * cols + nx)) continue;

                final float g = current.g + 1f;
                final float h = heuristic(nx, ny, goalX, goalY);
                open.add(new Node(nx, ny, current, g, h));
            }
        }

        return Collections.emptyList();
    }

    private static List<int[]> buildPath(Node node) {
        final List<int[]> path = new ArrayList<>();
        while (node != null) {
            path.add(new int[]{node.x, node.y});
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static float heuristic(final int x, final int y, final int gx, final int gy) {
        return Math.abs(x - gx) + Math.abs(y - gy);
    }

    private static boolean inBounds(final int x, final int y, final int cols, final int rows) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }
}