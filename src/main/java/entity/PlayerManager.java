package entity;

import render3d.HitFlash;

public final class PlayerManager {

    private HitFlash hitFlash;

    public void init(final HitFlash hitFlash) {
        this.hitFlash = hitFlash;
    }

    public void getsHit() {
        if (hitFlash != null) {
            hitFlash.trigger();
        }
    }
}