package dungeon;

import main.Main;

import java.awt.Color;
import java.awt.Graphics;

public final class Dungeon {

    private static final int TILE_SIZE = 32;

    private final DungeonCell[][] grid;

    public Dungeon() {
        this.grid = DungeonGenerator.generate(DungeonType.PLUS);
    }

    public DungeonCell[][] getGrid() {
        return grid;
    }

    public void render(Graphics g) {

        final int offsetX = Main.CAMERA.getOffsetX();
        final int offsetY = Main.CAMERA.getOffsetY();

        for (int y = 0; y < grid.length; y++) {
            final DungeonCell[] row = grid[y];

            for (int x = 0; x < row.length; x++) {
                final DungeonCell cell = row[x];

                g.setColor(cell.isActive() ? Color.WHITE : Color.DARK_GRAY);

                g.fillRect(
                        x * TILE_SIZE - offsetX,
                        y * TILE_SIZE - offsetY,
                        TILE_SIZE,
                        TILE_SIZE
                );
            }
        }
    }
}