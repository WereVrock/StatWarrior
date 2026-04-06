// ===== entity/EnemyHealth.java =====
package entity.enemy;

import balance.Balance;

public final class EnemyHealth {

    private static final float DEATH_TILT_DURATION = 0.45f; // seconds to fully fall back

    private float hp;
    private float deathTimer; // counts up after death

    public EnemyHealth() {
        hp         = Balance.ENEMY_HEALTH;
        deathTimer = 0f;
    }

    public void update(final float tpf) {
        if (isDead() && deathTimer < DEATH_TILT_DURATION) {
            deathTimer += tpf;
            if (deathTimer > DEATH_TILT_DURATION) deathTimer = DEATH_TILT_DURATION;
        }
    }

    public void damage(final float amount) {
        hp -= amount;
        if (hp < 0f) hp = 0f;
    }

    /** 0 = upright, 1 = fully fallen back. Only meaningful when isDead(). */
    public float getDeathTiltFraction() {
        return deathTimer / DEATH_TILT_DURATION;
    }

    /** True once the fall animation is complete and the sprite should vanish. */
    public boolean isDeathAnimDone() {
        return isDead() && deathTimer >= DEATH_TILT_DURATION;
    }

    public boolean isDead()         { return hp <= 0f; }
    public float   getFraction()    { return hp / Balance.ENEMY_HEALTH; }
    public float   getHp()          { return hp; }
}