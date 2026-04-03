package render3d;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import main.Main;

public final class PlayerRenderer3D {

    private static Geometry playerGeo;

    private PlayerRenderer3D() {}

    public static void init() {

        Box box = new Box(0.3f, 0.5f, 0.3f);

        playerGeo = new Geometry("Player", box);

        Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );

        playerGeo.setMaterial(mat);

        GameApplication.APP.getRootNode().attachChild(playerGeo);
    }

    public static void update() {

        playerGeo.setLocalTranslation(
                Main.PLAYER.getX() / 32f,
                0.5f,
                Main.PLAYER.getY() / 32f
        );
    }
}