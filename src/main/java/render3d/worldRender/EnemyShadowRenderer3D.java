package render3d.worldRender;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import dungeon.Dungeon;
import entity.enemy.Enemy;
import entity.enemy.EnemyManager;
import render3d.GameApplication;

import java.util.HashMap;
import java.util.Map;

public final class EnemyShadowRenderer3D {

    private static final float FLOOR_Y       = 0.21f;
    private static final float SHADOW_HEIGHT = 0.01f;
    private static final int   RADIAL_SAMPLES = 32;

    // SAME LOGIC AS PLAYER
    private static final float COLLISION_RADIUS_WORLD =
            (Dungeon.TILE_SIZE * 2f) / Dungeon.TILE_SIZE / 2f; // = 1.0

    private static final ColorRGBA SHADOW_COLOR =
            new ColorRGBA(0f, 0f, 0f, 0.6f);

    private static final Quaternion FLAT_ROTATION = buildFlatRotation();

    private static final Map<Enemy, Geometry> shadowMap = new HashMap<>();

    private EnemyShadowRenderer3D() {}

    public static void init(final EnemyManager manager) {
        shadowMap.clear();
    }

    public static void update(final EnemyManager manager) {

        // === CREATE missing shadows ===
        for (final Enemy enemy : manager.getEnemies()) {
            if (!shadowMap.containsKey(enemy)) {
                shadowMap.put(enemy, createShadow());
            }
        }

        // === UPDATE ===
        for (final Map.Entry<Enemy, Geometry> entry : shadowMap.entrySet()) {

            final Enemy enemy = entry.getKey();
            final Geometry geo = entry.getValue();

            if (enemy.getHealth().isDead() || enemy.getHealth().isDeathAnimDone()) {
                geo.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                continue;
            }

            geo.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);

            final float worldX = enemy.getX() / Dungeon.TILE_SIZE;
            final float worldZ = enemy.getY() / Dungeon.TILE_SIZE;

            geo.setLocalTranslation(new Vector3f(worldX, FLOOR_Y, worldZ));
        }
    }

    private static Geometry createShadow() {

        final Cylinder disc = new Cylinder(
                2,
                RADIAL_SAMPLES,
                COLLISION_RADIUS_WORLD,
                SHADOW_HEIGHT,
                true
        );

        final Geometry geo = new Geometry("EnemyShadow", disc);

        final Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );

        mat.setColor("Color", SHADOW_COLOR);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false);

        geo.setMaterial(mat);
        geo.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        geo.setLocalRotation(FLAT_ROTATION);

        GameApplication.APP.getRootNode().attachChild(geo);

        return geo;
    }

    private static Quaternion buildFlatRotation() {
        final Quaternion q = new Quaternion();
        q.fromAngles(com.jme3.math.FastMath.HALF_PI, 0f, 0f);
        return q;
    }
}