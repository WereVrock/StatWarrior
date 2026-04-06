package entity.enemy;

import entity.enemy.EnemyBody;
import balance.Balance;
import entity.ProjectileManager;
import main.Main;

public final class EnemyRanged {

    private final EnemyBody         body;
    private final ProjectileManager projectileManager;
    private final String            id;

    public EnemyRanged(final EnemyBody body, final ProjectileManager projectileManager,
                       final String id) {
        this.body              = body;
        this.projectileManager = projectileManager;
        this.id                = id;
    }

    public void fire() {
        final float dx   = (Main.PLAYER.getX() + body.getTileSize() / 2f) - body.centerX();
        final float dy   = (Main.PLAYER.getY() + body.getTileSize() / 2f) - body.centerY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 1f) return;
        projectileManager.spawn(body.centerX(), body.centerY(), dx / dist, dy / dist);
        log("projectile fired");
    }

    private void log(final String msg) {
        System.out.println("[" + id + "][Ranged] " + msg);
    }
}