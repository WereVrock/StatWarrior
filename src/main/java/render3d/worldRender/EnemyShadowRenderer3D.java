package render3d.worldRender;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import dungeon.Dungeon;
import entity.enemy.Enemy;
import entity.enemy.EnemyManager;
import render3d.GameApplication;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders soft blob shadows under enemies using a procedurally generated
 * radial gradient texture (opaque center → transparent edge).
 *
 * The gradient is generated once and shared across all shadow quads.
 */
public final class EnemyShadowRenderer3D {

    private static final float FLOOR_Y       = 0.22f;  // slightly above hard shadow to avoid z-fighting
    private static final float SHADOW_SIZE   = 2.4f;   // a little larger than hard shadow for soft look
    private static final float CENTER_ALPHA  = 0.8f;  // max opacity at center
    private static final int   TEX_SIZE      = 64;     // gradient texture resolution (power of 2)

    private static final Map<Enemy, Geometry> shadowMap = new HashMap<>();

    /** Shared gradient texture — created once. */
    private static Texture2D gradientTexture;

    private EnemyShadowRenderer3D() {}

    public static void init(final EnemyManager manager) {
        shadowMap.clear();
        gradientTexture = buildGradientTexture();
    }

    public static void update(final EnemyManager manager) {
        // Create missing shadows
        for (final Enemy enemy : manager.getEnemies()) {
            if (!shadowMap.containsKey(enemy)) {
                shadowMap.put(enemy, createShadow());
            }
        }

        // Update positions
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

            // Quad vertices are centered at origin — no offset needed
            geo.setLocalTranslation(worldX, FLOOR_Y, worldZ);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static Geometry createShadow() {
        final Quad quad = new Quad(SHADOW_SIZE, SHADOW_SIZE);
        shiftQuadToCenter(quad);

        final Geometry geo = new Geometry("EnemySoftShadow", quad);

        final Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );

        // Use the gradient texture instead of a flat color
        mat.setTexture("ColorMap", gradientTexture);
        mat.setColor("Color", ColorRGBA.White); // tint white so texture drives the alpha
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        geo.setMaterial(mat);
        geo.setQueueBucket(RenderQueue.Bucket.Transparent);
        geo.setLocalRotation(buildFlatRotation());

        GameApplication.APP.getRootNode().attachChild(geo);
        return geo;
    }

    /**
     * Builds a TEX_SIZE×TEX_SIZE RGBA texture that is:
     *   - black + CENTER_ALPHA at the center
     *   - black + 0 alpha at the edges
     * using a smooth cosine falloff for a natural soft shadow look.
     */
    private static Texture2D buildGradientTexture() {
        final int size   = TEX_SIZE;
        final int pixels = size * size;
        final ByteBuffer buf = BufferUtils.createByteBuffer(pixels * 4); // RGBA8

        final float center = size / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Normalized distance from center [0, 1]
                final float dx   = (x - center) / center;
                final float dy   = (y - center) / center;
                final float dist = FastMath.sqrt(dx * dx + dy * dy);

                // Smooth falloff: 1 at center, 0 at edge, clamped
                final float t     = FastMath.clamp(1f - dist, 0f, 1f);
                // Cosine curve makes it feel softer than linear
                final float alpha = CENTER_ALPHA * ((FastMath.cos((1f - t) * FastMath.PI) + 1f) / 2f);

                buf.put((byte) 0);                        // R
                buf.put((byte) 0);                        // G
                buf.put((byte) 0);                        // B
                buf.put((byte) (alpha * 255f));           // A
            }
        }
        buf.flip();

        final Image img = new Image(
                Image.Format.RGBA8,
                size, size,
                buf,
                com.jme3.texture.image.ColorSpace.Linear
        );

        final Texture2D tex = new Texture2D(img);
        tex.setMagFilter(Texture2D.MagFilter.Bilinear);
        tex.setMinFilter(Texture2D.MinFilter.BilinearNoMipMaps);
        tex.setWrap(Texture2D.WrapMode.EdgeClamp);
        return tex;
    }

    /**
     * Shifts the Quad's vertex buffer so the quad is centered at the origin
     * rather than having its bottom-left corner at (0, 0).
     */
    private static void shiftQuadToCenter(final Quad quad) {
        final float half = SHADOW_SIZE / 2f;
        final FloatBuffer pos = quad.getFloatBuffer(com.jme3.scene.VertexBuffer.Type.Position);
        pos.rewind();
        for (int i = 0; i < 4; i++) {
            final float x = pos.get();
            final float y = pos.get();
            final float z = pos.get();
            pos.position(pos.position() - 3);
            pos.put(x - half);
            pos.put(y - half);
            pos.put(z);
        }
        quad.updateBound();
    }

    private static Quaternion buildFlatRotation() {
        final Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.HALF_PI, 0f, 0f);
        return q;
    }
}