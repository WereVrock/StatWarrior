package render3d;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import entity.enemy.Enemy;
import entity.enemy.EnemyManager;

import java.util.ArrayList;
import java.util.List;

public final class EnemyShadowRenderer3D {

    private static final float SHADOW_WIDTH   = 0.7f;
    private static final float SHADOW_HEIGHT  = 0.35f;
    private static final float SHADOW_Y       = 0.01f; // just above floor to avoid z-fighting
    private static final float SHADOW_ALPHA   = 0.45f;

    private static final Quaternion FLAT_ROT;

    static {
        FLAT_ROT = new Quaternion();
        FLAT_ROT.fromAngles(-com.jme3.math.FastMath.HALF_PI, 0f, 0f);
    }

    private static final List<Geometry> shadowGeos = new ArrayList<>();

    private EnemyShadowRenderer3D() {}

    public static void init(final EnemyManager manager) {
        shadowGeos.clear();

        for (final Enemy enemy : manager.getEnemies()) {
            final Quad     quad = new Quad(SHADOW_WIDTH, SHADOW_HEIGHT);
            final Geometry geo  = new Geometry("Shadow_" + enemy.hashCode(), quad);

            final Material mat = new Material(
                    GameApplication.APP.getAssetManager(),
                    "Common/MatDefs/Misc/Unshaded.j3md"
            );
            mat.setColor("Color", new ColorRGBA(0f, 0f, 0f, SHADOW_ALPHA));
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            mat.getAdditionalRenderState().setDepthWrite(false);

            geo.setMaterial(mat);
            geo.setLocalRotation(FLAT_ROT);

            GameApplication.APP.getRootNode().attachChild(geo);
            shadowGeos.add(geo);
        }
    }

    public static void update(final EnemyManager manager) {
        final List<Enemy> enemies = manager.getEnemies();

        for (int i = 0; i < enemies.size(); i++) {
            final Enemy    enemy = enemies.get(i);
            final Geometry geo   = shadowGeos.get(i);

            if (enemy.getHealth().isDeathAnimDone() || enemy.getHealth().isDead()) {
                geo.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                continue;
            }

            geo.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);

            final float worldX = enemy.getX() / 32f
                    + (1.0f - SHADOW_WIDTH)  / 2f  // center under sprite
                    + SHADOW_WIDTH / 2f;
            final float worldZ = enemy.getY() / 32f
                    + (1.0f - SHADOW_HEIGHT) / 2f;

            geo.setLocalTranslation(new Vector3f(
                    worldX - SHADOW_WIDTH / 2f,
                    SHADOW_Y,
                    worldZ
            ));
        }
    }
}