package ui;

import controls.HybridController;
import dungeon.DungeonCell;
import main.Main;

import javax.swing.*;
import java.awt.*;

public final class GamePanel extends JPanel {

    private static final int TILE_SIZE = 32;

    public GamePanel() {
        setFocusable(true);
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(800, 600));

        if (Main.CONTROLLER instanceof HybridController hybrid) {
            addKeyListener(hybrid.getKeyboard());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int offsetX = Main.CAMERA.getOffsetX();
        final int offsetY = Main.CAMERA.getOffsetY();

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                final DungeonCell cell = grid[y][x];

                g.setColor(cell.isActive() ? Color.WHITE : Color.DARK_GRAY);
                g.fillRect(
                        x * TILE_SIZE - offsetX,
                        y * TILE_SIZE - offsetY,
                        TILE_SIZE,
                        TILE_SIZE
                );
            }
        }

        Main.PLAYER.render(g, TILE_SIZE, offsetX, offsetY);
    }
}