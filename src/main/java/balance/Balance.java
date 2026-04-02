package balance;

import java.io.*;

public final class Balance implements Serializable {

    private static final long serialVersionUID = 1L;

    // === PLAYER ===
    public static float PLAYER_ACCELERATION = 0.2f;
    public static float PLAYER_MAX_SPEED = 4f;
    public static float PLAYER_FRICTION = 0.85f;

    // === FILE ===
    private static final String SAVE_PATH = "balance.dat";

    // === SAVE ===
    public static void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_PATH))) {
            out.writeObject(new BalanceData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === LOAD ===
    public static void load() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_PATH))) {
            BalanceData data = (BalanceData) in.readObject();
            apply(data);
        } catch (Exception e) {
            System.out.println("No balance file found, using defaults.");
        }
    }

    // === REVERT TO LAST SAVE ===
    public static void revert() {
        load();
    }

    // === INTERNAL SNAPSHOT CLASS ===
    private static class BalanceData implements Serializable {
        private static final long serialVersionUID = 1L;

        float PLAYER_ACCELERATION = Balance.PLAYER_ACCELERATION;
        float PLAYER_MAX_SPEED = Balance.PLAYER_MAX_SPEED;
        float PLAYER_FRICTION = Balance.PLAYER_FRICTION;
    }

    private static void apply(BalanceData data) {
        PLAYER_ACCELERATION = data.PLAYER_ACCELERATION;
        PLAYER_MAX_SPEED = data.PLAYER_MAX_SPEED;
        PLAYER_FRICTION = data.PLAYER_FRICTION;
    }
}