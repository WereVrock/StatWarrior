// ===== entity/PlayerMelee.java =====
package entity.player;

import entity.enemy.Enemy;
import balance.Balance;
import main.Main;

/**
 * Handles the player's melee attack (B button).
 * No telegraph — instant lunge toward nearest enemy or camera-forward if none in range.
 */
public final class PlayerMelee {

    private PlayerMeleeState state    = PlayerMeleeState.IDLE;
    private float            timer    = 0f;
    private float            cooldown = 0f;

    private float originX, originY;

    public void update(final float tpf, final boolean buttonJustPressed) {
        cooldown -= tpf;
        if (cooldown < 0f) cooldown = 0f;

        switch (state) {
            case IDLE -> {
                if (buttonJustPressed && cooldown <= 0f) {
                    launch();
                }
            }
            case LUNGING -> {
                timer -= tpf;
                checkHit();
                if (timer <= 0f) {
                    returnToOrigin();
                }
            }
            case RETURNING -> {
                timer -= tpf;
                if (timer <= 0f) {
                    Main.PLAYER.stopVelocity();
                    state    = PlayerMeleeState.IDLE;
                    cooldown = Balance.PLAYER_MELEE_COOLDOWN;
                }
            }
        }
    }

    private void launch() {
        originX = Main.PLAYER.centerX();
        originY = Main.PLAYER.centerY();

        final float yaw      = Main.THIRD_PERSON_CAMERA.getYaw();
        float       dirX     = (float) Math.sin(yaw);
        float       dirY     = (float) Math.cos(yaw);

        // Prefer nearest enemy in front within range
        final Enemy nearest = nearestEnemyInRange();
        if (nearest != null) {
            final float dx   = nearest.getX() + 16f - Main.PLAYER.centerX();
            final float dy   = nearest.getY() + 16f - Main.PLAYER.centerY();
            final float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 0.001f) {
                dirX = dx / dist;
                dirY = dy / dist;
            }
        }

        Main.PLAYER.setVelocityDirect(
                dirX * Balance.PLAYER_MELEE_LUNGE_SPEED,
                dirY * Balance.PLAYER_MELEE_LUNGE_SPEED);

        state = PlayerMeleeState.LUNGING;
        timer = Balance.PLAYER_MELEE_LUNGE_DURATION;
    }

    private void returnToOrigin() {
        final float dx   = originX - Main.PLAYER.centerX();
        final float dy   = originY - Main.PLAYER.centerY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0.001f) {
            Main.PLAYER.setVelocityDirect(
                    (dx / dist) * Balance.PLAYER_MELEE_RETURN_SPEED,
                    (dy / dist) * Balance.PLAYER_MELEE_RETURN_SPEED);
        }
        state = PlayerMeleeState.RETURNING;
        timer = Balance.PLAYER_MELEE_RETURN_DURATION;
    }

    private void checkHit() {
        for (final Enemy enemy : Main.ENEMY_MANAGER.getEnemies()) {
            if (enemy.getHealth().isDead()) continue;

            final float dx     = enemy.getX() + 16f - Main.PLAYER.centerX();
            final float dy     = enemy.getY() + 16f - Main.PLAYER.centerY();
            final float distSq = dx * dx + dy * dy;
            final float range  = Balance.PLAYER_MELEE_HIT_RADIUS * 32f;

            if (distSq <= range * range) {
                enemy.getHealth().damage(Balance.PLAYER_MELEE_DAMAGE);
            }
        }
    }

    private Enemy nearestEnemyInRange() {
        Enemy nearest  = null;
        float minDistSq = Balance.PLAYER_MELEE_RANGE * Balance.PLAYER_MELEE_RANGE;

        for (final Enemy enemy : Main.ENEMY_MANAGER.getEnemies()) {
            if (enemy.getHealth().isDead()) continue;
            final float dx = enemy.getX() + 16f - Main.PLAYER.centerX();
            final float dy = enemy.getY() + 16f - Main.PLAYER.centerY();
            final float d2 = dx * dx + dy * dy;
            if (d2 < minDistSq) {
                minDistSq = d2;
                nearest   = enemy;
            }
        }
        return nearest;
    }

    public boolean isLunging()    { return state == PlayerMeleeState.LUNGING;   }
    public boolean isReturning()  { return state == PlayerMeleeState.RETURNING; }
    public boolean isActive()     { return state != PlayerMeleeState.IDLE;      }
    public float   getCooldownFraction() {
        return Balance.PLAYER_MELEE_COOLDOWN > 0f
                ? 1f - (cooldown / Balance.PLAYER_MELEE_COOLDOWN)
                : 1f;
    }
}