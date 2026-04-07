// ===== render3d/CooldownHUD.java =====
package render3d.screenRendering;

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
import entity.player.PlayerActions;
import main.Main;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Two radial cooldown indicators, bottom-center of screen.
 * Parry icon has a green circle that drains clockwise as the parry duration expires.
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

    /** Radius of the parry duration circle indicator, in pixels. */
    private static final float PARRY_CIRCLE_RADIUS = RING_SIZE / 2f + 6f;

    /** Thickness of the parry circle stroke in pixels. */
    private static final float PARRY_CIRCLE_THICK  = 4f;

    private static final ColorRGBA RING_TRACK   = new ColorRGBA(0.08f, 0.06f, 0.06f, 0.75f);
    private static final ColorRGBA RING_READY   = new ColorRGBA(0.72f, 0.58f, 0.22f, 1.00f);
    private static final ColorRGBA RING_FILL    = new ColorRGBA(0.55f, 0.14f, 0.14f, 1.00f);
    private static final ColorRGBA ICON_BRIGHT  = new ColorRGBA(1.00f, 1.00f, 1.00f, 0.90f);
    private static final ColorRGBA ICON_DIM     = new ColorRGBA(0.35f, 0.35f, 0.35f, 0.70f);
    private static final ColorRGBA PARRY_ACTIVE = new ColorRGBA(0.20f, 0.90f, 0.30f, 1.00f);

    private final RadialRing dodgeRing;
    private final RadialRing parryRing;
    private final Geometry   dodgeIcon;
    private final Geometry   parryIcon;

    /** Clockwise-draining circle arc rendered on top of the parry ring while parrying. */
    private final Geometry   parryCircle;
    private final float      parryCX;
    private final float      parryCY;
    private final Node       guiNode;
    private final AssetManager assets;

    public CooldownHUD(final Node guiNode, final AssetManager assets,
                       final float screenW, final float screenH) {
        this.guiNode = guiNode;
        this.assets  = assets;

        final float totalW = RING_SIZE * 2f + GAP;
        final float startX = screenW / 2f - totalW / 2f;
        final float baseY  = BOTTOM_MARGIN;

        final float dodgeCX = startX + RING_SIZE / 2f;
        parryCX             = startX + RING_SIZE + GAP + RING_SIZE / 2f;
        final float ringCY  = baseY + RING_SIZE / 2f;
        parryCY             = ringCY;

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

        // Parry duration circle — rebuilt every frame while parrying
        parryCircle = new Geometry("ParryCircle", buildCircleArc(0, SEGMENTS));
        final Material circleMat = new Material(
                assets, "Common/MatDefs/Misc/Unshaded.j3md");
        circleMat.setColor("Color", PARRY_ACTIVE);
        circleMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        parryCircle.setMaterial(circleMat);
        parryCircle.setLocalTranslation(parryCX, parryCY, 3f);
        parryCircle.setCullHint(com.jme3.scene.Spatial.CullHint.Always);

        guiNode.attachChild(dodgeRing.getNode());
        guiNode.attachChild(parryRing.getNode());
        guiNode.attachChild(dodgeIcon);
        guiNode.attachChild(parryIcon);
        guiNode.attachChild(parryCircle);
    }

    public void update() {
        final PlayerActions a     = Main.PLAYER.getActions();
        final float         dodge = a.getDodgeCooldownFraction();
        final float         parry = a.getParryCooldownFraction();
        final boolean       active = a.isParrying();

        dodgeRing.setFraction(dodge, dodge >= 1f ? RING_READY : RING_FILL);
        parryRing.setFraction(parry, parry >= 1f ? RING_READY : RING_FILL);

        dodgeIcon.getMaterial().setColor(
                "Color", dodge >= 1f ? ICON_BRIGHT : ICON_DIM);
        parryIcon.getMaterial().setColor(
                "Color", parry >= 1f ? ICON_BRIGHT : ICON_DIM);

        updateParryCircle(active, a.getParryDurationFraction());
    }

    /**
     * Rebuilds the parry duration circle arc each frame.
     * Starts full (clockwise from top) and drains clockwise as duration expires.
     * fraction=1.0 → full circle; fraction=0.0 → empty.
     */
    private void updateParryCircle(final boolean active, final float durationFraction) {
        if (!active) {
            parryCircle.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            return;
        }

        parryCircle.setCullHint(com.jme3.scene.Spatial.CullHint.Inherit);
        final int steps = Math.round(FastMath.clamp(durationFraction, 0f, 1f) * SEGMENTS);
        parryCircle.setMesh(buildCircleArc(steps, SEGMENTS));
    }

    /**
     * Builds a clockwise arc ring (annulus) starting from the top (12 o'clock).
     * Outer radius = PARRY_CIRCLE_RADIUS, inner radius = outer - PARRY_CIRCLE_THICK.
     * steps=0 → empty; steps=SEGMENTS → full circle.
     */
    private static Mesh buildCircleArc(final int steps, final int totalSegs) {
        if (steps <= 0) {
            final Mesh m = new Mesh();
            m.setBuffer(VertexBuffer.Type.Position, 3,
                    BufferUtils.createFloatBuffer(new float[]{ 0f, 0f, 0f }));
            m.setBuffer(VertexBuffer.Type.Normal,   3,
                    BufferUtils.createFloatBuffer(new float[]{ 0f, 0f, 1f }));
            m.setBuffer(VertexBuffer.Type.TexCoord, 2,
                    BufferUtils.createFloatBuffer(new float[]{ 0f, 0f }));
            m.setBuffer(VertexBuffer.Type.Index,    3,
                    BufferUtils.createShortBuffer(new short[0]));
            m.updateBound();
            return m;
        }

        final float outerR = PARRY_CIRCLE_RADIUS;
        final float innerR = outerR - PARRY_CIRCLE_THICK;

        // Each step = 2 verts (outer + inner), plus closing pair
        final int vertCount = (steps + 1) * 2;
        final FloatBuffer pos  = BufferUtils.createFloatBuffer(vertCount * 3);
        final FloatBuffer norm = BufferUtils.createFloatBuffer(vertCount * 3);
        final FloatBuffer uv   = BufferUtils.createFloatBuffer(vertCount * 2);
        // Each segment = 2 triangles = 6 indices
        final ShortBuffer idx  = BufferUtils.createShortBuffer(steps * 6);

        for (int i = 0; i <= steps; i++) {
            // Clockwise from top: start at -HALF_PI, go in positive direction
            final float angle = -FastMath.HALF_PI + FastMath.TWO_PI * i / totalSegs;
            final float cosA  = FastMath.cos(angle);
            final float sinA  = FastMath.sin(angle);

            // Outer vertex
            pos.put(cosA * outerR).put(sinA * outerR).put(0f);
            norm.put(0f).put(0f).put(1f);
            uv.put(0.5f + cosA * 0.5f).put(0.5f + sinA * 0.5f);

            // Inner vertex
            pos.put(cosA * innerR).put(sinA * innerR).put(0f);
            norm.put(0f).put(0f).put(1f);
            uv.put(0.5f + cosA * 0.5f * (innerR / outerR))
              .put(0.5f + sinA * 0.5f * (innerR / outerR));
        }

        for (int i = 0; i < steps; i++) {
            final short o0 = (short)(i * 2);      // outer current
            final short i0 = (short)(i * 2 + 1);  // inner current
            final short o1 = (short)(i * 2 + 2);  // outer next
            final short i1 = (short)(i * 2 + 3);  // inner next

            // Triangle 1: outer-cur, outer-next, inner-cur
            idx.put(o0).put(o1).put(i0);
            // Triangle 2: inner-cur, outer-next, inner-next
            idx.put(i0).put(o1).put(i1);
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
    //  RadialRing — filled pie arc, used for cooldown display
    // =========================================================

    private static final class RadialRing {

        private final Node     node   = new Node("Ring");
        private final Geometry fill;
        private final float    radius;
        private final int      segments;

        RadialRing(final AssetManager assets, final float size, final int segs) {
            this.radius   = size / 2f;
            this.segments = segs;

            final Geometry bg = new Geometry("Track", buildArc(segments, segments));
            bg.setMaterial(flatMat(assets, RING_TRACK));
            bg.setLocalTranslation(radius, radius, 0f);
            node.attachChild(bg);

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

        private Mesh buildArc(final int steps, final int totalSegs) {
            if (steps <= 0) {
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

            final int vertCount = steps + 2;
            final FloatBuffer pos  = BufferUtils.createFloatBuffer(vertCount * 3);
            final FloatBuffer norm = BufferUtils.createFloatBuffer(vertCount * 3);
            final FloatBuffer uv   = BufferUtils.createFloatBuffer(vertCount * 2);
            final ShortBuffer idx  = BufferUtils.createShortBuffer(steps * 3);

            pos.put(0f).put(0f).put(0f);
            norm.put(0f).put(0f).put(1f);
            uv.put(0.5f).put(0.5f);

            for (int i = 0; i <= steps; i++) {
                final float angle = FastMath.TWO_PI * i / totalSegs - FastMath.HALF_PI;
                final float vx    =  FastMath.cos(angle) * radius;
                final float vy    =  FastMath.sin(angle) * radius;
                pos.put(vx).put(vy).put(0f);
                norm.put(0f).put(0f).put(1f);
                uv.put(0.5f + vx / radius * 0.5f)
                  .put(0.5f + vy / radius * 0.5f);
            }

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
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            return mat;
        }
    }
}