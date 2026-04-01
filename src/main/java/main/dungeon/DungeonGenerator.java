package dungeon;

public final class DungeonGenerator {

    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;

    private static final int CORRIDOR_THICKNESS = 3;
    private static final int OUTER_CORRIDOR_THICKNESS = 1; // thickness of the outer corridor border

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

        // Initialize all cells as walls (inactive)
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

        // 1. Create the outer corridor border (walkable)
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                boolean isOuterCorridor = 
                    x < OUTER_CORRIDOR_THICKNESS || 
                    x >= WIDTH - OUTER_CORRIDOR_THICKNESS ||
                    y < OUTER_CORRIDOR_THICKNESS || 
                    y >= HEIGHT - OUTER_CORRIDOR_THICKNESS;

                if (isOuterCorridor) {
                    grid[y][x].setActive(true);
                }
            }
        }

        // 2. Create vertical plus corridor (walkable)
        for (int y = OUTER_CORRIDOR_THICKNESS; y < HEIGHT - OUTER_CORRIDOR_THICKNESS; y++) {
            for (int dx = -halfCorridor; dx <= halfCorridor; dx++) {
                int x = centerX + dx;
                if (x >= OUTER_CORRIDOR_THICKNESS && x < WIDTH - OUTER_CORRIDOR_THICKNESS) {
                    grid[y][x].setActive(true);
                }
            }
        }

        // 3. Create horizontal plus corridor (walkable)
        for (int x = OUTER_CORRIDOR_THICKNESS; x < WIDTH - OUTER_CORRIDOR_THICKNESS; x++) {
            for (int dy = -halfCorridor; dy <= halfCorridor; dy++) {
                int y = centerY + dy;
                if (y >= OUTER_CORRIDOR_THICKNESS && y < HEIGHT - OUTER_CORRIDOR_THICKNESS) {
                    grid[y][x].setActive(true);
                }
            }
        }

        // 4. Everything else remains walls (active = false), creating the 4 inner blocked rooms
    }
}