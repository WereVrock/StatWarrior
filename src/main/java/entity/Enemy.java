package entity;

import balance.Balance;
import main.Main;

import java.util.Random;

public final class Enemy {

    private enum AttackPhase { TELEGRAPH, LUNGE, COOLDOWN }

    private static final Random RANDOM = new Random();

    private EnemyState  state;
    private AttackPhase attackPhase;
    private float       attackTimer;
    private float       cooldownTimer;

    private float   wanderTimer;
    private float   wanderDirX, wanderDirY;
    private float   lostPlayerTimer;
    private float   lastKnownX, lastKnownY;
    private boolean hasLastKnown;
    private float   shakeOffset;

    private final EnemyBody     body;
    private final EnemyDetector detector;
    private final EnemySteer    steer;
    private final String        id;

    public Enemy(final float startX, final float startY, final int tileSize, final String id) {
        this.id          = id;
        this.body        = new EnemyBody(startX, startY, tileSize);
        this.detector    = new EnemyDetector(body);
        this.steer       = new EnemySteer(body);
        this.state       = EnemyState.WANDER;
        this.attackPhase = AttackPhase.TELEGRAPH;
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
        final boolean lunging = state == EnemyState.ATTACK && attackPhase == AttackPhase.LUNGE;
        body.applyPhysics(lunging);
        body.updateBob();
    }

    // =========================================================
    //  WANDER
    // =========================================================

    private void updateWander(final float tpf) {
        if (detector.canDetectPlayer()) {
            enterChase();
            return;
        }

        wanderTimer -= tpf;
        if (wanderTimer <= 0f) {
            pickNewWanderDir();
        }

        body.accelerate(
                wanderDirX * Balance.ENEMY_ACCELERATION,
                wanderDirY * Balance.ENEMY_ACCELERATION
        );
        steer.applyWallRepulsion();
        body.clampSpeed(Balance.ENEMY_WANDER_SPEED);
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
        log("State -> CHASE");
    }

    private void updateChase(final float tpf) {
        if (detector.canDetectPlayer()) {
            lostPlayerTimer = 0f;
            updateLastKnown();

            if (detector.isWithinAttackRange()) {
                enterAttack();
                return;
            }

            steer.seekTarget(
                    Main.PLAYER.getX() + body.getTileSize() / 2f,
                    Main.PLAYER.getY() + body.getTileSize() / 2f,
                    Balance.ENEMY_CHASE_SPEED
            );
            steer.applyWallRepulsion();

        } else {
            lostPlayerTimer += tpf;
            log("Chase: lost player, timeout in " + String.format("%.1f", Balance.ENEMY_LOST_PLAYER_TIMEOUT - lostPlayerTimer) + "s");

            if (lostPlayerTimer >= Balance.ENEMY_LOST_PLAYER_TIMEOUT) {
                enterWander();
                return;
            }

            if (hasLastKnown) {
                final float distToLastKnown = dist(body.centerX(), body.centerY(), lastKnownX, lastKnownY);

                if (distToLastKnown < Balance.ENEMY_WAYPOINT_REACH_DIST) {
                    body.stop();
                    return;
                }

                steer.seekTarget(lastKnownX, lastKnownY, Balance.ENEMY_CHASE_SPEED);
                steer.applyWallRepulsion();
            }
        }
    }

    private void enterWander() {
        state = EnemyState.WANDER;
        hasLastKnown = false;
        pickNewWanderDir();
        log("State -> WANDER (lost player)");
    }

    private void updateLastKnown() {
        lastKnownX = Main.PLAYER.getX() + body.getTileSize() / 2f;
        lastKnownY = Main.PLAYER.getY() + body.getTileSize() / 2f;
        hasLastKnown = true;
    }

    // =========================================================
    //  ATTACK
    // =========================================================

    private void enterAttack() {
        state = EnemyState.ATTACK;
        attackPhase = AttackPhase.TELEGRAPH;
        attackTimer = Balance.ENEMY_ATTACK_TELEGRAPH_DURATION;
        body.stop();
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

        if (!detector.canDetectPlayer()) {
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
        final int   tileSize = body.getTileSize();
        final float dx       = (Main.PLAYER.getX() + tileSize / 2f) - body.centerX();
        final float dy       = (Main.PLAYER.getY() + tileSize / 2f) - body.centerY();
        final float dist     = dist(0, 0, dx, dy);

        if (dist > 0f) {
            body.setVelocity(
                    (dx / dist) * Balance.ENEMY_ATTACK_LUNGE_SPEED,
                    (dy / dist) * Balance.ENEMY_ATTACK_LUNGE_SPEED
            );
        }

        attackPhase = AttackPhase.LUNGE;
        attackTimer = Balance.ENEMY_ATTACK_LUNGE_DURATION;
        log("Attack | Phase -> LUNGE");
    }

    private void updateLunge(final float tpf) {
        attackTimer -= tpf;

        if (attackTimer <= 0f) {
            body.stop();
            attackPhase   = AttackPhase.COOLDOWN;
            cooldownTimer = Balance.ENEMY_ATTACK_COOLDOWN;
            log("Attack | Phase -> COOLDOWN");
        }
    }

    private void updateCooldown(final float tpf) {
        cooldownTimer -= tpf;

        if (cooldownTimer <= 0f) {
            if (detector.canDetectPlayer() && detector.isWithinAttackRange()) {
                enterAttack();
            } else if (detector.canDetectPlayer()) {
                enterChase();
            } else {
                enterWander();
            }
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private static float dist(final float x1, final float y1, final float x2, final float y2) {
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void log(final String msg) {
        System.out.println("[" + id + "] " + msg);
    }

    public float getX()           { return body.getX(); }
    public float getY()           { return body.getY(); }
    public float getShakeOffset() { return shakeOffset; }
    public float getBobOffset()   { return body.getBobOffset(); }
    public EnemyState getState()  { return state; }
}