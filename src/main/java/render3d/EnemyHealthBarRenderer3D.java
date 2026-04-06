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
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import entity.Enemy;
import entity.EnemyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * World-space health bars floating above each enemy sprite.
 * Each bar billboards to face the camera.
 */
public final class EnemyHealthBarRenderer3D {

    private static final float BAR_WIDTH   = 0.8f;
    private static final float BAR_HEIGHT  = 0.07f;
    private static final float BAR_Y_ABOVE = 1.15f; // world units above sprite base
    private static final float Z_OFFSET    = 0.01f; // fill in front of background

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

            final float geoX = enemy.getX() / 32f;
            final float geoZ = enemy.getY() / 32f;
            final float geoY = BAR_Y_ABOVE;

            // Billboard yaw toward camera
            final float dx  = camPos.x - geoX;
            final float dz  = camPos.z - geoZ;
            final float yaw = FastMath.atan2(dx, dz);

            final Quaternion rot = new Quaternion();
            rot.fromAngles(0f, yaw, 0f);

            // Center background
            final float rightX = FastMath.cos(yaw);
            final float rightZ = -FastMath.sin(yaw);

            final Vector3f bgPos = new Vector3f(
                    geoX - rightX * BAR_WIDTH / 2f,
                    geoY,
                    geoZ - rightZ * BAR_WIDTH / 2f);

            bar.bg.setLocalTranslation(bgPos);
            bar.bg.setLocalRotation(rot);

            // Fill — scaled to health fraction, anchored left
            final float fraction = enemy.getHealth().getFraction();
            final float fillW    = BAR_WIDTH * fraction;

            bar.fill.setLocalScale(fraction, 1f, 1f);

            final Vector3f fillPos = new Vector3f(
                    geoX - rightX * BAR_WIDTH / 2f,
                    geoY + Z_OFFSET,
                    geoZ - rightZ * BAR_WIDTH / 2f);

            // Shift fill right by half of missing width so it stays left-anchored
            final float shift = (BAR_WIDTH - fillW) / 2f * (fraction - 1f);
            fillPos.x += rightX * (BAR_WIDTH * (1f - fraction) / 2f) * -1f;
            fillPos.z += rightZ * (BAR_WIDTH * (1f - fraction) / 2f) * -1f;

            bar.fill.setLocalTranslation(fillPos);
            bar.fill.setLocalRotation(rot);

            // Tint dead enemies gray
            if (enemy.getHealth().isDead()) {
                bar.fill.getMaterial().setColor("Color", DEAD_COLOR);
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