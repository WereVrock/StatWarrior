package balance;

public final class Balance {

    // any variables related to game balance should be placed here. dont delete this comment

    // === PLAYER ===
    public static float PLAYER_ACCELERATION = 0.2f;
    public static float PLAYER_MAX_SPEED    = 4f;
    public static float PLAYER_FRICTION     = 0.85f;

    // === ENEMY ===
    public static float ENEMY_ACCELERATION                  = 0.15f;
    public static float ENEMY_WANDER_SPEED                  = 1.5f;
    public static float ENEMY_CHASE_SPEED                   = 3.0f;
    public static float ENEMY_FRICTION                      = 0.80f;
    public static float ENEMY_DETECT_RANGE                  = 6f;   // in tiles
    public static float ENEMY_ATTACK_RANGE                  = 1.2f; // in tiles
    public static float ENEMY_WANDER_DIR_CHANGE_INTERVAL    = 2.0f; // seconds
    public static float ENEMY_WANDER_DIR_CHANGE_VARIANCE    = 1.5f; // seconds added randomly
    public static float ENEMY_LOST_PLAYER_TIMEOUT           = 15.0f; // seconds
    public static float ENEMY_WAYPOINT_REACH_DIST           = 8f;   // pixels

    // === FILE ===
    public static final String SAVE_PATH = "balance.json";
}