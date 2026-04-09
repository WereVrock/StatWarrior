// ===== render3d/worldRender/EnemyShadowRenderer3D.java =====
package render3d.worldRender;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import entity.enemy.Enemy;
import entity.enemy.EnemyManager;
import render3d.GameApplication;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a soft black radial-gradient shadow disc on the floor under each enemy.
 * Shares the same procedural texture across all enemy shadow quads.
 */
public final class EnemyShadowRenderer3D {

    private static final float SHADOW_DIAMETER = 0.9f;
    private static final float SHADOW_Y        = 0.3f;   // ✅ lifted above floor to avoid z-fighting
    private static final float SHADOW_ALPHA    = 0.60f;
    private static final int   TEX_SIZE        = 64;

    private static final Quaternion FLAT_ROT = buildFlatRotation();

    private static final List<Geometry> shadowGeos = new ArrayList<>();

    private EnemyShadowRenderer3D() {}

    public static void init(final EnemyManager manager) {
        shadowGeos.clear();

        final Texture2D sharedTex = buildRadialGradientTexture();

        for (final Enemy enemy : manager.getEnemies()) {
            final Quad quad = new Quad(SHADOW_DIAMETER, SHADOW_DIAMETER);
            final Geometry geo = new Geometry("EnemyShadow_" + enemy.hashCode(), quad);

            final Material mat = new Material(
                    GameApplication.APP.getAssetManager(),
                    "Common/MatDefs/Misc/Unshaded.j3md"
            );

            mat.setTexture("ColorMap", sharedTex);

            // ✅ Proper transparency setup
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            mat.getAdditionalRenderState().setDepthWrite(false);
            mat.getAdditionalRenderState().setDepthTest(true);
            mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

            geo.setMaterial(mat);
            geo.setLocalRotation(FLAT_ROT);

            // ✅ Critical for transparency sorting
            geo.setQueueBucket(RenderQueue.Bucket.Transparent);

            GameApplication.APP.getRootNode().attachChild(geo);
            shadowGeos.add(geo);
        }
    }

    public static void update(final EnemyManager manager) {
        final List<Enemy> enemies = manager.getEnemies();
        final float half = SHADOW_DIAMETER / 2f;

        for (int i = 0; i < enemies.size(); i++) {
            final Enemy enemy = enemies.get(i);
            final Geometry geo = shadowGeos.get(i);

            if (enemy.getHealth().isDeathAnimDone() || enemy.getHealth().isDead()) {
                geo.setCullHint(Spatial.CullHint.Always);
                continue;
            }

            geo.setCullHint(Spatial.CullHint.Inherit);

            final float worldX = enemy.getX() / 32f;
            final float worldZ = enemy.getY() / 32f;

            geo.setLocalTranslation(
                    new Vector3f(worldX - half, SHADOW_Y, worldZ - half)
            );
        }
    }

    // -------------------------------------------------------------------------
    // Texture helpers
    // -------------------------------------------------------------------------

    private static Texture2D buildRadialGradientTexture() {
        final int size = TEX_SIZE;
        final float centre = size / 2f;

        final ByteBuffer buf = BufferUtils.createByteBuffer(size * size * 4);

        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                final float dx = (px + 0.5f) - centre;
                final float dy = (py + 0.5f) - centre;
                final float dist = (float) Math.sqrt(dx * dx + dy * dy);
                final float t = FastMath.clamp(dist / centre, 0f, 1f);

                // ✅ slightly smoother falloff
                final float alpha = SHADOW_ALPHA * FastMath.pow(1f - t, 3f);

                buf.put((byte) 0);
                buf.put((byte) 0);
                buf.put((byte) 0);
                buf.put((byte) Math.round(alpha * 255f));
            }
        }

        buf.flip();

        final Image img = new Image(
                Image.Format.RGBA8,
                size,
                size,
                buf,
                com.jme3.texture.image.ColorSpace.Linear
        );

        final Texture2D tex = new Texture2D(img);
        tex.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);
        tex.setMinFilter(com.jme3.texture.Texture.MinFilter.BilinearNoMipMaps);

        return tex;
    }

    private static Quaternion buildFlatRotation() {
        final Quaternion q = new Quaternion();
        q.fromAngles(FastMath.HALF_PI, 0f, 0f);
        return q;
    }
}