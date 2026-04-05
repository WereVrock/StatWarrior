package entity;

import balance.Balance;
import main.Main;

import java.util.Random;

public final class Enemy {

    private enum AttackPhase { TELEGRAPH, EXECUTE, RETURN, COOLDOWN }

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
    private int     shakeStep;

    private float meleeOriginX, meleeOriginY;

    private final AttackType        attackType;
    private final EnemyBody         body;
    private final EnemyDetector     detector;
    private final EnemySteer        steer;
    private final ProjectileManager projectileManager;
    private final String            id;

    public Enemy(final float startX,      final float startY,
                 final int tileSize,      final String id,
                 final AttackType attackType,
                 final ProjectileManager projectileManager) {
        this.id                = id;
        this.attackType        = attackType;
        this.projectileManager = projectileManager;
        this.body              = new EnemyBody(startX, startY, tileSize);
        this.detector          = new EnemyDetector(body);
        this.steer             = new EnemySteer(body);
        this.state             = EnemyState.WANDER;
        this.attackPhase       = AttackPhase.TELEGRAPH;
        this.hasLastKnown      = false;
        this.shakeStep         = 0;
        pickNewWanderDir();
        log("Spawned at tile (" + (int)startX + ", " + (int)startY + ") type=" + attackType);
    }

    public void update(final float tpf) {
        switch (state) {
            case WANDER -> updateWander(tpf);
            case CHASE  -> updateChase(tpf);
            case ATTACK -> updateAttack(tpf);
        }
        final boolean lunging = state == EnemyState.ATTACK
                && attackPhase == AttackPhase.EXECUTE
                && (attackType == AttackType.CHARGE || attackType == AttackType.MELEE);
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
        if (wanderTimer <= 0f) pickNewWanderDir();
        body.accelerate(wanderDirX * Balance.ENEMY_ACCELERATION,
                        wanderDirY * Balance.ENEMY_ACCELERATION);
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

            if (isAttackRangeReached()) {
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
            log("Chase: lost player, timeout in "
                    + String.format("%.1f", Balance.ENEMY_LOST_PLAYER_TIMEOUT - lostPlayerTimer) + "s");

            if (lostPlayerTimer >= Balance.ENEMY_LOST_PLAYER_TIMEOUT) {
                enterWander();
                return;
            }

            if (hasLastKnown) {
                final float distToLK = dist(body.centerX(), body.centerY(), lastKnownX, lastKnownY);
                if (distToLK < Balance.ENEMY_WAYPOINT_REACH_DIST) {
                    body.stop();
                    return;
                }
                steer.seekTarget(lastKnownX, lastKnownY, Balance.ENEMY_CHASE_SPEED);
                steer.applyWallRepulsion();
            }
        }
    }

    private boolean isAttackRangeReached() {
        return switch (attackType) {
            case MELEE  -> detector.isWithinMeleeRange();
            case RANGED -> detector.isWithinRangedRange();
            case CHARGE -> detector.isWithinChargeRange();
        };
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
    //  ATTACK dispatch
    // =========================================================

    private void enterAttack() {
        state       = EnemyState.ATTACK;
        attackPhase = AttackPhase.TELEGRAPH;
        attackTimer = Balance.ENEMY_ATTACK_TELEGRAPH_DURATION;
        shakeStep   = 0;
        shakeOffset = 0f;
        body.stop();
        log("State -> ATTACK type=" + attackType + " | Phase -> TELEGRAPH");
    }

    private void updateAttack(final float tpf) {
        switch (attackType) {
            case MELEE  -> updateMeleeAttack(tpf);
            case RANGED -> updateRangedAttack(tpf);
            case CHARGE -> updateChargeAttack(tpf);
        }
    }

    // =========================================================
    //  MELEE
    // =========================================================

    private void updateMeleeAttack(final float tpf) {
        switch (attackPhase) {
            case TELEGRAPH -> updateTelegraph(tpf, () -> {
                meleeOriginX = body.centerX();
                meleeOriginY = body.centerY();
                launchTowardPlayer(Balance.ENEMY_MELEE_LUNGE_SPEED);
                transitionTo(AttackPhase.EXECUTE, Balance.ENEMY_MELEE_LUNGE_DURATION);
                log("Melee | Phase -> EXECUTE");
            });
            case EXECUTE -> {
                attackTimer -= tpf;
                checkMeleeHit();
                if (attackTimer <= 0f) {
                    body.stop();
                    launchToward(meleeOriginX, meleeOriginY, Balance.ENEMY_MELEE_RETURN_SPEED);
                    transitionTo(AttackPhase.RETURN, Balance.ENEMY_MELEE_RETURN_DURATION);
                    log("Melee | Phase -> RETURN");
                }
            }
            case RETURN -> {
                attackTimer -= tpf;
                if (attackTimer <= 0f) {
                    body.stop();
                    transitionTo(AttackPhase.COOLDOWN, Balance.ENEMY_ATTACK_COOLDOWN);
                    log("Melee | Phase -> COOLDOWN");
                }
            }
            case COOLDOWN -> updateCooldown(tpf);
        }
    }

    private void checkMeleeHit() {
        final float dx     = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy     = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_MELEE_HIT_RADIUS * body.getTileSize();
        if (distSq <= range * range) {
            Main.PLAYER_MANAGER.getsHit();
        }
    }

    // =========================================================
    //  RANGED
    // =========================================================

    private void updateRangedAttack(final float tpf) {
        switch (attackPhase) {
            case TELEGRAPH -> updateTelegraph(tpf, () -> {
                fireProjectile();
                transitionTo(AttackPhase.COOLDOWN, Balance.ENEMY_ATTACK_COOLDOWN);
                log("Ranged | fired -> COOLDOWN");
            });
            case COOLDOWN -> updateCooldown(tpf);
            default -> { }
        }
    }

    private void fireProjectile() {
        final float dx   = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy   = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float dist = dist(0, 0, dx, dy);
        if (dist < 1f) return;
        projectileManager.spawn(body.centerX(), body.centerY(), dx / dist, dy / dist);
    }

    // =========================================================
    //  CHARGE
    // =========================================================

    private void updateChargeAttack(final float tpf) {
        switch (attackPhase) {
            case TELEGRAPH -> updateTelegraph(tpf, () -> {
                launchTowardPlayer(Balance.ENEMY_CHARGE_SPEED);
                transitionTo(AttackPhase.EXECUTE, Balance.ENEMY_CHARGE_DURATION);
                log("Charge | Phase -> EXECUTE");
            });
            case EXECUTE -> {
                attackTimer -= tpf;
                checkChargeHit();
                if (attackTimer <= 0f) {
                    body.stop();
                    transitionTo(AttackPhase.COOLDOWN, Balance.ENEMY_ATTACK_COOLDOWN);
                    log("Charge | Phase -> COOLDOWN");
                }
            }
            case COOLDOWN -> updateCooldown(tpf);
            default -> { }
        }
    }

    private void checkChargeHit() {
        final float dx     = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy     = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_CHARGE_HIT_RADIUS * body.getTileSize();
        if (distSq <= range * range) {
            Main.PLAYER_MANAGER.getsHit();
        }
    }

    // =========================================================
    //  SHARED ATTACK HELPERS
    // =========================================================

    private void updateTelegraph(final float tpf, final Runnable onComplete) {
        attackTimer -= tpf;

        // Discrete left-right shake: flip once per SHAKE_INTERVAL seconds
        final float elapsed  = Balance.ENEMY_ATTACK_TELEGRAPH_DURATION - attackTimer;
        final int   newStep  = (int)(elapsed / Balance.ENEMY_SHAKE_INTERVAL);
        if (newStep != shakeStep) {
            shakeStep   = newStep;
            shakeOffset = (shakeStep % 2 == 0)
                    ? Balance.ENEMY_SHAKE_AMOUNT
                    : -Balance.ENEMY_SHAKE_AMOUNT;
        }

        if (!detector.canDetectPlayer()) {
            shakeOffset = 0f;
            enterWander();
            return;
        }

        if (attackTimer <= 0f) {
            shakeOffset = 0f;
            onComplete.run();
        }
    }

    private void updateCooldown(final float tpf) {
        cooldownTimer -= tpf;
        if (cooldownTimer <= 0f) {
            if (detector.canDetectPlayer() && isAttackRangeReached()) {
                enterAttack();
            } else if (detector.canDetectPlayer()) {
                enterChase();
            } else {
                enterWander();
            }
        }
    }

    private void launchTowardPlayer(final float speed) {
        launchToward(
                Main.PLAYER.getX() + body.getTileSize() / 2f,
                Main.PLAYER.getY() + body.getTileSize() / 2f,
                speed
        );
    }

    private void launchToward(final float tx, final float ty, final float speed) {
        final float dx   = tx - body.centerX();
        final float dy   = ty - body.centerY();
        final float dist = dist(0, 0, dx, dy);
        if (dist < 1f) return;
        body.setVelocity((dx / dist) * speed, (dy / dist) * speed);
    }

    private void transitionTo(final AttackPhase phase, final float duration) {
        attackPhase = phase;
        if (phase == AttackPhase.COOLDOWN) {
            cooldownTimer = duration;
        } else {
            attackTimer = duration;
        }
    }

    private static float dist(final float x1, final float y1,
                               final float x2, final float y2) {
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void log(final String msg) {
        System.out.println("[" + id + "] " + msg);
    }

    public float     getX()           { return body.getX(); }
    public float     getY()           { return body.getY(); }
    public float     getShakeOffset() { return shakeOffset; }
    public float     getBobOffset()   { return body.getBobOffset(); }
    public EnemyState getState()      { return state; }
    public AttackType getAttackType() { return attackType; }
}