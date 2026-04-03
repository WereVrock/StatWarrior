package dungeon;

public final class DungeonGenerator {

    private static final int WIDTH                    = 45;
    private static final int HEIGHT                   = 45;
    private static final int CORRIDOR_THICKNESS       = 7;
    private static final int OUTER_CORRIDOR_THICKNESS = 5;
    private static final int EDGE_WALL_THICKNESS      = 1;

    private DungeonGenerator() {}

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
                grid[y][x] = new DungeonCell(false);
            }
        }
        return grid;
    }

    private static void generatePlusWithOuterCorridors(final DungeonCell[][] grid) {
        final int centerX = WIDTH / 2;
        final int centerY = HEIGHT / 2;
        final int halfCorridor = CORRIDOR_THICKNESS / 2;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (x < EDGE_WALL_THICKNESS || x >= WIDTH - EDGE_WALL_THICKNESS ||
                    y < EDGE_WALL_THICKNESS || y >= HEIGHT - EDGE_WALL_THICKNESS) {
                    grid[y][x].setActive(false);
                }
            }
        }

        for (int y = EDGE_WALL_THICKNESS; y < HEIGHT - EDGE_WALL_THICKNESS; y++) {
            for (int x = EDGE_WALL_THICKNESS; x < WIDTH - EDGE_WALL_THICKNESS; x++) {
                if (x < EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS ||
                    x >= WIDTH - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS ||
                    y < EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS ||
                    y >= HEIGHT - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS) {
                    grid[y][x].setActive(true);
                }
            }
        }

        for (int y = EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS;
             y < HEIGHT - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS; y++) {
            for (int dx = -halfCorridor; dx <= halfCorridor; dx++) {
                int x = centerX + dx;
                if (x > EDGE_WALL_THICKNESS - 1 && x < WIDTH - EDGE_WALL_THICKNESS) {
                    grid[y][x].setActive(true);
                }
            }
        }

        for (int x = EDGE_WALL_THICKNESS + OUTER_CORRIDOR_THICKNESS;
             x < WIDTH - EDGE_WALL_THICKNESS - OUTER_CORRIDOR_THICKNESS; x++) {
            for (int dy = -halfCorridor; dy <= halfCorridor; dy++) {
                int y = centerY + dy;
                if (y > EDGE_WALL_THICKNESS - 1 && y < HEIGHT - EDGE_WALL_THICKNESS) {
                    grid[y][x].setActive(true);
                }
            }
        }
    }
}