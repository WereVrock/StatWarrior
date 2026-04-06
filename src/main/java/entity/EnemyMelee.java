// ===== entity/EnemyMelee.java =====
package entity;

import balance.Balance;
import main.Main;

public final class EnemyMelee {

    private float originX, originY;

    private final EnemyBody body;
    private final String    id;

    public EnemyMelee(final EnemyBody body, final String id) {
        this.body = body;
        this.id   = id;
    }

    public void recordOrigin() {
        originX = body.centerX();
        originY = body.centerY();
    }

    public void launchTowardPlayer() {
        final int   ts = body.getTileSize();
        final float tx = Main.PLAYER.getX() + ts / 2f;
        final float ty = Main.PLAYER.getY() + ts / 2f;
        launchToward(tx, ty, Balance.ENEMY_MELEE_LUNGE_SPEED);
        log("lunge launched");
    }

    public void returnToOrigin() {
        launchToward(originX, originY, Balance.ENEMY_MELEE_RETURN_SPEED);
        log("returning to origin");
    }

    public boolean checkHit() {
        final float dx     = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy     = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float distSq = dx * dx + dy * dy;
        final float range  = Balance.ENEMY_MELEE_HIT_RADIUS * body.getTileSize();

        if (distSq > range * range) return false;

        final boolean parried = Main.PLAYER.tryParry(body.centerX(), body.centerY());

        if (!parried && !Main.PLAYER.isInvincible()) {
            Main.PLAYER_MANAGER.getsHit(body.centerX(), body.centerY());
        }

        body.bounceFromPlayer(PlayerConstants.BOUNCE_SPEED);
        return true;
    }

    private void launchToward(final float tx, final float ty, final float speed) {
        final float dx   = tx - body.centerX();
        final float dy   = ty - body.centerY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 1f) return;
        body.setVelocity((dx / dist) * speed, (dy / dist) * speed);
    }

    private void log(final String msg) {
        System.out.println("[" + id + "][Melee] " + msg);
    }
}