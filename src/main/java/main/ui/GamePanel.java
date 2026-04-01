package ui;

import dungeon.DungeonCell;
import entity.Player;
import main.Main;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class GamePanel extends JPanel {

    private static final int TILE_SIZE = 32;
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

    private final Player player;
    private final Camera camera;

    public GamePanel() {
        setFocusable(true);
        setDoubleBuffered(true);

        player = new Player(SCREEN_WIDTH / TILE_SIZE / 2, SCREEN_HEIGHT / TILE_SIZE / 2);
        camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> player.move(0, -1);
                    case KeyEvent.VK_S -> player.move(0, 1);
                    case KeyEvent.VK_A -> player.move(-1, 0);
                    case KeyEvent.VK_D -> player.move(1, 0);
                }
                camera.update(player);
                repaint();
            }
        });

        camera.update(player);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int offsetX = camera.getOffsetX();
        final int offsetY = camera.getOffsetY();

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

        player.render(g, TILE_SIZE, offsetX, offsetY);
    }
}