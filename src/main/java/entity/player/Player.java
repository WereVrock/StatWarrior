// ===== entity/Player.java =====
package entity.player;

import balance.Balance;
import controls.InputController;
import dungeon.DungeonCell;
import entity.BobConstants;
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
    private final PlayerStats     stats;

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
        this.stats      = new PlayerStats();
    }

    public void update(final float tpf) {
        computeInputDir();

        final boolean sprinting = controller.isButtonPressed("LT") && stats.hasStamina()
                && (inputDirX != 0f || inputDirY != 0f);

        stats.update(tpf, sprinting);

        final float[] impulse = actions.update(tpf, inputDirX, inputDirY, vx, vy);

        if (actions.isDodging()) {
            vx = impulse[0];
            vy = impulse[1];
        } else if (!actions.isDodgeFreeze() && !actions.isMeleeActive()) {
            handleInput(sprinting);
        }

        applyPhysics();
        updateBob();
    }

    public void nudge(final float nx, final float ny) {
        vx += nx;
        vy += ny;
    }

    public void setVelocityDirect(final float nvx, final float nvy) {
        vx = nvx;
        vy = nvy;
    }

    public void stopVelocity() {
        vx = 0f;
        vy = 0f;
    }

    private void computeInputDir() {
        final boolean up    = controller.isUpPressed();
        final boolean down  = controller.isDownPressed();
        final boolean left  = controller.isLeftPressed();
        final boolean right = controller.isRightPressed();

        final float yaw      = Main.FIRST_PERSON_CAMERA.getYaw();
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

    private void handleInput(final boolean sprinting) {
        final float speedMult = sprinting ? Balance.PLAYER_SPRINT_SPEED_MULT : 1f;
        final float maxSpeed  = Balance.PLAYER_MAX_SPEED        * speedMult;
        final float accel     = Balance.PLAYER_ACCELERATION     * speedMult; // ← sprint accel scales too

        vx += inputDirX * accel;
        vy += inputDirY * accel;

        if (vx >  maxSpeed) vx =  maxSpeed;
        if (vx < -maxSpeed) vx = -maxSpeed;
        if (vy >  maxSpeed) vy =  maxSpeed;
        if (vy < -maxSpeed) vy = -maxSpeed;
    }

    private void applyPhysics() {
    vx *= Balance.PLAYER_FRICTION;
    vy *= Balance.PLAYER_FRICTION;
    final DungeonCell[][] grid = Main.DUNGEON.getGrid();
    final int gridWidth  = grid[0].length;
    final int gridHeight = grid.length;
    // =========================
    // ===== X AXIS ============
    // =========================
    float nextX = x + vx;
    if (vx > 0) { // moving right
        int tileRight  = (int)((nextX + 2*width) / tileSize);
        int tileTop    = (int)(y / tileSize);
        int tileBottom = (int)((y + height - 1) / tileSize);
        if (tileRight >= gridWidth ||
            !grid[tileTop][tileRight].isActive() ||
            !grid[tileBottom][tileRight].isActive()) {
            // snap: consistent with 2*width detection
            x = tileRight * tileSize - 2*width;
            vx = 0;
        } else {
            x = nextX;
        }
    } else if (vx < 0) { // moving left
        int tileLeft   = (int)(nextX / tileSize);
        int tileTop    = (int)(y / tileSize);
        int tileBottom = (int)((y + height - 1) / tileSize);
        if (tileLeft < 0 ||
            !grid[tileTop][tileLeft].isActive() ||
            !grid[tileBottom][tileLeft].isActive()) {
            // snap to wall (no multiplier on left)
            x = (tileLeft + 1) * tileSize;
            vx = 0;
        } else {
            x = nextX;
        }
    }
    // =========================
    // ===== Y AXIS ============
    // =========================
    float nextY = y + vy;
    if (vy > 0) { // moving down
        int tileBottom = (int)((nextY + 2*height) / tileSize);
        int tileLeft   = (int)(x / tileSize);
        int tileRight  = (int)((x + width - 1) / tileSize);
        if (tileBottom >= gridHeight ||
            !grid[tileBottom][tileLeft].isActive() ||
            !grid[tileBottom][tileRight].isActive()) {
            // snap: consistent with 2*height detection
            y = tileBottom * tileSize - 2*height;
            vy = 0;
        } else {
            y = nextY;
        }
    } else if (vy < 0) { // moving up
        int tileTop   = (int)(nextY / tileSize);
        int tileLeft  = (int)(x / tileSize);
        int tileRight = (int)((x + width - 1) / tileSize);
        if (tileTop < 0 ||
            !grid[tileTop][tileLeft].isActive() ||
            !grid[tileTop][tileRight].isActive()) {
            // snap to wall (no multiplier on up)
            y = (tileTop + 1) * tileSize;
            vy = 0;
        } else {
            y = nextY;
        }
    }
}
    private void updateBob() {
        if (actions.isDodging() || actions.isMeleeActive()) {
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
        return actions.tryParry(attackerX, attackerY);
    }

    public boolean isInvincible()     { return actions.isInvincible();   }
    public float   centerX()          { return x + width  / 2f;          }
    public float   centerY()          { return y + height / 2f;          }
    public float   getX()             { return x;                        }
    public float   getY()             { return y;                        }
    public float   getBobOffset()     { return bobOffset;                }
    public PlayerActions getActions() { return actions;                  }
    public PlayerStats   getStats()   { return stats;                    }
}