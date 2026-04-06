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
 * the camera. Position matches the sprite exactly (same logic as BillboardRenderer3D)
 * with a vertical offset so the bar floats just above the sprite top without touching.
 */
public final class EnemyHealthBarRenderer3D {

    private static final float BAR_WIDTH   = 0.8f;
    private static final float BAR_HEIGHT  = 0.07f;

    /** Sprite height in world units (must match BillboardRenderer3D / EnemyRenderer3D). */
    private static final float SPRITE_HEIGHT = 1.0f;

    /** Gap between sprite top and bar bottom, in world units. */
    private static final float BAR_GAP     = 0.12f;

    /** Y position = sprite top + gap. Sprite sits on FLOOR_TOP_Y. */
    private static final float FLOOR_TOP_Y = 0.2f;
    private static final float BAR_Y       = FLOOR_TOP_Y + SPRITE_HEIGHT + BAR_GAP;

    /** Fill is rendered slightly in front of background in the bar's local plane. */
    private static final float FILL_Z      = 0.005f;

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

            final boolean hide = enemy.getHealth().isDeathAnimDone();
            final com.jme3.scene.Spatial.CullHint hint =
                    hide ? com.jme3.scene.Spatial.CullHint.Always
                         : com.jme3.scene.Spatial.CullHint.Inherit;
            bar.bg.setCullHint(hint);
            bar.fill.setCullHint(hint);
            if (hide) continue;

            // Mirror BillboardRenderer3D.faceCamera() to get the exact same world position
            // as the sprite, then raise to BAR_Y.
            final float geoX = enemy.getX() / 32f;
            final float geoZ = enemy.getY() / 32f;

            final float dx  = camPos.x - geoX;
            final float dz  = camPos.z - geoZ;
            final float yaw = FastMath.atan2(dx, dz);

            final Quaternion rot = new Quaternion();
            rot.fromAngles(0f, yaw, 0f);

            // Sprite center X in world space (sprite left edge = geoX - rightX*halfW)
            // Bar should be centered on sprite, so we use sprite center = geoX + halfW offset
            final float rightX =  FastMath.cos(yaw);
            final float rightZ = -FastMath.sin(yaw);

            // Sprite left edge world pos (same as BillboardRenderer3D)
            final float spriteLeftX = geoX - rightX * (BAR_WIDTH / 2f);
            final float spriteLeftZ = geoZ - rightZ * (BAR_WIDTH / 2f);

            // Camera-facing direction (normalized, horizontal)
            final float toCamLen = (float) Math.sqrt(dx * dx + dz * dz);
            final float normCamX = toCamLen > 0.001f ? dx / toCamLen : 0f;
            final float normCamZ = toCamLen > 0.001f ? dz / toCamLen : 1f;

            // Background: anchored at bar left edge, at BAR_Y height
            final Vector3f bgPos = new Vector3f(spriteLeftX, BAR_Y, spriteLeftZ);
            bar.bg.setLocalTranslation(bgPos);
            bar.bg.setLocalRotation(rot);

            // Fill: same anchor, scaled to health fraction, nudged toward camera
            final float fraction = enemy.getHealth().getFraction();
            bar.fill.setLocalScale(Math.max(0f, fraction), 1f, 1f);

            final Vector3f fillPos = new Vector3f(
                    bgPos.x + normCamX * FILL_Z,
                    BAR_Y,
                    bgPos.z + normCamZ * FILL_Z);
            bar.fill.setLocalTranslation(fillPos);
            bar.fill.setLocalRotation(rot);

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