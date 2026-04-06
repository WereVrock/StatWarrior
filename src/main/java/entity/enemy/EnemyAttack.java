package entity.enemy;

import balance.Balance;
import entity.AttackType;

import java.util.List;

public final class EnemyAttack {

    public enum Phase { TELEGRAPH, EXECUTE, RETURN, COOLDOWN }

    private Phase phase;
    private float attackTimer;
    private float cooldownTimer;
    private int   shakeStep;
    private float shakeOffset;

    private int        attackIndex;
    private AttackType currentType;

    private final List<AttackType> attackTypes;
    private final EnemyBody        body;
    private final EnemyDetector    detector;
    private final EnemyMelee       melee;
    private final EnemyRanged      ranged;
    private final EnemyCharge      charge;
    private final String           id;

    public EnemyAttack(final List<AttackType> attackTypes,
                       final EnemyBody body, final EnemyDetector detector,
                       final EnemyMelee melee, final EnemyRanged ranged,
                       final EnemyCharge charge, final String id) {
        this.attackTypes = attackTypes;
        this.body        = body;
        this.detector    = detector;
        this.melee       = melee;
        this.ranged      = ranged;
        this.charge      = charge;
        this.id          = id;
        this.attackIndex = 0;
        this.currentType = attackTypes.get(0);
    }

    public void enter() {
        currentType = attackTypes.get(attackIndex);
        phase       = Phase.TELEGRAPH;
        attackTimer = Balance.ENEMY_ATTACK_TELEGRAPH_DURATION;
        shakeStep   = 0;
        shakeOffset = 0f;
        body.stop();
        log("enter TELEGRAPH type=" + currentType);
    }

    /** Returns true if player was lost during telegraph — caller should enter wander. */
    public boolean update(final float tpf) {
        return switch (currentType) {
            case MELEE  -> updateMelee(tpf);
            case RANGED -> updateRanged(tpf);
            case CHARGE -> updateCharge(tpf);
        };
    }

    // =========================================================
    //  MELEE
    // =========================================================

    private boolean updateMelee(final float tpf) {
        switch (phase) {
            case TELEGRAPH -> {
                if (tickTelegraph(tpf)) return true;
                if (attackTimer <= 0f) {
                    shakeOffset = 0f;
                    melee.recordOrigin();
                    melee.launchTowardPlayer();
                    transitionTo(Phase.EXECUTE, Balance.ENEMY_MELEE_LUNGE_DURATION);
                    log("MELEE EXECUTE");
                }
            }
            case EXECUTE -> {
                attackTimer -= tpf;
                melee.checkHit();
                if (attackTimer <= 0f) {
                    body.stop();
                    melee.returnToOrigin();
                    transitionTo(Phase.RETURN, Balance.ENEMY_MELEE_RETURN_DURATION);
                    log("MELEE RETURN");
                }
            }
            case RETURN -> {
                attackTimer -= tpf;
                if (attackTimer <= 0f) {
                    body.stop();
                    advanceAttackIndex();
                    transitionTo(Phase.COOLDOWN, Balance.ENEMY_ATTACK_COOLDOWN);
                    log("COOLDOWN");
                }
            }
            case COOLDOWN -> { cooldownTimer -= tpf; }
        }
        return false;
    }

    // =========================================================
    //  RANGED
    // =========================================================

    private boolean updateRanged(final float tpf) {
        switch (phase) {
            case TELEGRAPH -> {
                if (tickTelegraph(tpf)) return true;
                if (attackTimer <= 0f) {
                    shakeOffset = 0f;
                    ranged.fire();
                    advanceAttackIndex();
                    transitionTo(Phase.COOLDOWN, Balance.ENEMY_ATTACK_COOLDOWN);
                    log("RANGED fired -> COOLDOWN");
                }
            }
            case COOLDOWN -> { cooldownTimer -= tpf; }
            default -> { }
        }
        return false;
    }

    // =========================================================
    //  CHARGE
    // =========================================================

    private boolean updateCharge(final float tpf) {
        switch (phase) {
            case TELEGRAPH -> {
                if (tickTelegraph(tpf)) return true;
                if (attackTimer <= 0f) {
                    shakeOffset = 0f;
                    charge.launch();
                    transitionTo(Phase.EXECUTE, Balance.ENEMY_CHARGE_DURATION);
                    log("CHARGE EXECUTE");
                }
            }
            case EXECUTE -> {
                attackTimer -= tpf;
                charge.checkHit();
                if (attackTimer <= 0f) {
                    body.stop();
                    advanceAttackIndex();
                    transitionTo(Phase.COOLDOWN, Balance.ENEMY_ATTACK_COOLDOWN);
                    log("COOLDOWN");
                }
            }
            case COOLDOWN -> { cooldownTimer -= tpf; }
            default -> { }
        }
        return false;
    }

    // =========================================================
    //  SHARED
    // =========================================================

    private boolean tickTelegraph(final float tpf) {
        attackTimer -= tpf;
        final float elapsed = Balance.ENEMY_ATTACK_TELEGRAPH_DURATION - attackTimer;
        final int   newStep = (int)(elapsed / Balance.ENEMY_SHAKE_INTERVAL);
        if (newStep != shakeStep) {
            shakeStep   = newStep;
            shakeOffset = (shakeStep % 2 == 0)
                    ? Balance.ENEMY_SHAKE_AMOUNT
                    : -Balance.ENEMY_SHAKE_AMOUNT;
        }
        if (!detector.canDetectPlayer()) {
            shakeOffset = 0f;
            return true;
        }
        return false;
    }

    private void advanceAttackIndex() {
        attackIndex = (attackIndex + 1) % attackTypes.size();
        log("next attack index=" + attackIndex + " type=" + attackTypes.get(attackIndex));
    }

    private void transitionTo(final Phase p, final float duration) {
        phase = p;
        if (p == Phase.COOLDOWN) cooldownTimer = duration;
        else                     attackTimer   = duration;
    }

    public AttackType getCurrentAttackType() { return currentType; }
    public boolean isCooldownDone()  { return phase == Phase.COOLDOWN && cooldownTimer <= 0f; }
    public boolean isLunging()       {
        return phase == Phase.EXECUTE
                && (currentType == AttackType.MELEE || currentType == AttackType.CHARGE);
    }
    public float getShakeOffset()    { return shakeOffset; }

    private void log(final String msg) {
        System.out.println("[" + id + "][Attack] " + msg);
    }
}