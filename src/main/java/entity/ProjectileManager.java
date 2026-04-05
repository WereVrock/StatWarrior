// ===== entity/ProjectileManager.java =====
package entity;

import balance.Balance;
import main.Main;

import java.util.ArrayList;
import java.util.List;

public final class ProjectileManager {

    private final List<Projectile> projectiles = new ArrayList<>();

    public void spawn(final float x,    final float y,
                      final float dirX, final float dirY) {
        projectiles.add(new Projectile(
                x, y, dirX, dirY,
                Balance.PROJECTILE_SPEED,
                Balance.PROJECTILE_LIFETIME
        ));
    }

    public void update(final float tpf) {
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            final Projectile p = projectiles.get(i);
            p.update(tpf);

            if (p.hitsPlayer()) {
                final boolean parried = Main.PLAYER.tryParry(p.getX(), p.getY());
                if (!parried && !Main.PLAYER.isInvincible()) {
                    Main.PLAYER_MANAGER.getsHit(p.getX(), p.getY());
                }
                p.kill();
            }

            if (p.isDead()) {
                projectiles.remove(i);
            }
        }
    }

    public List<Projectile> getProjectiles() { return projectiles; }
}