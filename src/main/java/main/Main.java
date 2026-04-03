package main;

import balance.BalanceFrame;
import controls.HybridController;
import controls.InputController;
import dungeon.Dungeon;
import entity.Player;
import render3d.Camera;
import render3d.GameApplication;
import render3d.ThirdPersonCamera;

public final class Main {

    public static final Dungeon DUNGEON = new Dungeon();
    public static final InputController CONTROLLER = new HybridController();
    public static final ThirdPersonCamera THIRD_PERSON_CAMERA = new ThirdPersonCamera();

    public static final Player PLAYER;
    public static final Camera CAMERA;
    public static BalanceFrame BALANCE_FRAME;

    static {
        final int tileSize = 32;
        final int screenWidth = 800;
        final int screenHeight = 600;

        final var grid = DUNGEON.getGrid();

        PLAYER = new Player(
                grid[0].length / 2,
                grid.length / 2,
                tileSize,
                CONTROLLER
        );

        CAMERA = new Camera(screenWidth, screenHeight, tileSize);
    }

    private Main() {}

    public static void main(final String[] args) {
        balance.BalanceStorage.init();
        BALANCE_FRAME = new BalanceFrame();
        GameApplication.startApp();
    }
}