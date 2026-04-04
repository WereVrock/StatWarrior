package entity;

import balance.Balance;
import main.Main;

public final class EnemyDetector {

    private final EnemyBody body;

    public EnemyDetector(final EnemyBody body) {
        this.body = body;
    }

    public boolean canDetectPlayer() {
        final float dx     = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy     = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_DETECT_RANGE * body.getTileSize();
        return distSq <= range * range;
    }

    public boolean isWithinAttackRange() {
        final float dx     = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy     = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_ATTACK_RANGE * body.getTileSize();
        return distSq <= range * range;
    }
}