package entity.enemy;

import balance.Balance;
import main.Main;

public final class EnemyDetector {

    private final EnemyBody body;

    public EnemyDetector(final EnemyBody body) {
        this.body = body;
    }

    public boolean canDetectPlayer() {
        return distToPlayer() <= Balance.ENEMY_DETECT_RANGE * body.getTileSize();
    }

    public boolean isWithinMeleeRange() {
        return distToPlayer() <= Balance.ENEMY_MELEE_RANGE * body.getTileSize();
    }

    public boolean isWithinRangedRange() {
        return distToPlayer() <= Balance.ENEMY_RANGED_RANGE * body.getTileSize();
    }

    public boolean isWithinChargeRange() {
        return distToPlayer() <= Balance.ENEMY_CHARGE_RANGE * body.getTileSize();
    }

    public float distToPlayer() {
        final float dx = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}