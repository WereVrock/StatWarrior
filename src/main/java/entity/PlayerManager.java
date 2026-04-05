// ===== entity/PlayerManager.java =====
package entity;

import render3d.HitFlash;

public final class PlayerManager {

    private HitFlash hitFlash;

    public void init(final HitFlash hitFlash) {
        this.hitFlash = hitFlash;
    }

    /** Called when something contacts the player. Attacker world position is used for parry direction. */
    public void getsHit(final float attackerX, final float attackerY) {
        if (hitFlash != null) {
            hitFlash.trigger();
        }
    }
}