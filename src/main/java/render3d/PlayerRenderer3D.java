// ===== render3d/PlayerRenderer3D.java =====
package render3d;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import main.Main;

public final class PlayerRenderer3D {

    private static final float  WIDTH        = 1.0f;
    private static final float  HEIGHT       = 1.0f;
    private static final String TEXTURE_PATH = "Textures/sprites/player.png";

    private static Geometry playerGeo;

    private PlayerRenderer3D() {}

    public static void init() {
        final Quad quad = new Quad(WIDTH, HEIGHT);
        playerGeo = new Geometry("Player", quad);

        final Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        final Texture tex = GameApplication.APP.getAssetManager().loadTexture(TEXTURE_PATH);
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        playerGeo.setMaterial(mat);
        GameApplication.APP.getRootNode().attachChild(playerGeo);
    }

    public static void update() {
        BillboardRenderer3D.faceCamera(playerGeo,
                Main.PLAYER.getX(), Main.PLAYER.getY(),
                Main.PLAYER.getBobOffset());
    }
}