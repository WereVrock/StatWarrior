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
import entity.player.PlayerActions;
import main.Main;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Two radial cooldown indicators, bottom-center of screen.
 * Parry icon has a green border indicator that drains top-to-bottom
 * while parry is active.
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

    /** Thickness of the parry active border in pixels. */
    private static final float BORDER_THICK  = 4f;

    private static final ColorRGBA RING_TRACK    = new ColorRGBA(0.08f, 0.06f, 0.06f, 0.75f);
    private static final ColorRGBA RING_READY    = new ColorRGBA(0.72f, 0.58f, 0.22f, 1.00f);
    private static final ColorRGBA RING_FILL     = new ColorRGBA(0.55f, 0.14f, 0.14f, 1.00f);
    private static final ColorRGBA ICON_BRIGHT   = new ColorRGBA(1.00f, 1.00f, 1.00f, 0.90f);
    private static final ColorRGBA ICON_DIM      = new ColorRGBA(0.35f, 0.35f, 0.35f, 0.70f);
    private static final ColorRGBA PARRY_ACTIVE  = new ColorRGBA(0.20f, 0.90f, 0.30f, 1.00f);

    private final RadialRing dodgeRing;
    private final RadialRing parryRing;
    private final Geometry   dodgeIcon;
    private final Geometry   parryIcon;

    /** Four quads forming the parry active border around the parry ring. */
    private final Geometry parryBorderLeft;
    private final Geometry parryBorderRight;
    private final Geometry parryBorderBottom;
    /** Top border drains from full-height down to zero as duration expires. */
    private final Geometry parryBorderTop;

    /** Stored so update() can reposition the top border. */
    private final float parryRingBaseY;
    private final float parryCX;

    public CooldownHUD(final Node guiNode, final AssetManager assets,
                       final float screenW, final float screenH) {

        final float totalW = RING_SIZE * 2f + GAP;
        final float startX = screenW / 2f - totalW / 2f;
        final float baseY  = BOTTOM_MARGIN;

        final float dodgeCX = startX + RING_SIZE / 2f;
        parryCX             = startX + RING_SIZE + GAP + RING_SIZE / 2f;
        final float ringCY  = baseY + RING_SIZE / 2f;
        parryRingBaseY      = baseY;

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

        // Parry border quads — drawn at z=2 so they appear in front of rings
        final float bx = parryCX - RING_SIZE / 2f; // ring left edge X
        final float by = parryRingBaseY;            // ring bottom edge Y

        parryBorderLeft   = buildColorQuad(assets, BORDER_THICK, RING_SIZE,  PARRY_ACTIVE);
        parryBorderRight  = buildColorQuad(assets, BORDER_THICK, RING_SIZE,  PARRY_ACTIVE);
        parryBorderBottom = buildColorQuad(assets, RING_SIZE,    BORDER_THICK, PARRY_ACTIVE);
        parryBorderTop    = buildColorQuad(assets, RING_SIZE,    BORDER_THICK, PARRY_ACTIVE);

        parryBorderLeft  .setLocalTranslation(bx - BORDER_THICK, by, 2f);
        parryBorderRight .setLocalTranslation(bx + RING_SIZE,    by, 2f);
        parryBorderBottom.setLocalTranslation(bx, by - BORDER_THICK, 2f);
        parryBorderTop   .setLocalTranslation(bx, by + RING_SIZE,    2f);

        guiNode.attachChild(dodgeRing.getNode());
        guiNode.attachChild(parryRing.getNode());
        guiNode.attachChild(dodgeIcon);
        guiNode.attachChild(parryIcon);
        guiNode.attachChild(parryBorderLeft);
        guiNode.attachChild(parryBorderRight);
        guiNode.attachChild(parryBorderBottom);
        guiNode.attachChild(parryBorderTop);
    }

    public void update() {
        final PlayerActions a      = Main.PLAYER.getActions();
        final float         dodge  = a.getDodgeCooldownFraction();
        final float         parry  = a.getParryCooldownFraction();
        final boolean       active = a.isParrying();

        dodgeRing.setFraction(dodge, dodge >= 1f ? RING_READY : RING_FILL);
        parryRing.setFraction(parry, parry >= 1f ? RING_READY : RING_FILL);

        dodgeIcon.getMaterial().setColor(
                "Color", dodge >= 1f ? ICON_BRIGHT : ICON_DIM);
        parryIcon.getMaterial().setColor(
                "Color", parry >= 1f ? ICON_BRIGHT : ICON_DIM);

        updateParryBorder(active, a.getParryDurationFraction());
    }

    /**
     * Shows or hides the parry border.
     * When active, the top bar scales down from RING_SIZE to 0 as duration expires.
     * Left, right, and bottom bars are always full while active.
     *
     * @param active           true while the player is in the PARRYING state
     * @param durationFraction 1.0 = full duration remaining, 0.0 = expired
     */
    private void updateParryBorder(final boolean active, final float durationFraction) {
        final com.jme3.scene.Spatial.CullHint hint =
                active ? com.jme3.scene.Spatial.CullHint.Inherit
                       : com.jme3.scene.Spatial.CullHint.Always;

        parryBorderLeft  .setCullHint(hint);
        parryBorderRight .setCullHint(hint);
        parryBorderBottom.setCullHint(hint);
        parryBorderTop   .setCullHint(hint);

        if (!active) return;

        // Side and bottom bars are always full — no scale needed.
        // Side bars scale their height with duration (they drain from top to bottom).
        // Bottom bar stays full.
        // Top bar scales its width from full to zero (it shrinks, giving the
        // "disappears from top to bottom" feel together with side bars shrinking).

        // The sides drain: full height at fraction=1, zero at fraction=0.
        // Scale Y of left/right bars — their origin is at the bottom, so they shrink upward.
        final float sideScale = Math.max(0f, durationFraction);
        parryBorderLeft .setLocalScale(1f, sideScale, 1f);
        parryBorderRight.setLocalScale(1f, sideScale, 1f);

        // Top bar: only visible while the sides are still at full height, then vanishes.
        // Simpler: the top bar itself shrinks in width from center outward — but per the
        // spec "disappears from top to bottom" means the rectangle outline fills like a
        // gauge. Implementing: side bars drain downward, top bar disappears first.
        // We show the top bar only at high fraction (it disappears before the sides finish).
        parryBorderTop.setCullHint(
                durationFraction > 0.05f
                        ? com.jme3.scene.Spatial.CullHint.Inherit
                        : com.jme3.scene.Spatial.CullHint.Always);
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

    private static Geometry buildColorQuad(final AssetManager assets,
                                           final float w, final float h,
                                           final ColorRGBA color) {
        final Geometry geo = new Geometry("Border", new Quad(w, h));
        final Material mat = new Material(
                assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        return geo;
    }

    // =========================================================
    //  RadialRing
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