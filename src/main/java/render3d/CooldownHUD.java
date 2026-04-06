// ===== render3d/CooldownHUD.java =====
package render3d;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import entity.PlayerActions;
import main.Main;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Two radial cooldown indicators, bottom-center of screen.
 *
 * Icon images:
 *   assets/Textures/hud/icon_dodge.png   (64x64)
 *   assets/Textures/hud/icon_parry.png   (64x64)
 */
public final class CooldownHUD {

    private static final float RING_SIZE     = 64f;
    private static final float ICON_SIZE     = 40f;
    private static final float GAP           = 16f;
    private static final float BOTTOM_MARGIN = 20f;
    private static final int   SEGMENTS      = 64;

    private static final ColorRGBA RING_TRACK  = new ColorRGBA(0.08f, 0.06f, 0.06f, 0.75f);
    private static final ColorRGBA RING_READY  = new ColorRGBA(0.72f, 0.58f, 0.22f, 1.00f);
    private static final ColorRGBA RING_FILL   = new ColorRGBA(0.55f, 0.14f, 0.14f, 1.00f);
    private static final ColorRGBA ICON_BRIGHT = new ColorRGBA(1.00f, 1.00f, 1.00f, 0.90f);
    private static final ColorRGBA ICON_DIM    = new ColorRGBA(0.35f, 0.35f, 0.35f, 0.70f);

    private final RadialRing dodgeRing;
    private final RadialRing parryRing;
    private final Geometry   dodgeIcon;
    private final Geometry   parryIcon;

    public CooldownHUD(final Node guiNode, final AssetManager assets,
                       final float screenW, final float screenH) {

        final float totalW = RING_SIZE * 2f + GAP;
        final float startX = screenW / 2f - totalW / 2f;
        final float baseY  = BOTTOM_MARGIN;

        final float dodgeCX = startX + RING_SIZE / 2f;
        final float parryCX = startX + RING_SIZE + GAP + RING_SIZE / 2f;
        final float ringCY  = baseY + RING_SIZE / 2f;

        dodgeRing = new RadialRing(assets, RING_SIZE, SEGMENTS);
        parryRing = new RadialRing(assets, RING_SIZE, SEGMENTS);

        dodgeRing.getNode().setLocalTranslation(
                dodgeCX - RING_SIZE / 2f, baseY, 0f);
        parryRing.getNode().setLocalTranslation(
                parryCX - RING_SIZE / 2f, baseY, 0f);

        dodgeIcon = buildIcon(assets, "Textures/hud/icon_dodge.png");
        parryIcon = buildIcon(assets, "Textures/hud/icon_parry.png");

        dodgeIcon.setLocalTranslation(
                dodgeCX - ICON_SIZE / 2f, ringCY - ICON_SIZE / 2f, 1f);
        parryIcon.setLocalTranslation(
                parryCX - ICON_SIZE / 2f, ringCY - ICON_SIZE / 2f, 1f);

        guiNode.attachChild(dodgeRing.getNode());
        guiNode.attachChild(parryRing.getNode());
        guiNode.attachChild(dodgeIcon);
        guiNode.attachChild(parryIcon);
    }

    public void update() {
        final PlayerActions a     = Main.PLAYER.getActions();
        final float         dodge = a.getDodgeCooldownFraction();
        final float         parry = a.getParryCooldownFraction();

        dodgeRing.setFraction(dodge, dodge >= 1f ? RING_READY : RING_FILL);
        parryRing.setFraction(parry, parry >= 1f ? RING_READY : RING_FILL);

        dodgeIcon.getMaterial().setColor(
                "Color", dodge >= 1f ? ICON_BRIGHT : ICON_DIM);
        parryIcon.getMaterial().setColor(
                "Color", parry >= 1f ? ICON_BRIGHT : ICON_DIM);
    }

    private static Geometry buildIcon(final AssetManager assets, final String path) {
        final Geometry geo = new Geometry("Icon", new Quad(ICON_SIZE, ICON_SIZE));
        final Material mat = new Material(
                assets, "Common/MatDefs/Misc/Unshaded.j3md");
        try {
            final Texture tex = assets.loadTexture(path);
            mat.setTexture("ColorMap", tex);
        } catch (final Exception ignored) {}
        mat.setColor("Color", ICON_BRIGHT);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        return geo;
    }

    // =========================================================
    //  RadialRing — dark track disc + arc fill disc
    // =========================================================

    private static final class RadialRing {

        private final Node     node   = new Node("Ring");
        private final Geometry fill;
        private final float    radius;
        private final int      segments;

        RadialRing(final AssetManager assets, final float size, final int segs) {
            this.radius   = size / 2f;
            this.segments = segs;

            // Dark background disc — always full circle
            final Geometry bg = new Geometry("Track", buildArc(segments, segments));
            final Material bgMat = flatMat(assets, RING_TRACK);
            bg.setMaterial(bgMat);
            bg.setLocalTranslation(radius, radius, 0f);
            node.attachChild(bg);

            // Fill arc — starts empty
            fill = new Geometry("Fill", buildArc(0, segments));
            fill.setMaterial(flatMat(assets, RING_FILL));
            fill.setLocalTranslation(radius, radius, 0.5f);
            node.attachChild(fill);
        }

        void setFraction(final float fraction, final ColorRGBA color) {
            fill.getMaterial().setColor("Color", color);
            final int steps = Math.round(FastMath.clamp(fraction, 0f, 1f) * segments);
            fill.setMesh(buildArc(steps, segments));
        }

        Node getNode() { return node; }

        /**
         * Builds a pie/arc mesh.
         * steps  = how many segments to fill (0 = empty, totalSegs = full circle)
         * totalSegs = total resolution of the circle
         *
         * Starts at 12 o'clock, sweeps clockwise.
         * Center vertex at (0,0). Rim verts at radius.
         */
        private Mesh buildArc(final int steps, final int totalSegs) {
            if (steps <= 0) {
                // Empty mesh — no triangles
                final Mesh m = new Mesh();
                m.setBuffer(VertexBuffer.Type.Position, 3,
                        BufferUtils.createFloatBuffer(new float[]{ 0f, 0f, 0f }));
                m.setBuffer(VertexBuffer.Type.Normal,   3,
                        BufferUtils.createFloatBuffer(new float[]{ 0f, 0f, 1f }));
                m.setBuffer(VertexBuffer.Type.TexCoord, 2,
                        BufferUtils.createFloatBuffer(new float[]{ 0.5f, 0.5f }));
                m.setBuffer(VertexBuffer.Type.Index,    3,
                        BufferUtils.createShortBuffer(new short[0]));
                m.updateBound();
                return m;
            }

            // vertices: 1 center + (steps+1) rim verts
            final int vertCount = steps + 2;

            final FloatBuffer pos  = BufferUtils.createFloatBuffer(vertCount * 3);
            final FloatBuffer norm = BufferUtils.createFloatBuffer(vertCount * 3);
            final FloatBuffer uv   = BufferUtils.createFloatBuffer(vertCount * 2);
            final ShortBuffer idx  = BufferUtils.createShortBuffer(steps * 3);

            // Center
            pos.put(0f).put(0f).put(0f);
            norm.put(0f).put(0f).put(1f);
            uv.put(0.5f).put(0.5f);

            // Rim verts — 12 o'clock = +Y axis, sweeping clockwise
            for (int i = 0; i <= steps; i++) {
                final float angle = FastMath.TWO_PI * i / totalSegs - FastMath.HALF_PI;
                final float vx    =  FastMath.cos(angle) * radius;
                final float vy    =  FastMath.sin(angle) * radius;
                pos.put(vx).put(vy).put(0f);
                norm.put(0f).put(0f).put(1f);
                uv.put(0.5f + vx / radius * 0.5f)
                  .put(0.5f + vy / radius * 0.5f);
            }

            // Triangles: fan from center
            for (int i = 0; i < steps; i++) {
                idx.put((short) 0)
                   .put((short)(i + 1))
                   .put((short)(i + 2));
            }

            pos.flip(); norm.flip(); uv.flip(); idx.flip();

            final Mesh m = new Mesh();
            m.setBuffer(VertexBuffer.Type.Position, 3, pos);
            m.setBuffer(VertexBuffer.Type.Normal,   3, norm);
            m.setBuffer(VertexBuffer.Type.TexCoord, 2, uv);
            m.setBuffer(VertexBuffer.Type.Index,    3, idx);
            m.updateBound();
            return m;
        }

        private static Material flatMat(final AssetManager assets,
                                        final ColorRGBA color) {
            final Material mat = new Material(
                    assets, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            mat.getAdditionalRenderState().setBlendMode(
                    RenderState.BlendMode.Alpha);
            return mat;
        }
    }
}