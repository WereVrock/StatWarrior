// ===== render3d/GameApplication.java =====
package render3d;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import main.Main;

import java.util.HashMap;
import java.util.Map;

public final class GameApplication extends SimpleApplication {

    public static GameApplication APP;

    private HitFlash    hitFlash;
    private PauseMenu   pauseMenu;
    private CooldownHUD cooldownHUD;

    private boolean paused = false;

    private final Map<String, Boolean> lastButtons = new HashMap<>();

    private static final float INITIAL_DELAY = 0.25f;
    private static final float REPEAT_RATE   = 0.12f;

    private float upTimer   = 0f;
    private float downTimer = 0f;
    private boolean upHeld   = false;
    private boolean downHeld = false;

    public GameApplication() {
        APP = this;
    }

    public static void startApp() {
        final GameApplication app      = new GameApplication();
        final AppSettings     settings = new AppSettings(true);

        settings.setTitle("Modular Dungeon 3D");
        settings.setFullscreen(true);

        final java.awt.DisplayMode dm =
                java.awt.GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDisplayMode();

        settings.setResolution(dm.getWidth(), dm.getHeight());
        app.setSettings(settings);
        app.setShowSettings(false);
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
        Main.THIRD_PERSON_CAMERA.setFirstPersonAllowed(true);

        hitFlash = new HitFlash(settings);
        Main.PLAYER_MANAGER.init(hitFlash);

        pauseMenu = new PauseMenu(
                assetManager.loadFont("Interface/Fonts/Default.fnt"),
                settings.getWidth(),
                settings.getHeight(),
                assetManager,
                () -> paused = false,
                () -> AppLifecycle.exit()
        );
        guiNode.attachChild(pauseMenu.getNode());
        pauseMenu.hide();

        cooldownHUD = new CooldownHUD(guiNode, assetManager,
                settings.getWidth(), settings.getHeight());
    }

    @Override
    public void simpleUpdate(final float tpf) {
        Main.CONTROLLER.update();

        handlePauseToggle();
        handleMenuInput(tpf);

        if (paused) return;

        Main.PLAYER.update();
        Main.CAMERA.update(Main.PLAYER);
        Main.ENEMY_MANAGER.update(tpf);

        PlayerRenderer3D.update();
        EnemyRenderer3D.update(Main.ENEMY_MANAGER);
        ProjectileRenderer3D.update(Main.ENEMY_MANAGER.getProjectileManager());
        Main.THIRD_PERSON_CAMERA.update(cam);

        hitFlash.update(tpf);
        cooldownHUD.update();
    }

    private void handlePauseToggle() {
        if (pressedButton("START")) {
            paused = !paused;
            if (paused) {
                pauseMenu.show();
                resetTimers();
            } else {
                pauseMenu.hide();
            }
        }
    }

    private void handleMenuInput(final float tpf) {
        if (!paused) return;

        final boolean up   = Main.CONTROLLER.isUpPressed();
        final boolean down = Main.CONTROLLER.isDownPressed();

        if (up) {
            if (!upHeld) {
                pauseMenu.moveUp();
                upHeld  = true;
                upTimer = INITIAL_DELAY;
            } else {
                upTimer -= tpf;
                if (upTimer <= 0f) {
                    pauseMenu.moveUp();
                    upTimer = REPEAT_RATE;
                }
            }
        } else {
            upHeld = false;
        }

        if (down) {
            if (!downHeld) {
                pauseMenu.moveDown();
                downHeld  = true;
                downTimer = INITIAL_DELAY;
            } else {
                downTimer -= tpf;
                if (downTimer <= 0f) {
                    pauseMenu.moveDown();
                    downTimer = REPEAT_RATE;
                }
            }
        } else {
            downHeld = false;
        }

        if (pressedButton("A")) pauseMenu.select();
        if (pressedButton("B")) pauseMenu.back();
    }

    private void resetTimers() {
        upHeld = false; downHeld = false;
        upTimer = 0f;   downTimer = 0f;
    }

    private boolean pressedButton(final String key) {
        final boolean current = Main.CONTROLLER.isButtonPressed(key);
        final boolean last    = lastButtons.getOrDefault(key, false);
        lastButtons.put(key, current);
        return current && !last;
    }

    @Override
    public void destroy() {
        super.destroy();
        AppLifecycle.exit();
    }
}