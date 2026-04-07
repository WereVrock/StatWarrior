package render3d.worldRender;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import entity.enemy.Enemy;
import entity.enemy.EnemyManager;

import java.util.ArrayList;
import java.util.List;
import render3d.GameApplication;

public final class EnemyShadowRenderer3D {

    private static final float SHADOW_WIDTH   = 0.65f;
    private static final float SHADOW_HEIGHT  = 0.22f;
    private static final float SHADOW_Y       = 0.21f; // just above floor top (floor top = 0.2f)
    private static final float SHADOW_ALPHA   = 0.55f;

    // Must match BillboardRenderer3D constants
    private static final float SPRITE_WIDTH   = 1.0f;

    private static final Quaternion FLAT_ROT;

    static {
        FLAT_ROT = new Quaternion();
        FLAT_ROT.fromAngles(-FastMath.HALF_PI, 0f, 0f);
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

            // Mirror BillboardRenderer3D.faceCamera positioning exactly —
            // same world origin, same rightX/rightZ shift, flat on floor
            final float geoX = enemy.getX() / 32f;
            final float geoZ = enemy.getY() / 32f;

            // Yaw toward camera (same formula as BillboardRenderer3D)
            final Vector3f camPos = GameApplication.APP.getCamera().getLocation();
            final float dx  = camPos.x - geoX;
            final float dz  = camPos.z - geoZ;
            final float yaw = FastMath.atan2(dx, dz);

            // Center the shadow under the sprite using same right-axis shift
            final float rightX = FastMath.cos(yaw);
            final float rightZ = -FastMath.sin(yaw);

            final float centerX = geoX - rightX * (SPRITE_WIDTH / 2f)
                    + rightX * (SPRITE_WIDTH / 2f)   // cancel sprite shift
                    - rightX * (SHADOW_WIDTH  / 2f);  // center shadow
            final float centerZ = geoZ - rightZ * (SPRITE_WIDTH / 2f)
                    + rightZ * (SPRITE_WIDTH / 2f)
                    - rightZ * (SHADOW_WIDTH  / 2f);

            geo.setLocalTranslation(new Vector3f(centerX, SHADOW_Y, centerZ));

            // Rotate flat on floor, facing same yaw so it aligns with sprite footprint
            final Quaternion rot = new Quaternion();
            // Flat (X rotation) then yaw to align with billboard facing
            final Quaternion flatRot = FLAT_ROT.clone();
            final Quaternion yawRot  = new Quaternion();
            yawRot.fromAngles(0f, yaw, 0f);
            rot.set(yawRot.mult(flatRot));
            geo.setLocalRotation(rot);
        }
    }
}