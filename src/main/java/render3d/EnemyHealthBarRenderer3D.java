// ===== render3d/EnemyHealthBarRenderer3D.java =====
package render3d;

import com.jme3.asset.AssetManager;
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

/**
 * World-space health bars floating above each enemy sprite, always facing
 * the camera and rendered in front of the sprite via a Z offset toward the
 * camera.
 */
public final class EnemyHealthBarRenderer3D {

    private static final float BAR_WIDTH    = 0.8f;
    private static final float BAR_HEIGHT   = 0.07f;
    /** How far above the sprite base the bar floats. */
    private static final float BAR_Y_ABOVE  = 1.25f;
    /** Push bar toward the camera by this amount so it never clips the sprite. */
    private static final float CAM_OFFSET   = 0.00f;
    /** Fill is rendered slightly in front of background in the bar's local plane. */
    private static final float FILL_Z       = 0.005f;

    private static final ColorRGBA BG_COLOR   = new ColorRGBA(0.10f, 0.05f, 0.05f, 0.85f);
    private static final ColorRGBA FILL_COLOR = new ColorRGBA(0.70f, 0.12f, 0.12f, 1.00f);
    private static final ColorRGBA DEAD_COLOR = new ColorRGBA(0.20f, 0.20f, 0.20f, 0.50f);

    private static final class Bar {
        final Geometry bg;
        final Geometry fill;

        Bar(final Geometry bg, final Geometry fill) {
            this.bg   = bg;
            this.fill = fill;
        }
    }

    private final List<Bar> bars = new ArrayList<>();

    public void init(final EnemyManager manager, final AssetManager assets) {
        bars.clear();
        for (final Enemy enemy : manager.getEnemies()) {
            final Geometry bg   = buildQuad(assets, BAR_WIDTH, BAR_HEIGHT, BG_COLOR);
            final Geometry fill = buildQuad(assets, BAR_WIDTH, BAR_HEIGHT, FILL_COLOR);
            GameApplication.APP.getRootNode().attachChild(bg);
            GameApplication.APP.getRootNode().attachChild(fill);
            bars.add(new Bar(bg, fill));
        }
    }

    public void update(final EnemyManager manager) {
        final List<Enemy> enemies = manager.getEnemies();
        final Vector3f    camPos  = GameApplication.APP.getCamera().getLocation();

        for (int i = 0; i < enemies.size(); i++) {
            final Enemy enemy = enemies.get(i);
            final Bar   bar   = bars.get(i);

            // Hide bars for enemies whose death animation is complete
            final boolean hide = enemy.getHealth().isDeathAnimDone();
            final com.jme3.scene.Spatial.CullHint hint =
                    hide ? com.jme3.scene.Spatial.CullHint.Always
                         : com.jme3.scene.Spatial.CullHint.Inherit;
            bar.bg.setCullHint(hint);
            bar.fill.setCullHint(hint);
            if (hide) continue;

            final float geoX = enemy.getX() / 32f + 0.5f; // sprite center X
            final float geoZ = enemy.getY() / 32f + 0.5f; // sprite center Z
            final float geoY = BAR_Y_ABOVE;

            // Direction from sprite to camera (horizontal only)
            final float toCamX = camPos.x - geoX;
            final float toCamZ = camPos.z - geoZ;
            final float toCamLen = (float) Math.sqrt(toCamX * toCamX + toCamZ * toCamZ);
            final float normCamX = toCamLen > 0.001f ? toCamX / toCamLen : 0f;
            final float normCamZ = toCamLen > 0.001f ? toCamZ / toCamLen : 1f;

            // Billboard yaw — bar faces camera
            final float yaw = FastMath.atan2(toCamX, toCamZ);
            final Quaternion rot = new Quaternion();
            rot.fromAngles(0f, yaw, 0f);

            // Right vector in world space (perpendicular to cam direction, horizontal)
            final float rightX =  FastMath.cos(yaw);
            final float rightZ = -FastMath.sin(yaw);

            // Push the bar slightly toward the camera so it is always in front
            final float nudgeX = normCamX * CAM_OFFSET;
            final float nudgeZ = normCamZ * CAM_OFFSET;

            // Background: left edge anchored, full width
            final Vector3f bgPos = new Vector3f(
                    geoX - rightX * BAR_WIDTH / 2f + nudgeX,
                    geoY,
                    geoZ - rightZ * BAR_WIDTH / 2f + nudgeZ);
            bar.bg.setLocalTranslation(bgPos);
            bar.bg.setLocalRotation(rot);

            // Fill: left-anchored, scaled to health fraction
            final float fraction = enemy.getHealth().getFraction();
            bar.fill.setLocalScale(Math.max(0f, fraction), 1f, 1f);

            // The Quad origin is at its left edge, so the fill left edge = bg left edge.
            // We push it slightly in front of the background along the cam direction.
            final Vector3f fillPos = new Vector3f(
                    bgPos.x + normCamX * FILL_Z,
                    geoY,
                    bgPos.z + normCamZ * FILL_Z);
            bar.fill.setLocalTranslation(fillPos);
            bar.fill.setLocalRotation(rot);

            // Dead tint
            if (enemy.getHealth().isDead()) {
                bar.fill.getMaterial().setColor("Color", DEAD_COLOR);
            } else {
                bar.fill.getMaterial().setColor("Color", FILL_COLOR);
            }
        }
    }

    private static Geometry buildQuad(final AssetManager assets,
                                      final float w, final float h,
                                      final ColorRGBA color) {
        final Geometry geo = new Geometry("HealthBar", new Quad(w, h));
        final Material mat = new Material(
                assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        return geo;
    }
}