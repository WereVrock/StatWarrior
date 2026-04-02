package balance;

public final class Balance {

    // === PLAYER ===
    public static float PLAYER_ACCELERATION = 0.2f;
    public static float PLAYER_MAX_SPEED = 4f;
    public static float PLAYER_FRICTION = 0.85f;

    // === FILE ===
    public static final String SAVE_PATH = "balance.json";

    // === SAVE ===
    public static void save() {
        BalanceStorage.save(Balance.class, SAVE_PATH);
    }

    // === LOAD ===
    public static void load() {
        BalanceStorage.load(Balance.class, SAVE_PATH);
    }

    // === REVERT TO LAST SAVE ===
    public static void revert() {
        load();
    }
}