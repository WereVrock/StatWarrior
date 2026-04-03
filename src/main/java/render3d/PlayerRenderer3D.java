package render3d;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import main.Main;

public final class PlayerRenderer3D {

    private static final float RADIUS = 0.3f;
    private static final float HEIGHT = 1.0f;
    private static final int RADIAL_SAMPLES = 16;

    private static Geometry playerGeo;

    private PlayerRenderer3D() {}

    public static void init() {
        // Cylinder is horizontal by default; axisSamples=2, closed=true, inverted=false
        Cylinder cylinder = new Cylinder(2, RADIAL_SAMPLES, RADIUS, HEIGHT, true);

        playerGeo = new Geometry("Player", cylinder);

        // Rotate 90 degrees around X to stand upright
        playerGeo.rotate((float) Math.PI / 2f, 0f, 0f);

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
                HEIGHT / 2f,
                Main.PLAYER.getY() / 32f
        );
    }
}