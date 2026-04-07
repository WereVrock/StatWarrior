package render3d;

import render3d.screenRendering.YouDiedOverlay;
import render3d.worldRender.EnemyRenderer3D;
import render3d.worldRender.DungeonRenderer3D;
import render3d.worldRender.EnemyHealthBarRenderer3D;
import render3d.worldRender.ProjectileRenderer3D;
import render3d.worldRender.PalyerShadowRenderer3D;
import render3d.worldRender.EnemyShadowRenderer3D;
import render3d.screenRendering.CooldownHUD;
import render3d.screenRendering.PlayerHUD;
import render3d.screenRendering.PauseMenu;
import render3d.screenRendering.HitFlash;
import render3d.cameras.CameraDebugPanel;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import main.Main;

import java.util.HashMap;
import java.util.Map;

public final class GameApplication extends SimpleApplication {

    public static GameApplication APP;

    private HitFlash hitFlash;
    private PauseMenu pauseMenu;
    private CooldownHUD cooldownHUD;
    private PlayerHUD playerHUD;
    private EnemyHealthBarRenderer3D enemyHealthBars;
    private YouDiedOverlay youDiedOverlay;

    private boolean paused     = false;
    private boolean playerDead = false;

    private final Map<String, Boolean> lastButtons = new HashMap<>();

    private static final float INITIAL_DELAY = 0.25f;
    private static final float REPEAT_RATE   = 0.12f;

    private float   upTimer   = 0f;
    private float   downTimer = 0f;
    private boolean upHeld    = false;
    private boolean downHeld  = false;

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
        cam.setFrustumNear(0.87f);
        cam.setFov(46);
       System.out.println( "frustrum : "+cam.getFrustumNear() + "- " +cam.getFrustumFar());
       CameraDebugPanel.open(() -> cam);

        org.lwjgl.glfw.GLFW.glfwSetInputMode(
                ((com.jme3.system.lwjgl.LwjglWindow) context).getWindowHandle(),
                org.lwjgl.glfw.GLFW.GLFW_CURSOR,
                org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL
        );

        getRootNode().detachAllChildren();
        getGuiNode().detachAllChildren();

        DungeonRenderer3D.renderDungeon();
        PalyerShadowRenderer3D.init();
        EnemyRenderer3D.init(Main.ENEMY_MANAGER);
        EnemyShadowRenderer3D.init(Main.ENEMY_MANAGER);

        Main.FIRST_PERSON_CAMERA.init(cam);

        hitFlash = new HitFlash(settings);
        Main.PLAYER_MANAGER.init(hitFlash);

        enemyHealthBars = new EnemyHealthBarRenderer3D();
        enemyHealthBars.init(Main.ENEMY_MANAGER, assetManager);

        pauseMenu = new PauseMenu(
                assetManager.loadFont("Interface/Fonts/Default.fnt"),
                settings.getWidth(),
                settings.getHeight(),
                assetManager,
                () -> paused = false,
                this::restartGame
        );
        guiNode.attachChild(pauseMenu.getNode());
        pauseMenu.hide();

        youDiedOverlay = new YouDiedOverlay(
                guiNode,
                assetManager.loadFont("Interface/Fonts/Default.fnt"),
                settings.getWidth(),
                settings.getHeight(),
                this::restartGame,
                AppLifecycle::exit
        );

        cooldownHUD = new CooldownHUD(guiNode, assetManager,
                settings.getWidth(), settings.getHeight());

        playerHUD = new PlayerHUD(guiNode, assetManager, settings.getHeight());

        paused     = false;
        playerDead = false;
    }

    private void restartGame() {
        paused     = false;
        playerDead = false;
        lastButtons.clear();
        resetTimers();
        youDiedOverlay.hide();
        Main.restart();
        simpleInitApp();
    }

    @Override
    public void simpleUpdate(final float tpf) {
        Main.CONTROLLER.update();

        // Death screen takes priority over everything
        if (playerDead) {
            youDiedOverlay.update(tpf);
            return;
        }

        handlePauseToggle();
        handleMenuInput(tpf);

        if (paused) return;

        Main.PLAYER.update(tpf);
        Main.ENEMY_MANAGER.update(tpf);

        PalyerShadowRenderer3D.update();
        EnemyRenderer3D.update(Main.ENEMY_MANAGER);
        EnemyShadowRenderer3D.update(Main.ENEMY_MANAGER);
        ProjectileRenderer3D.update(Main.ENEMY_MANAGER.getProjectileManager());

        Main.FIRST_PERSON_CAMERA.update();

        hitFlash.update(tpf);
        cooldownHUD.update();
        playerHUD.update();
        enemyHealthBars.update(Main.ENEMY_MANAGER);

        checkPlayerDeath();
    }

    private void checkPlayerDeath() {
        if (Main.PLAYER.getStats().isDead()) {
            playerDead = true;
            youDiedOverlay.show();
        }
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
        upHeld    = false;
        downHeld  = false;
        upTimer   = 0f;
        downTimer = 0f;
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