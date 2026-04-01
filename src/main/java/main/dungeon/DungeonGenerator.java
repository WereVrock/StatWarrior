package dungeon;

public final class DungeonGenerator {

    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;

    private static final int CORRIDOR_THICKNESS = 3;
    private static final int OUTER_CORRIDOR_THICKNESS = 1; // walkable outer corridor
    private static final int EDGE_WALL_THICKNESS = 1; // always-wall outermost layer

    private DungeonGenerator() {
        // utility class
    }

    public static DungeonCell[][] generate(final DungeonType type) {
        final DungeonCell[][] grid = createEmptyGrid();

        switch (type) {
            case PLUS:
                generatePlusWithOuterCorridors(grid);
                break;
            default:
                throw new IllegalArgumentException("Unsupported dungeon type");
        }

        return grid;
    }

    private static DungeonCell[][] createEmptyGrid() {
        final DungeonCell[][] grid = new DungeonCell[HEIGHT][WIDTH];

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grid[y][x] = new DungeonCell(false); // all cells start as walls
            }
        }

        return grid;
    }

    private static void generatePlusWithOuterCorridors(final DungeonCell[][] grid) {
        final int centerX = WIDTH / 2;
        final int centerY = HEIGHT / 2;
        final int halfCorridor = CORRIDOR_THICKNESS / 2;

        // 1. Outer edge walls
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (x < EDGE_WALL_THICKNESS || x >= WIDTH - EDGE_WALL_THICKNESS ||
                    y < EDGE_WALL_THICKNESS || y >= HEIGHT - EDGE_WALL_THICKNESS) {
                    grid[y][x].setActive(false); // ensure outermost walls
                }
            }
        }

        // 2. Outer corridor inside the edge walls
        for (int y = EDGE_WALL_THICKNESS; y < HEIGHT - EDGE_WALL_THICKNESS; y++) {
            for (int x = EDGE_WALL_THICKNESS; x < WIDTH - EDGE_WALL_THICKNESS; x++) {
                if (x < EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS ||
                    x >= WIDTH - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS ||
                    y < EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS ||
                    y >= HEIGHT - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS) {
                    grid[y][x].setActive(true); // walkable outer corridor
                }
            }
        }

        // 3. Vertical plus corridor
        for (int y = EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS; 
             y < HEIGHT - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS; y++) {
            for (int dx = -halfCorridor; dx <= halfCorridor; dx++) {
                int x = centerX + dx;
                if (x > EDGE_WALL_THICKNESS - 1 && x < WIDTH - EDGE_WALL_THICKNESS) {
                    grid[y][x].setActive(true);
                }
            }
        }

        // 4. Horizontal plus corridor
        for (int x = EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS; 
             x < WIDTH - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS; x++) {
            for (int dy = -halfCorridor; dy <= halfCorridor; dy++) {
                int y = centerY + dy;
                if (y > EDGE_WALL_THICKNESS - 1 && y < HEIGHT - EDGE_WALL_THICKNESS) {
                    grid[y][x].setActive(true);
                }
            }
        }

        // All other cells remain walls
    }
}