// ===== main/Main.java =====
package main;

import balance.BalanceStorage;
import controls.HybridController;
import controls.InputController;
import dungeon.Dungeon;
import entity.enemy.EnemyManager;
import entity.player.Player;
import entity.player.PlayerManager;
import render3d.cameras.Camera;
import render3d.cameras.FirstPersonCamera;
import render3d.GameApplication;


public final class Main {

    public static final Dungeon           DUNGEON             = new Dungeon();
    public static final InputController   CONTROLLER          = new HybridController();
    public static final FirstPersonCamera FIRST_PERSON_CAMERA = new FirstPersonCamera();
    public static final PlayerManager     PLAYER_MANAGER      = new PlayerManager();

    public static EnemyManager ENEMY_MANAGER;
    public static Player       PLAYER;
    public static Camera       CAMERA;

    public static balance.BalanceFrame BALANCE_FRAME;

    private static final int TILE_SIZE     = 32;
    private static final int SCREEN_WIDTH  = 800;
    private static final int SCREEN_HEIGHT = 600;

    static {
        initMutableState();
    }

    private static void initMutableState() {
        
        // getGrid() returns DungeonCell[][], keep consistent with existing API
        ENEMY_MANAGER = new EnemyManager();
        PLAYER        = new Player(
                DUNGEON.getGrid()[0].length / 2,
                DUNGEON.getGrid().length    / 2,
                TILE_SIZE,
                CONTROLLER
        );
        CAMERA = new Camera(SCREEN_WIDTH, SCREEN_HEIGHT, TILE_SIZE);
    }

    /** Called by GameApplication.restartGame() to reset all mutable game state. */
    public static void restart() {
        initMutableState();
    }

    private Main() {}

    public static void main(final String[] args) {
        BalanceStorage.init();
        BALANCE_FRAME = new balance.BalanceFrame();
        GameApplication.startApp();
    }
}