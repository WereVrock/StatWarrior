package entity;

import balance.Balance;
import main.Main;

public final class EnemyCharge {

    private final EnemyBody body;
    private final String    id;

    public EnemyCharge(final EnemyBody body, final String id) {
        this.body = body;
        this.id   = id;
    }

    public void launch() {
        final int   ts   = body.getTileSize();
        final float tx   = Main.PLAYER.getX() + ts / 2f;
        final float ty   = Main.PLAYER.getY() + ts / 2f;
        final float dx   = tx - body.centerX();
        final float dy   = ty - body.centerY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 1f) return;
        body.setVelocity((dx / dist) * Balance.ENEMY_CHARGE_SPEED,
                         (dy / dist) * Balance.ENEMY_CHARGE_SPEED);
        log("charge launched");
    }

    public void checkHit() {
        final float dx     = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy     = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_CHARGE_HIT_RADIUS * body.getTileSize();
        if (distSq <= range * range) {
            Main.PLAYER_MANAGER.getsHit();
        }
    }

    private void log(final String msg) {
        System.out.println("[" + id + "][Charge] " + msg);
    }
}