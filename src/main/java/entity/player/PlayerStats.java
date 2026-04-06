// ===== entity/PlayerStats.java =====
package entity.player;

import balance.Balance;

/**
 * Holds the player's three resource bars: health, muscle, stamina.
 * Muscle absorbs hits before health. When muscle is 0, health is damaged.
 * Stamina drains during sprint, regenerates when not sprinting.
 */
public final class PlayerStats {

    private float health;
    private float muscle;
    private float stamina;

    public PlayerStats() {
        health  = Balance.PLAYER_HEALTH;
        muscle  = Balance.PLAYER_MUSCLE;
        stamina = Balance.PLAYER_STAMINA;
    }

    public void update(final float tpf, final boolean sprinting) {
        if (sprinting && stamina > 0f) {
            stamina -= Balance.PLAYER_STAMINA_SPRINT_DRAIN * tpf;
            if (stamina < 0f) stamina = 0f;
        } else if (!sprinting) {
            stamina += Balance.PLAYER_STAMINA_REGEN * tpf;
            if (stamina > Balance.PLAYER_STAMINA) stamina = Balance.PLAYER_STAMINA;
        }
    }

    /** Applies a hit. Muscle absorbs first; overflow goes to health. */
    public void applyHit() {
        if (muscle > 0f) {
            muscle -= Balance.PLAYER_MUSCLE_HIT_DAMAGE;
            if (muscle < 0f) {
                health += muscle; // muscle went negative — bleed into health
                muscle = 0f;
            }
        } else {
            health -= Balance.PLAYER_HEALTH_HIT_DAMAGE;
            if (health < 0f) health = 0f;
        }
    }

    public boolean hasStamina()   { return stamina > 0f; }
    public boolean isDead()        { return health <= 0f; }

    public float getHealthFraction()  { return health  / Balance.PLAYER_HEALTH;  }
    public float getMuscleFraction()  { return muscle  / Balance.PLAYER_MUSCLE;  }
    public float getStaminaFraction() { return stamina / Balance.PLAYER_STAMINA; }

    public float getHealth()  { return health;  }
    public float getMuscle()  { return muscle;  }
    public float getStamina() { return stamina; }
}