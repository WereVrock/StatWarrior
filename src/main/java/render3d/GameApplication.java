package render3d;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import main.Main;

public final class GameApplication extends SimpleApplication {

    public static GameApplication APP;

    public GameApplication() {
        APP = this;
    }

    public static void startApp() {
        GameApplication app = new GameApplication();

        AppSettings settings = new AppSettings(true);
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
        Main.THIRD_PERSON_CAMERA.init(cam);
    }

    @Override
    public void simpleUpdate(float tpf) {
        Main.CONTROLLER.update();
        Main.PLAYER.update();
        Main.CAMERA.update(Main.PLAYER);

        PlayerRenderer3D.update();
        Main.THIRD_PERSON_CAMERA.update(cam);
    }
}