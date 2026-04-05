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
    private final PlayerActions   actions;

    private float inputDirX = 0f;
    private float inputDirY = 0f;

    public Player(final int startX, final int startY, final int tileSize,
                  final InputController controller) {
        this.tileSize   = tileSize;
        this.width      = tileSize;
        this.height     = tileSize;
        this.x          = startX * tileSize;
        this.y          = startY * tileSize;
        this.controller = controller;
        this.actions    = new PlayerActions(controller);
    }

    public void update() {
        computeInputDir();

        final float[] impulse = actions.update(0.016f, inputDirX, inputDirY, vx, vy);

        if (actions.isDodging()) {
            vx = impulse[0];
            vy = impulse[1];
        } else if (!actions.isDodgeFreeze()) {
            handleInput();
        }

        applyPhysics();
        updateBob();
    }

    /** Applies an external velocity nudge — used by bounce logic. Physics resolves wall safety. */
    public void nudge(final float nx, final float ny) {
        vx += nx;
        vy += ny;
    }

    private void computeInputDir() {
        final boolean up    = controller.isUpPressed();
        final boolean down  = controller.isDownPressed();
        final boolean left  = controller.isLeftPressed();
        final boolean right = controller.isRightPressed();

        final float yaw      = Main.THIRD_PERSON_CAMERA.getYaw();
        final float forwardX =  (float) Math.sin(yaw);
        final float forwardY =  (float) Math.cos(yaw);
        final float rightX   = -(float) Math.cos(yaw);
        final float rightY   =  (float) Math.sin(yaw);

        float mx = 0, my = 0;
        if (up)    { mx += forwardX; my += forwardY; }
        if (down)  { mx -= forwardX; my -= forwardY; }
        if (left)  { mx -= rightX;   my -= rightY;   }
        if (right) { mx += rightX;   my += rightY;   }

        final float len = (float) Math.sqrt(mx * mx + my * my);
        if (len > 0.001f) {
            inputDirX = mx / len;
            inputDirY = my / len;
        } else {
            inputDirX = 0f;
            inputDirY = 0f;
        }
    }

    private void handleInput() {
        vx += inputDirX * Balance.PLAYER_ACCELERATION;
        vy += inputDirY * Balance.PLAYER_ACCELERATION;

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
        if (actions.isDodging()) {
            bobOffset = 0f;
            return;
        }
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
        g.fillOval(Math.round(x) - offsetX, Math.round(y) - offsetY, width, height);
    }

    public boolean tryParry(final float attackerX, final float attackerY) {
        return actions.tryParry(attackerX, attackerY, centerX(), centerY());
    }

    public boolean isInvincible()   { return actions.isInvincible(); }
    public float   centerX()        { return x + width  / 2f; }
    public float   centerY()        { return y + height / 2f; }
    public float   getX()           { return x; }
    public float   getY()           { return y; }
    public float   getBobOffset()   { return bobOffset; }
    public PlayerActions getActions() { return actions; }
}