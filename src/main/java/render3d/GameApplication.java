// ===== render3d/GameApplication.java =====
package render3d;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import main.Main;

public final class GameApplication extends SimpleApplication {

    public static GameApplication APP;

    private HitFlash  hitFlash;
    private PauseMenu pauseMenu;

    public GameApplication() {
        APP = this;
    }

    public static void startApp() {
        final GameApplication app      = new GameApplication();
        final AppSettings     settings = new AppSettings(true);
        settings.setTitle("Modular Dungeon 3D");
        settings.setFullscreen(true);

        // Grab the primary screen resolution for fullscreen
        final java.awt.DisplayMode dm =
                java.awt.GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDisplayMode();
        settings.setResolution(dm.getWidth(), dm.getHeight());

        app.setSettings(settings);
        app.setShowSettings(false); // skip the jME settings dialog
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        org.lwjgl.glfw.GLFW.glfwSetInputMode(
                ((com.jme3.system.lwjgl.LwjglWindow) context).getWindowHandle(),
                org.lwjgl.glfw.GLFW.GLFW_CURSOR,
                org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL
        );

        DungeonRenderer3D.renderDungeon();
        PlayerRenderer3D.init();
        EnemyRenderer3D.init(Main.ENEMY_MANAGER);
        Main.THIRD_PERSON_CAMERA.init(cam);

        hitFlash = new HitFlash(settings);
        Main.PLAYER_MANAGER.init(hitFlash);

        // Build pause menu on the Swing thread; hide it until Start is pressed
        javax.swing.SwingUtilities.invokeLater(() -> {
            pauseMenu = new PauseMenu(
                    () -> { /* continue — game is already running */ },
                    () -> AppLifecycle.exit() // restart: simplest approach is full relaunch
            );
        });
    }

    @Override
    public void simpleUpdate(final float tpf) {
        Main.CONTROLLER.update();

        handleStartButton();

        Main.PLAYER.update();
        Main.CAMERA.update(Main.PLAYER);

        Main.ENEMY_MANAGER.update(tpf);

        PlayerRenderer3D.update();
        EnemyRenderer3D.update(Main.ENEMY_MANAGER);
        ProjectileRenderer3D.update(Main.ENEMY_MANAGER.getProjectileManager());
        Main.THIRD_PERSON_CAMERA.update(cam);

        hitFlash.update(tpf);
    }

    @Override
    public void destroy() {
        super.destroy();
        AppLifecycle.exit();
    }

    private void handleStartButton() {
        if (Main.CONTROLLER.isButtonPressed("START") && pauseMenu != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                System.out.println("start button pressed");
                if (!pauseMenu.isVisible()) {
                    pauseMenu.setVisible(true);
                }
            });
        }
    }
}