// ===== entity/EnemyBody.java =====
package entity;

import balance.Balance;
import dungeon.DungeonCell;
import main.Main;

public final class EnemyBody {

    private float x, y;
    private float vx, vy;
    private float bobTimer;
    private float bobOffset;

    private final int tileSize;
    private final int width, height;

    public EnemyBody(final float startTileX, final float startTileY, final int tileSize) {
        this.tileSize = tileSize;
        this.width    = tileSize;
        this.height   = tileSize;
        this.x        = startTileX * tileSize;
        this.y        = startTileY * tileSize;
    }

    public void applyPhysics(final boolean skipFriction) {
        if (!skipFriction) {
            vx *= Balance.ENEMY_FRICTION;
            vy *= Balance.ENEMY_FRICTION;
        }

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int gridWidth  = grid[0].length;
        final int gridHeight = grid.length;

        final float nextX = x + vx;
        final float nextY = y + vy;

        int tileLeft   = (int)(nextX / tileSize);
        int tileRight  = (int)((nextX + width - 1) / tileSize);
        int tileTop    = clamp((int)(y / tileSize), 0, gridHeight - 1);
        int tileBottom = clamp((int)((y + height - 1) / tileSize), 0, gridHeight - 1);

        if (tileLeft < 0 || tileRight >= gridWidth) {
            vx = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottom][tileLeft].isActive() && grid[tileBottom][tileRight].isActive()) {
            x = nextX;
        } else {
            vx = 0;
        }

        tileLeft        = clamp((int)(x / tileSize),              0, gridWidth  - 1);
        tileRight       = clamp((int)((x + width - 1) / tileSize), 0, gridWidth  - 1);
        tileTop         = clamp((int)(nextY / tileSize),           0, gridHeight - 1);
        int tileBottomY = clamp((int)((nextY + height - 1) / tileSize), 0, gridHeight - 1);

        if (tileTop < 0 || tileBottomY >= gridHeight) {
            vy = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottomY][tileLeft].isActive() && grid[tileBottomY][tileRight].isActive()) {
            y = nextY;
        } else {
            vy = 0;
        }
    }

    /**
     * Bounces both the enemy and the player away from each other.
     * Enemy is pushed toward melee range distance; player is nudged back.
     * Both respect wall collisions via applyPhysics next frame.
     */
    public void bounceFromPlayer(final float playerSpeed) {
        final float px = Main.PLAYER.centerX();
        final float py = Main.PLAYER.centerY();

        final float ex = centerX();
        final float ey = centerY();

        final float dx   = ex - px;
        final float dy   = ey - py;
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 1f) return;

        final float nx = dx / dist;
        final float ny = dy / dist;

        // Push enemy away from player
        setVelocity(nx * playerSpeed, ny * playerSpeed);

        // Nudge player away from enemy (opposite direction, lighter)
        final float playerNudge = playerSpeed * 0.4f;
        Main.PLAYER.nudge(-nx * playerNudge, -ny * playerNudge);
    }

    public void updateBob() {
        final float speed = (float) Math.sqrt(vx * vx + vy * vy);
        if (speed > BobConstants.SPEED_THRESHOLD) {
            bobTimer += speed * 0.016f;
            bobOffset = (float) Math.sin(bobTimer * BobConstants.FREQUENCY) * BobConstants.MAGNITUDE;
        } else {
            bobTimer  = 0f;
            bobOffset = 0f;
        }
    }

    public void clampSpeed(final float max) {
        if (vx >  max) vx =  max;
        if (vx < -max) vx = -max;
        if (vy >  max) vy =  max;
        if (vy < -max) vy = -max;
    }

    public void accelerate(final float ax, final float ay) { vx += ax; vy += ay; }
    public void setVelocity(final float newVx, final float newVy) { vx = newVx; vy = newVy; }
    public void stopX() { vx = 0; }
    public void stopY() { vy = 0; }
    public void stop()  { vx = 0; vy = 0; }

    public float centerX() { return x + width  / 2f; }
    public float centerY() { return y + height / 2f; }

    public int toTileX(final float worldX) { return (int)(worldX / tileSize); }
    public int toTileY(final float worldY) { return (int)(worldY / tileSize); }

    public int   getTileSize()  { return tileSize; }
    public float getX()         { return x; }
    public float getY()         { return y; }
    public float getVx()        { return vx; }
    public float getVy()        { return vy; }
    public float getBobOffset() { return bobOffset; }

    private static int clamp(final int val, final int min, final int max) {
        return Math.max(min, Math.min(val, max));
    }
}