// ===== entity/PlayerManager.java =====
package entity.player;

import main.Main;
import render3d.HitFlash;

public final class PlayerManager {

    private HitFlash hitFlash;

    public void init(final HitFlash hitFlash) {
        this.hitFlash = hitFlash;
    }

    /** Applies hit damage to the player and triggers visual feedback. */
    public void getsHit(final float attackerX, final float attackerY) {
        Main.PLAYER.getStats().applyHit();
        
        if (hitFlash != null) {
            hitFlash.trigger();
        }
    }
}