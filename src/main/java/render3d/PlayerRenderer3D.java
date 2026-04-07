package render3d;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import dungeon.Dungeon;
import main.Main;

/**
 * Renders a flat blue ellipse on the floor directly under the player.
 * The ellipse matches the player's collision box exactly:
 *   width  = tileSize * 2 pixels = 2 world units
 *   depth  = tileSize * 2 pixels = 2 world units
 *
 * Serves as both a collision debug tool and a gameplay position indicator.
 */
public final class PlayerRenderer3D {

    private static final float  FLOOR_Y        = .21f; // just above floor surface
    private static final float  SHADOW_HEIGHT  = 0.01f; // flat disc thickness
    private static final int    RADIAL_SAMPLES = 32;

    // Player collision is tileSize*2 pixels. 1 world unit = TILE_SIZE pixels.
    // So collision diameter in world units = (tileSize*2) / TILE_SIZE = 2.0
    private static final float  COLLISION_RADIUS_WORLD = (Dungeon.TILE_SIZE * 2f) / Dungeon.TILE_SIZE / 2f; // = 1.0

    private static final ColorRGBA SHADOW_COLOR = new ColorRGBA(0.1f, 0.3f, 1.0f, 0.6f);

    // Cylinder is oriented along Y by default; rotate it flat onto the XZ plane
    private static final Quaternion FLAT_ROTATION = buildFlatRotation();

    private static Geometry shadowGeo;

    private PlayerRenderer3D() {}

    public static void init() {
        // Cylinder(radius, height, axisSamples, radialSamples, closed)
        final Cylinder disc = new Cylinder(
                2, RADIAL_SAMPLES,
                COLLISION_RADIUS_WORLD,
                SHADOW_HEIGHT,
                true
        );

        shadowGeo = new Geometry("PlayerShadow", disc);

        final Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", SHADOW_COLOR);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        shadowGeo.setMaterial(mat);
        shadowGeo.setLocalRotation(FLAT_ROTATION);

        GameApplication.APP.getRootNode().attachChild(shadowGeo);
    }

    public static void update() {
        // Player.getX()/getY() are top-left of the collision box.
        // centerX/Y add half the box size to get the middle.
        final float worldX = Main.PLAYER.centerX() / Dungeon.TILE_SIZE;
        final float worldZ = Main.PLAYER.centerY() / Dungeon.TILE_SIZE;

        shadowGeo.setLocalTranslation(new Vector3f(worldX, FLOOR_Y, worldZ));
    }

    private static Quaternion buildFlatRotation() {
        final Quaternion q = new Quaternion();
        q.fromAngles(com.jme3.math.FastMath.HALF_PI, 0f, 0f);
        return q;
    }
}