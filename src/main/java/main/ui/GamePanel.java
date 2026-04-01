package ui;

import dungeon.DungeonCell;
import entity.Player;
import main.Main;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public final class GamePanel extends JPanel implements Runnable {

    private static final int TILE_SIZE = 32;
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int FPS = 60;

    private final Player player;
    private final Camera camera;

    private final Set<Integer> keys = new HashSet<>();

    public GamePanel() {
        setFocusable(true);
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        player = new Player(grid[0].length / 2, grid.length / 2, TILE_SIZE);
        camera = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { keys.add(e.getKeyCode()); }
            @Override
            public void keyReleased(KeyEvent e) { keys.remove(e.getKeyCode()); }
        });

        new Thread(this).start();
    }

    @Override
    public void run() {
        long delay = 1000 / FPS;
        while (true) {
            boolean up = keys.contains(KeyEvent.VK_W);
            boolean down = keys.contains(KeyEvent.VK_S);
            boolean left = keys.contains(KeyEvent.VK_A);
            boolean right = keys.contains(KeyEvent.VK_D);

            player.update(up, down, left, right);
            camera.update(player);

            repaint();

            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
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