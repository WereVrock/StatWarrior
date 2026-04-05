package render3d;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import main.Main;

public final class GameApplication extends SimpleApplication {

    public static GameApplication APP;

    private HitFlash hitFlash;

    public GameApplication() {
        APP = this;
    }

    public static void startApp() {
        final GameApplication app = new GameApplication();
        final AppSettings settings = new AppSettings(true);
        settings.setTitle("Modular Dungeon 3D");
        settings.setResolution(800, 600);
        app.setSettings(settings);
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
    }

    @Override
    public void simpleUpdate(final float tpf) {
        Main.CONTROLLER.update();
        Main.PLAYER.update();
        Main.CAMERA.update(Main.PLAYER);

        Main.ENEMY_MANAGER.update(tpf);

        PlayerRenderer3D.update();
        EnemyRenderer3D.update(Main.ENEMY_MANAGER);
        ProjectileRenderer3D.update(Main.ENEMY_MANAGER.getProjectileManager());
        Main.THIRD_PERSON_CAMERA.update(cam);

        hitFlash.update(tpf);
    }
}