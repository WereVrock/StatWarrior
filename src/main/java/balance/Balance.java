// ===== balance/Balance.java =====
package balance;

public final class Balance {
    // any variables related to game balance should be placed here. these fields are saved and loaded from json. IF AI changes values it should explicitely tell the user in a separate block comment so that user can update the json. new fields are handled automatically. dont delete this comment
    // === PLAYER ===
    public static float PLAYER_ACCELERATION = 0.2f;
    public static float PLAYER_MAX_SPEED    = 4f;
    public static float PLAYER_FRICTION     = 0.85f;
    // === ENEMY ===
    public static float ENEMY_ACCELERATION               = 0.15f;
    public static float ENEMY_WANDER_SPEED               = 1.5f;
    public static float ENEMY_CHASE_SPEED                = 3.0f;
    public static float ENEMY_FRICTION                   = 0.80f;
    public static float ENEMY_DETECT_RANGE               = 6f;
    public static float ENEMY_WANDER_DIR_CHANGE_INTERVAL = 2.0f;
    public static float ENEMY_WANDER_DIR_CHANGE_VARIANCE = 1.5f;
    public static float ENEMY_LOST_PLAYER_TIMEOUT        = 15.0f;
    public static float ENEMY_WAYPOINT_REACH_DIST        = 12.0f;
    public static float ENEMY_WALL_REPULSION             = 1.0f;
    // === ATTACK SHARED ===
    public static float ENEMY_ATTACK_TELEGRAPH_DURATION = 0.5f;
    public static float ENEMY_ATTACK_COOLDOWN           = 1.2f;
    public static float ENEMY_SHAKE_AMOUNT              = 0.08f;
    public static float ENEMY_SHAKE_INTERVAL            = 0.07f;
    // === MELEE ===
    public static float ENEMY_MELEE_RANGE           = 1.5f;
    public static float ENEMY_MELEE_LUNGE_SPEED     = 14f;
    public static float ENEMY_MELEE_LUNGE_DURATION  = 0.15f;
    public static float ENEMY_MELEE_RETURN_SPEED    = 10f;
    public static float ENEMY_MELEE_RETURN_DURATION = 0.2f;
    public static float ENEMY_MELEE_HIT_RADIUS      = 1.2f;
    // === RANGED ===
    public static float ENEMY_RANGED_RANGE  = 5f;
    public static float PROJECTILE_SPEED    = 6f;
    public static float PROJECTILE_LIFETIME = 3f;
    // === CHARGE ===
    public static float ENEMY_CHARGE_RANGE      = 4f;
    public static float ENEMY_CHARGE_SPEED      = 16f;
    public static float ENEMY_CHARGE_DURATION   = 0.4f;
    public static float ENEMY_CHARGE_HIT_RADIUS = 1.0f;
    // === FILE ===
    public static final String SAVE_PATH = "balance.json";
    // BOB constants removed — now in entity.BobConstants as static final
}