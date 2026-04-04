package entity;

import balance.Balance;
import dungeon.DungeonCell;
import main.Main;
import pathfinding.PathFinder;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Enemy {

    private static final Random RANDOM = new Random();

    private float x, y;
    private float vx, vy;

    private EnemyState state;

    private float wanderTimer;
    private float wanderDirX, wanderDirY;

    private float lostPlayerTimer;

    private float lastKnownX, lastKnownY;
    private boolean hasLastKnown;

    private List<int[]> currentPath;
    private int pathIndex;

    private final int tileSize;
    private final int width, height;

    public Enemy(final float startX, final float startY, final int tileSize) {
        this.tileSize   = tileSize;
        this.width      = tileSize;
        this.height     = tileSize;
        this.x          = startX * tileSize;
        this.y          = startY * tileSize;
        this.vx         = 0;
        this.vy         = 0;
        this.state      = EnemyState.WANDER;
        this.currentPath = Collections.emptyList();
        this.pathIndex   = 0;
        this.hasLastKnown = false;
        pickNewWanderDir();
    }

    public void update(final float tpf) {
        switch (state) {
            case WANDER -> updateWander(tpf);
            case CHASE  -> updateChase(tpf);
            case ATTACK -> updateAttack(tpf);
        }
        applyPhysics();
    }

    // =========================================================
    //  WANDER
    // =========================================================

    private void updateWander(final float tpf) {
        if (canDetectPlayer()) {
            enterChase();
            return;
        }

        wanderTimer -= tpf;
        if (wanderTimer <= 0f) {
            pickNewWanderDir();
        }

        vx += wanderDirX * Balance.ENEMY_ACCELERATION;
        vy += wanderDirY * Balance.ENEMY_ACCELERATION;

        clampSpeed(Balance.ENEMY_WANDER_SPEED);
    }

    private void pickNewWanderDir() {
        wanderTimer = Balance.ENEMY_WANDER_DIR_CHANGE_INTERVAL
                + RANDOM.nextFloat() * Balance.ENEMY_WANDER_DIR_CHANGE_VARIANCE;

        final float angle = RANDOM.nextFloat() * (float)(Math.PI * 2);
        wanderDirX = (float) Math.cos(angle);
        wanderDirY = (float) Math.sin(angle);
    }

    // =========================================================
    //  CHASE
    // =========================================================

    private void enterChase() {
        state = EnemyState.CHASE;
        lostPlayerTimer = 0f;
        updateLastKnown();
        recalculatePath(toTileX(Main.PLAYER.getX()), toTileY(Main.PLAYER.getY()));
    }

    private void updateChase(final float tpf) {
        if (canDetectPlayer()) {
            lostPlayerTimer = 0f;
            updateLastKnown();

            if (isWithinAttackRange()) {
                state = EnemyState.ATTACK;
                currentPath = Collections.emptyList();
                return;
            }

            recalculatePath(toTileX(Main.PLAYER.getX()), toTileY(Main.PLAYER.getY()));
        } else {
            lostPlayerTimer += tpf;

            if (lostPlayerTimer >= Balance.ENEMY_LOST_PLAYER_TIMEOUT) {
                enterWander();
                return;
            }

            // Walk to last known position
            if (hasLastKnown) {
                final int lkTileX = toTileX(lastKnownX);
                final int lkTileY = toTileY(lastKnownY);
                final int myTileX = toTileX(x);
                final int myTileY = toTileY(y);

                if (myTileX == lkTileX && myTileY == lkTileY) {
                    // Arrived at last known — keep waiting for timeout
                    vx = 0;
                    vy = 0;
                    return;
                }

                if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
                    recalculatePath(lkTileX, lkTileY);
                }
            }
        }

        followPath();
        clampSpeed(Balance.ENEMY_CHASE_SPEED);
    }

    private void enterWander() {
        state = EnemyState.WANDER;
        hasLastKnown = false;
        currentPath = Collections.emptyList();
        pathIndex = 0;
        pickNewWanderDir();
    }

    private void updateLastKnown() {
        lastKnownX = Main.PLAYER.getX();
        lastKnownY = Main.PLAYER.getY();
        hasLastKnown = true;
    }

    // =========================================================
    //  ATTACK
    // =========================================================

    private void updateAttack(final float tpf) {
        // Placeholder — no movement during attack
        vx = 0;
        vy = 0;

        if (!isWithinAttackRange()) {
            enterChase();
            return;
        }

        if (!canDetectPlayer()) {
            lostPlayerTimer += tpf;
            if (lostPlayerTimer >= Balance.ENEMY_LOST_PLAYER_TIMEOUT) {
                enterWander();
            }
        } else {
            lostPlayerTimer = 0f;
            updateLastKnown();
            // TODO: perform actual attack logic here
        }
    }

    // =========================================================
    //  PATHFINDING
    // =========================================================

    private void recalculatePath(final int goalTileX, final int goalTileY) {
        final int myTileX = toTileX(x);
        final int myTileY = toTileY(y);
        final List<int[]> path = PathFinder.findPath(myTileX, myTileY, goalTileX, goalTileY);
        currentPath = path;
        pathIndex = 0;
    }

    private void followPath() {
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            return;
        }

        final int[] target = currentPath.get(pathIndex);
        final float targetWorldX = target[0] * tileSize + tileSize / 2f;
        final float targetWorldY = target[1] * tileSize + tileSize / 2f;

        final float dx = targetWorldX - (x + width / 2f);
        final float dy = targetWorldY - (y + height / 2f);
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < Balance.ENEMY_WAYPOINT_REACH_DIST) {
            pathIndex++;
            return;
        }

        vx += (dx / dist) * Balance.ENEMY_ACCELERATION;
        vy += (dy / dist) * Balance.ENEMY_ACCELERATION;
    }

    // =========================================================
    //  PHYSICS & COLLISION
    // =========================================================

    private void applyPhysics() {
        vx *= Balance.ENEMY_FRICTION;
        vy *= Balance.ENEMY_FRICTION;

        final DungeonCell[][] grid = Main.DUNGEON.getGrid();
        final int gridWidth  = grid[0].length;
        final int gridHeight = grid.length;

        float nextX = x + vx;
        float nextY = y + vy;

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
            wanderDirX = -wanderDirX;
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
            wanderDirY = -wanderDirY;
        }
    }

    private void clampSpeed(final float max) {
        if (vx >  max) vx =  max;
        if (vx < -max) vx = -max;
        if (vy >  max) vy =  max;
        if (vy < -max) vy = -max;
    }

    // =========================================================
    //  DETECTION
    // =========================================================

    private boolean canDetectPlayer() {
        final float px = Main.PLAYER.getX() + tileSize / 2f;
        final float py = Main.PLAYER.getY() + tileSize / 2f;
        final float ex = x + width / 2f;
        final float ey = y + height / 2f;

        final float dx = px - ex;
        final float dy = py - ey;
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_DETECT_RANGE * tileSize;

        return distSq <= range * range;
    }

    private boolean isWithinAttackRange() {
        final float px = Main.PLAYER.getX() + tileSize / 2f;
        final float py = Main.PLAYER.getY() + tileSize / 2f;
        final float ex = x + width / 2f;
        final float ey = y + height / 2f;

        final float dx = px - ex;
        final float dy = py - ey;
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_ATTACK_RANGE * tileSize;

        return distSq <= range * range;
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private int toTileX(final float worldX) { return (int)(worldX / tileSize); }
    private int toTileY(final float worldY) { return (int)(worldY / tileSize); }

    public float getX()          { return x; }
    public float getY()          { return y; }
    public EnemyState getState() { return state; }
}