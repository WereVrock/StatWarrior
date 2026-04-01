package entity;

import dungeon.DungeonCell;
import main.Main;

import java.awt.Color;
import java.awt.Graphics;

public final class Player {

    private static final Color COLOR = Color.RED;
    private static final float ACCELERATION = 0.2f;
    private static final float MAX_SPEED = 4f;
    private static final float FRICTION = 0.85f;

    private float x, y; // continuous position in pixels
    private float vx, vy;
    private final int tileSize;
    private final int width, height; // player size in pixels

    public Player(final int startX, final int startY, final int tileSize) {
        this.tileSize = tileSize;
        this.width = tileSize;  // can change to different size if needed
        this.height = tileSize;

        this.x = startX * tileSize;
        this.y = startY * tileSize;
        this.vx = 0;
        this.vy = 0;
    }

    // Update player movement
    public void update(boolean up, boolean down, boolean left, boolean right) {
        if (up) vy -= ACCELERATION;
        if (down) vy += ACCELERATION;
        if (left) vx -= ACCELERATION;
        if (right) vx += ACCELERATION;

        // Clamp velocity
        if (vx > MAX_SPEED) vx = MAX_SPEED;
        if (vx < -MAX_SPEED) vx = -MAX_SPEED;
        if (vy > MAX_SPEED) vy = MAX_SPEED;
        if (vy < -MAX_SPEED) vy = -MAX_SPEED;

        // Apply friction
        vx *= FRICTION;
        vy *= FRICTION;

        // Predict new positions
        float nextX = x + vx;
        float nextY = y + vy;

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        int gridWidth = grid[0].length;
        int gridHeight = grid.length;

        // --- X collision ---
        int tileLeft = (int)(nextX / tileSize);
        int tileRight = (int)((nextX + width - 1) / tileSize);
        int tileTop = (int)(y / tileSize);
        int tileBottom = (int)((y + height - 1) / tileSize);

        if (tileLeft < 0 || tileRight >= gridWidth) {
            vx = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottom][tileLeft].isActive() && grid[tileBottom][tileRight].isActive()) {
            x = nextX;
        } else {
            vx = 0;
        }

        // --- Y collision ---
        tileLeft = (int)(x / tileSize);
        tileRight = (int)((x + width - 1) / tileSize);
        tileTop = (int)(nextY / tileSize);
        tileBottom = (int)((nextY + height - 1) / tileSize);

        if (tileTop < 0 || tileBottom >= gridHeight) {
            vy = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottom][tileLeft].isActive() && grid[tileBottom][tileRight].isActive()) {
            y = nextY;
        } else {
            vy = 0;
        }
    }

    // Continuous coordinates
    public float getX() { return x; }
    public float getY() { return y; }

    // Tile indices (center of player)
    public int getTileX() { return (int)((x + width / 2f) / tileSize); }
    public int getTileY() { return (int)((y + height / 2f) / tileSize); }

    // Render player
    public void render(Graphics g, int tileSize, int offsetX, int offsetY) {
        g.setColor(COLOR);
        g.fillOval(
            Math.round(x) - offsetX,
            Math.round(y) - offsetY,
            width,
            height
        );
    }
}