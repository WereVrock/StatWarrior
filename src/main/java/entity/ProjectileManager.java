package entity;

import balance.Balance;

import java.util.ArrayList;
import java.util.List;

public final class ProjectileManager {

    private final List<Projectile> projectiles = new ArrayList<>();
    private boolean playerHitThisFrame = false;

    public void spawn(final float x,    final float y,
                      final float dirX, final float dirY) {
        projectiles.add(new Projectile(
                x, y, dirX, dirY,
                Balance.PROJECTILE_SPEED,
                Balance.PROJECTILE_LIFETIME
        ));
    }

    public void update(final float tpf) {
        playerHitThisFrame = false;

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            final Projectile p = projectiles.get(i);
            p.update(tpf);

            if (p.hitsPlayer()) {
                playerHitThisFrame = true;
               
                p.kill();
            }

            if (p.isDead()) {
                projectiles.remove(i);
            }
        }
    }

    public boolean wasPlayerHitThisFrame() { return playerHitThisFrame; }
    public List<Projectile> getProjectiles() { return projectiles; }
}