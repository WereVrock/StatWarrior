package ui;

import dungeon.DungeonCell;
import main.Main;

import javax.swing.*;
import java.awt.*;

public final class GamePanel extends JPanel implements Runnable {

    private static final int TILE_SIZE = 32;
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int FPS = 60;

    public GamePanel() {
        setFocusable(true);
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        // only keyboard listens to keys
        if (Main.KEYBOARD instanceof java.awt.event.KeyListener keyListener) {
            addKeyListener(keyListener);
        }

        new Thread(this).start();
    }

    @Override
    public void run() {
        final long delay = 1000 / FPS;

        while (true) {

            // 🔥 update input FIRST
            Main.CONTROLLER.update();

            // update game
            Main.PLAYER.update();
            Main.CAMERA.update(Main.PLAYER);

            repaint();

            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {}
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