package entity;

import dungeon.DungeonCell;
import main.Main;

import java.awt.Color;
import java.awt.Graphics;

public final class Player {

    private static final Color COLOR = Color.RED;
    private static final float ACCELERATION = 0.2f; // pixels per frame per input
    private static final float MAX_SPEED = 4f;       // max pixels per frame
    private static final float FRICTION = 0.85f;    // slows player naturally

    private float x; // continuous position in pixels
    private float y;

    private float vx;
    private float vy;

    private final int tileSize;

    public Player(final int startX, final int startY, final int tileSize) {
        this.tileSize = tileSize;
        this.x = startX * tileSize;
        this.y = startY * tileSize;
        this.vx = 0;
        this.vy = 0;
    }

    // Called every frame
    public void update(boolean up, boolean down, boolean left, boolean right) {
        // Input acceleration
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

        // Predict new position
        float nextX = x + vx;
        float nextY = y + vy;

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        int gridWidth = grid[0].length;
        int gridHeight = grid.length;

        // Collision check on X
        int tileLeft = (int)(nextX / tileSize);
        int tileRight = (int)((nextX + tileSize - 1) / tileSize);
        int tileTop = (int)(y / tileSize);
        int tileBottom = (int)((y + tileSize - 1) / tileSize);

        if (tileLeft >= 0 && tileRight < gridWidth &&
            tileTop >= 0 && tileBottom < gridHeight) {
            if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                grid[tileBottom][tileLeft].isActive() && grid[tileBottom][tileRight].isActive()) {
                x = nextX;
            } else {
                vx = 0; // stop horizontal movement on collision
            }
        }

        // Collision check on Y
        tileLeft = (int)(x / tileSize);
        tileRight = (int)((x + tileSize - 1) / tileSize);
        tileTop = (int)(nextY / tileSize);
        tileBottom = (int)((nextY + tileSize - 1) / tileSize);

        if (tileLeft >= 0 && tileRight < gridWidth &&
            tileTop >= 0 && tileBottom < gridHeight) {
            if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                grid[tileBottom][tileLeft].isActive() && grid[tileBottom][tileRight].isActive()) {
                y = nextY;
            } else {
                vy = 0; // stop vertical movement on collision
            }
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void render(Graphics g, int tileSize, int offsetX, int offsetY) {
        g.setColor(COLOR);
        g.fillOval(
            Math.round(x) - offsetX,
            Math.round(y) - offsetY,
            tileSize,
            tileSize
        );
    }
}