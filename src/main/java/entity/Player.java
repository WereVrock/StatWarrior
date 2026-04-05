// ===== entity/Player.java =====
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
    private float bobTimer;
    private float bobOffset;

    private final int tileSize;
    private final int width, height;
    private final InputController controller;

    public Player(final int startX, final int startY, final int tileSize, final InputController controller) {
        this.tileSize   = tileSize;
        this.width      = tileSize;
        this.height     = tileSize;
        this.x          = startX * tileSize;
        this.y          = startY * tileSize;
        this.controller = controller;
    }

    public void update() {
        handleInput();
        applyPhysics();
        updateBob();
    }

    private void handleInput() {
        final boolean up    = controller.isUpPressed();
        final boolean down  = controller.isDownPressed();
        final boolean left  = controller.isLeftPressed();
        final boolean right = controller.isRightPressed();

        final float yaw      = Main.THIRD_PERSON_CAMERA.getYaw();
        final float forwardX =  (float) Math.sin(yaw);
        final float forwardY =  (float) Math.cos(yaw);
        final float rightX   = -(float) Math.cos(yaw);
        final float rightY   =  (float) Math.sin(yaw);

        float moveX = 0, moveY = 0;
        if (up)    { moveX += forwardX; moveY += forwardY; }
        if (down)  { moveX -= forwardX; moveY -= forwardY; }
        if (left)  { moveX -= rightX;   moveY -= rightY;   }
        if (right) { moveX += rightX;   moveY += rightY;   }

        if (moveX != 0 || moveY != 0) {
            final float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= len;
            moveY /= len;
        }

        vx += moveX * Balance.PLAYER_ACCELERATION;
        vy += moveY * Balance.PLAYER_ACCELERATION;

        if (vx >  Balance.PLAYER_MAX_SPEED) vx =  Balance.PLAYER_MAX_SPEED;
        if (vx < -Balance.PLAYER_MAX_SPEED) vx = -Balance.PLAYER_MAX_SPEED;
        if (vy >  Balance.PLAYER_MAX_SPEED) vy =  Balance.PLAYER_MAX_SPEED;
        if (vy < -Balance.PLAYER_MAX_SPEED) vy = -Balance.PLAYER_MAX_SPEED;
    }

    private void applyPhysics() {
        vx *= Balance.PLAYER_FRICTION;
        vy *= Balance.PLAYER_FRICTION;

        final float nextX = x + vx;
        final float nextY = y + vy;

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int gridWidth  = grid[0].length;
        final int gridHeight = grid.length;

        int tileLeft   = (int)(nextX / tileSize);
        int tileRight  = (int)((nextX + width - 1) / tileSize);
        int tileTop    = (int)(y / tileSize);
        int tileBottom = (int)((y + height - 1) / tileSize);

        if (tileLeft < 0 || tileRight >= gridWidth) {
            vx = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottom][tileLeft].isActive() && grid[tileBottom][tileRight].isActive()) {
            x = nextX;
        } else {
            vx = 0;
        }

        tileLeft        = (int)(x / tileSize);
        tileRight       = (int)((x + width - 1) / tileSize);
        tileTop         = (int)(nextY / tileSize);
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

    private void updateBob() {
        final float speed = (float) Math.sqrt(vx * vx + vy * vy);
        if (speed > BobConstants.SPEED_THRESHOLD) {
            bobTimer += speed * 0.016f;
            bobOffset = (float) Math.sin(bobTimer * BobConstants.FREQUENCY) * BobConstants.MAGNITUDE;
        } else {
            bobTimer  = 0f;
            bobOffset = 0f;
        }
    }

    public void render(final Graphics g) {
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

    public float getX()         { return x; }
    public float getY()         { return y; }
    public float getBobOffset() { return bobOffset; }
}