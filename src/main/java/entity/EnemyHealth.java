// ===== entity/EnemyHealth.java =====
package entity;

import balance.Balance;

/** Tracks an enemy's health and dead state. */
public final class EnemyHealth {

    private float hp;

    public EnemyHealth() {
        hp = Balance.ENEMY_HEALTH;
    }

    public void damage(final float amount) {
        hp -= amount;
        if (hp < 0f) hp = 0f;
    }

    public boolean isDead()         { return hp <= 0f; }
    public float   getFraction()    { return hp / Balance.ENEMY_HEALTH; }
    public float   getHp()          { return hp; }
}