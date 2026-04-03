package render3d;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import main.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;

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
        flyCam.setMoveSpeed(20);

        final long windowHandle = ((com.jme3.system.lwjgl.LwjglWindow) context).getWindowHandle();

        GLFW.glfwSetWindowCloseCallback(windowHandle, handle -> {
            if (Main.BALANCE_FRAME != null) {
                Main.BALANCE_FRAME.dispose();
            }
        });

        DungeonRenderer3D.renderDungeon();
        PlayerRenderer3D.init();
    }

    @Override
    public void simpleUpdate(float tpf) {
        Main.CONTROLLER.update();
        Main.PLAYER.update();
        Main.CAMERA.update(Main.PLAYER);

        PlayerRenderer3D.update();
    }
}