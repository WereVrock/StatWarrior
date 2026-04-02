package main;

import controls.HybridController;
import controls.InputController;
import dungeon.Dungeon;
import entity.Player;
import ui.Camera;
import ui.GameFrame;

public final class Main {


public static final Dungeon DUNGEON = new Dungeon();

// 🔥 zero setup logic
public static final InputController CONTROLLER = new HybridController();

public static final Player PLAYER;
public static final Camera CAMERA;

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
    new GameFrame();
}
 

}
