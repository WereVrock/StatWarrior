package entity;

import balance.Balance;

import java.util.List;

public final class Enemy {

    private EnemyState state;

    private final List<AttackType> attackTypes;
    private final EnemyBody        body;
    private final EnemyDetector    detector;
    private final EnemyWander      wander;
    private final EnemyChase       chase;
    private final EnemyAttack      attack;
    private final String           id;

    public Enemy(final float startX,      final float startY,
                 final int tileSize,      final String id,
                 final ProjectileManager projectileManager,
                 final List<AttackType> attackTypes) {
        this.id          = id;
        this.attackTypes = attackTypes;
        this.body        = new EnemyBody(startX, startY, tileSize);
        this.detector    = new EnemyDetector(body);

        final EnemySteer  steer  = new EnemySteer(body);
        final EnemyMelee  melee  = new EnemyMelee(body, id);
        final EnemyRanged ranged = new EnemyRanged(body, projectileManager, id);
        final EnemyCharge charge = new EnemyCharge(body, id);

        this.wander = new EnemyWander(body, steer, id);
        this.chase  = new EnemyChase(body, detector, steer, id);
        this.attack = new EnemyAttack(attackTypes, body, detector, melee, ranged, charge, id);
        this.state  = EnemyState.WANDER;

        log("Spawned at tile (" + (int)startX + ", " + (int)startY
                + ") types=" + attackTypes);
    }

    public void update(final float tpf) {
        switch (state) {
            case WANDER -> {
                if (detector.canDetectPlayer()) enterChase();
                else                            wander.update(tpf);
            }
            case CHASE -> {
                if (detector.canDetectPlayer() && isAttackRangeReached()) {
                    enterAttack();
                } else {
                    final boolean timedOut = chase.update(tpf);
                    if (timedOut) enterWander();
                }
            }
            case ATTACK -> {
                final boolean lostPlayer = attack.update(tpf);
                if (lostPlayer) {
                    enterWander();
                } else if (attack.isCooldownDone()) {
                    if (detector.canDetectPlayer() && isAttackRangeReached()) enterAttack();
                    else if (detector.canDetectPlayer())                       enterChase();
                    else                                                       enterWander();
                }
            }
        }
        body.applyPhysics(attack.isLunging() && state == EnemyState.ATTACK);
        body.updateBob();
    }

    private void enterChase() {
        state = EnemyState.CHASE;
        chase.reset();
        log("State -> CHASE");
    }

    private void enterAttack() {
        state = EnemyState.ATTACK;
        attack.enter();
        log("State -> ATTACK");
    }

    private void enterWander() {
        state = EnemyState.WANDER;
        chase.clearLastKnown();
        log("State -> WANDER");
    }

    private boolean isAttackRangeReached() {
        return switch (attack.getCurrentAttackType()) {
            case MELEE  -> detector.isWithinMeleeRange();
            case RANGED -> detector.isWithinRangedRange();
            case CHARGE -> detector.isWithinChargeRange();
        };
    }

    private void log(final String msg) {
        System.out.println("[" + id + "] " + msg);
    }

    public float      getX()              { return body.getX(); }
    public float      getY()              { return body.getY(); }
    public float      getShakeOffset()    { return attack.getShakeOffset(); }
    public float      getBobOffset()      { return body.getBobOffset(); }
    public EnemyState getState()          { return state; }
    public AttackType getCurrentAttack()  { return attack.getCurrentAttackType(); }
    public List<AttackType> getAttackTypes() { return attackTypes; }
}