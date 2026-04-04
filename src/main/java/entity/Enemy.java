package entity;

import balance.Balance;
import dungeon.DungeonCell;
import main.Main;
import pathfinding.PathFinder;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Enemy {

    private enum AttackPhase { TELEGRAPH, LUNGE, COOLDOWN }

    private static final Random RANDOM = new Random();

    private float x, y;
    private float vx, vy;

    private EnemyState state;
    private AttackPhase attackPhase;
    private float attackTimer;
    private float cooldownTimer;

    private float wanderTimer;
    private float wanderDirX, wanderDirY;

    private float lostPlayerTimer;

    private float lastKnownX, lastKnownY;
    private boolean hasLastKnown;

    private List<int[]> currentPath;
    private int pathIndex;

    private float shakeOffset;
    private float bobTimer;
    private float bobOffset;

    private final int tileSize;
    private final int width, height;
    private final String id;

    public Enemy(final float startX, final float startY, final int tileSize, final String id) {
        this.tileSize     = tileSize;
        this.width        = tileSize;
        this.height       = tileSize;
        this.x            = startX * tileSize;
        this.y            = startY * tileSize;
        this.id           = id;
        this.state        = EnemyState.WANDER;
        this.attackPhase  = AttackPhase.TELEGRAPH;
        this.currentPath  = Collections.emptyList();
        this.pathIndex    = 0;
        this.hasLastKnown = false;
        pickNewWanderDir();
        log("Spawned at tile (" + (int)startX + ", " + (int)startY + ")");
    }

    public void update(final float tpf) {
        switch (state) {
            case WANDER -> updateWander(tpf);
            case CHASE  -> updateChase(tpf);
            case ATTACK -> updateAttack(tpf);
        }
        applyPhysics();
        updateBob();
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
        log("Wander: new direction angle=" + String.format("%.2f", Math.toDegrees(angle)) + " deg");
    }

    // =========================================================
    //  CHASE
    // =========================================================

    private void enterChase() {
        state = EnemyState.CHASE;
        lostPlayerTimer = 0f;
        updateLastKnown();
        recalculatePath(toTileX(Main.PLAYER.getX()), toTileY(Main.PLAYER.getY()));
        log("State -> CHASE");
    }

    private void updateChase(final float tpf) {
        if (canDetectPlayer()) {
            lostPlayerTimer = 0f;
            updateLastKnown();

            if (isWithinAttackRange() && currentPath.isEmpty()) {
                enterAttack();
                return;
            }

            recalculatePath(toTileX(Main.PLAYER.getX()), toTileY(Main.PLAYER.getY()));

        } else {
            lostPlayerTimer += tpf;
            log("Chase: lost player, timeout in " + String.format("%.1f", Balance.ENEMY_LOST_PLAYER_TIMEOUT - lostPlayerTimer) + "s");

            if (lostPlayerTimer >= Balance.ENEMY_LOST_PLAYER_TIMEOUT) {
                enterWander();
                return;
            }

            if (hasLastKnown) {
                final int lkTileX = toTileX(lastKnownX);
                final int lkTileY = toTileY(lastKnownY);
                final int myTileX = toTileX(centerX());
                final int myTileY = toTileY(centerY());

                if (myTileX == lkTileX && myTileY == lkTileY) {
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
        log("State -> WANDER (lost player)");
    }

    private void updateLastKnown() {
        lastKnownX = Main.PLAYER.getX();
        lastKnownY = Main.PLAYER.getY();
        hasLastKnown = true;
    }

    // =========================================================
    //  ATTACK
    // =========================================================

    private void enterAttack() {
        state = EnemyState.ATTACK;
        attackPhase = AttackPhase.TELEGRAPH;
        attackTimer = Balance.ENEMY_ATTACK_TELEGRAPH_DURATION;
        vx = 0;
        vy = 0;
        log("State -> ATTACK | Phase -> TELEGRAPH");
    }

    private void updateAttack(final float tpf) {
        switch (attackPhase) {
            case TELEGRAPH -> updateTelegraph(tpf);
            case LUNGE     -> updateLunge(tpf);
            case COOLDOWN  -> updateCooldown(tpf);
        }
    }

    private void updateTelegraph(final float tpf) {
        attackTimer -= tpf;
        shakeOffset = (float)(Math.sin(attackTimer * 80f) * Balance.ENEMY_ATTACK_SHAKE_MAGNITUDE);

        if (!canDetectPlayer()) {
            shakeOffset = 0f;
            enterWander();
            return;
        }

        if (attackTimer <= 0f) {
            shakeOffset = 0f;
            launchLunge();
        }
    }

    private void launchLunge() {
        final float dx   = (Main.PLAYER.getX() + tileSize / 2f) - centerX();
        final float dy   = (Main.PLAYER.getY() + tileSize / 2f) - centerY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0f) {
            vx = (dx / dist) * Balance.ENEMY_ATTACK_LUNGE_SPEED;
            vy = (dy / dist) * Balance.ENEMY_ATTACK_LUNGE_SPEED;
        }

        attackPhase = AttackPhase.LUNGE;
        attackTimer = Balance.ENEMY_ATTACK_LUNGE_DURATION;
        log("Attack | Phase -> LUNGE");
    }

    private void updateLunge(final float tpf) {
        attackTimer -= tpf;

        if (attackTimer <= 0f) {
            vx = 0;
            vy = 0;
            attackPhase = AttackPhase.COOLDOWN;
            cooldownTimer = Balance.ENEMY_ATTACK_COOLDOWN;
            log("Attack | Phase -> COOLDOWN");
        }
    }

    private void updateCooldown(final float tpf) {
        cooldownTimer -= tpf;

        if (cooldownTimer <= 0f) {
            if (canDetectPlayer() && isWithinAttackRange()) {
                enterAttack();
            } else if (canDetectPlayer()) {
                enterChase();
            } else {
                enterWander();
            }
        }
    }

    // =========================================================
    //  PATHFINDING
    // =========================================================

    private void recalculatePath(final int goalTileX, final int goalTileY) {
        final int myTileX = toTileX(centerX());
        final int myTileY = toTileY(centerY());

        if (myTileX == goalTileX && myTileY == goalTileY) {
            currentPath = Collections.emptyList();
            pathIndex = 0;
            return;
        }

        final List<int[]> path = PathFinder.findPath(myTileX, myTileY, goalTileX, goalTileY);

        if (!path.isEmpty()) {
            currentPath = path;
            pathIndex = 1;
            log("Chase: path recalculated, " + path.size() + " steps to (" + goalTileX + ", " + goalTileY + ")");
        }
    }

    private void followPath() {
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            return;
        }

        final int[]  target    = currentPath.get(pathIndex);
        final float  targetCX  = target[0] * tileSize + tileSize / 2f;
        final float  targetCY  = target[1] * tileSize + tileSize / 2f;
        final float  dx        = targetCX - centerX();
        final float  dy        = targetCY - centerY();
        final float  dist      = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < Balance.ENEMY_WAYPOINT_REACH_DIST) {
            pathIndex++;
            if (pathIndex < currentPath.size()) {
                final int[] next = currentPath.get(pathIndex);
                log("Chase: waypoint reached, next=(" + next[0] + ", " + next[1] + ")");
            }
            return;
        }

        vx += (dx / dist) * Balance.ENEMY_ACCELERATION;
        vy += (dy / dist) * Balance.ENEMY_ACCELERATION;
    }

    // =========================================================
    //  PHYSICS & COLLISION
    // =========================================================

    private void applyPhysics() {
        if (!(state == EnemyState.ATTACK && attackPhase == AttackPhase.LUNGE)) {
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
        int tileTop    = (int)(y / tileSize);
        int tileBottom = (int)((y + height - 1) / tileSize);

        tileTop    = Math.max(0, Math.min(tileTop,    gridHeight - 1));
        tileBottom = Math.max(0, Math.min(tileBottom, gridHeight - 1));

        if (tileLeft < 0 || tileRight >= gridWidth) {
            vx = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottom][tileLeft].isActive() && grid[tileBottom][tileRight].isActive()) {
            x = nextX;
        } else {
            vx = 0;
            if (state == EnemyState.WANDER) wanderDirX = -wanderDirX;
        }

        tileLeft        = (int)(x / tileSize);
        tileRight       = (int)((x + width - 1) / tileSize);
        tileTop         = (int)(nextY / tileSize);
        int tileBottomY = (int)((nextY + height - 1) / tileSize);

        tileTop     = Math.max(0, Math.min(tileTop,     gridHeight - 1));
        tileBottomY = Math.max(0, Math.min(tileBottomY, gridHeight - 1));
        tileLeft    = Math.max(0, Math.min(tileLeft,    gridWidth  - 1));
        tileRight   = Math.max(0, Math.min(tileRight,   gridWidth  - 1));

        if (tileTop < 0 || tileBottomY >= gridHeight) {
            vy = 0;
        } else if (grid[tileTop][tileLeft].isActive() && grid[tileTop][tileRight].isActive() &&
                   grid[tileBottomY][tileLeft].isActive() && grid[tileBottomY][tileRight].isActive()) {
            y = nextY;
        } else {
            vy = 0;
            if (state == EnemyState.WANDER) wanderDirY = -wanderDirY;
        }
    }

    private void clampSpeed(final float max) {
        if (vx >  max) vx =  max;
        if (vx < -max) vx = -max;
        if (vy >  max) vy =  max;
        if (vy < -max) vy = -max;
    }

    // =========================================================
    //  BOB
    // =========================================================

    private void updateBob() {
        final float speed = (float) Math.sqrt(vx * vx + vy * vy);
        if (speed > Balance.BOB_SPEED_THRESHOLD) {
            bobTimer += speed * 0.016f;
            bobOffset = (float) Math.sin(bobTimer * Balance.BOB_FREQUENCY) * Balance.BOB_MAGNITUDE;
        } else {
            bobTimer  = 0f;
            bobOffset = 0f;
        }
    }

    // =========================================================
    //  DETECTION
    // =========================================================

    private boolean canDetectPlayer() {
        final float dx     = (Main.PLAYER.getX() + tileSize / 2f) - centerX();
        final float dy     = (Main.PLAYER.getY() + tileSize / 2f) - centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_DETECT_RANGE * tileSize;
        return distSq <= range * range;
    }

    private boolean isWithinAttackRange() {
        final float dx     = (Main.PLAYER.getX() + tileSize / 2f) - centerX();
        final float dy     = (Main.PLAYER.getY() + tileSize / 2f) - centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_ATTACK_RANGE * tileSize;
        return distSq <= range * range;
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private float centerX() { return x + width  / 2f; }
    private float centerY() { return y + height / 2f; }

    private int toTileX(final float worldX) { return (int)(worldX / tileSize); }
    private int toTileY(final float worldY) { return (int)(worldY / tileSize); }

    private void log(final String msg) {
        System.out.println("[" + id + "] " + msg);
    }

    public float getX()            { return x; }
    public float getY()            { return y; }
    public float getShakeOffset()  { return shakeOffset; }
    public float getBobOffset()    { return bobOffset; }
    public EnemyState getState()   { return state; }
}