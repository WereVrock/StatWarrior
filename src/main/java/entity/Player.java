package entity;

import balance.Balance;
import controls.InputController;
import dungeon.DungeonCell;
import main.Main;

import java.awt.Color;
import java.awt.Graphics;

public final class Player {

    private static final Color COLOR = Color.RED;

    private float x, y;
    private float vx, vy;
    private final int tileSize;
    private final int width, height;
    private final InputController controller;

    public Player(final int startX, final int startY, final int tileSize, InputController controller) {
        this.tileSize = tileSize;
        this.width = tileSize;
        this.height = tileSize;
        this.x = startX * tileSize;
        this.y = startY * tileSize;
        this.vx = 0;
        this.vy = 0;
        this.controller = controller;
    }

    public void update() {
        boolean up = controller.isUpPressed();
        boolean down = controller.isDownPressed();
        boolean left = controller.isLeftPressed();
        boolean right = controller.isRightPressed();

        if (up) vy -= Balance.PLAYER_ACCELERATION;
        if (down) vy += Balance.PLAYER_ACCELERATION;
        if (left) vx -= Balance.PLAYER_ACCELERATION;
        if (right) vx += Balance.PLAYER_ACCELERATION;

        if (vx > Balance.PLAYER_MAX_SPEED) vx = Balance.PLAYER_MAX_SPEED;
        if (vx < -Balance.PLAYER_MAX_SPEED) vx = -Balance.PLAYER_MAX_SPEED;
        if (vy > Balance.PLAYER_MAX_SPEED) vy = Balance.PLAYER_MAX_SPEED;
        if (vy < -Balance.PLAYER_MAX_SPEED) vy = -Balance.PLAYER_MAX_SPEED;

        vx *= Balance.PLAYER_FRICTION;
        vy *= Balance.PLAYER_FRICTION;

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
        int tileBottomY = (int)((nextY + height - 1) / tileSize);

        if (tileTop < 0 || tileBottomY >= gridHeight) {
            vy = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottomY][tileLeft].isActive() && grid[tileBottomY][tileRight].isActive()) {
            y = nextY;
        } else {
            vy = 0;
        }
    }

    public void render(Graphics g) {
        final int offsetX = Main.CAMERA.getOffsetX();
        final int offsetY = Main.CAMERA.getOffsetY();

        g.setColor(COLOR);
        g.fillOval(
                Math.round(x) - offsetX,
                Math.round(y) - offsetY,
                width,
                height
        );
    }

    public float getX() { return x; }
    public float getY() { return y; }
}