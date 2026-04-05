// ===== entity/PlayerConstants.java =====
package entity;

public final class PlayerConstants {

    public static final float DODGE_SPEED         = 12f;
    public static final float DODGE_DURATION      = 0.18f;
    public static final float DODGE_FREEZE        = 0.5f;
    public static final float DODGE_COOLDOWN      = 3.0f;

    public static final float PARRY_DURATION      = 0.5f;
    public static final float PARRY_COOLDOWN      = 3.0f;

    public static final float BOUNCE_SPEED        = 6f;
    public static final float BOUNCE_MIN_DIST     = 1.5f; // tiles — melee range to bounce back to

    private PlayerConstants() {}
}