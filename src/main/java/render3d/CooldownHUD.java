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
 * Draws two radial cooldown indicators in the bottom-center of the screen.
 *
 * Icon images go here:
 *   assets/Textures/hud/icon_dodge.png   (64x64 recommended)
 *   assets/Textures/hud/icon_parry.png   (64x64 recommended)
 *
 * Layout (bottom-center, 20px above bottom edge):
 *   [DODGE]  [PARRY]
 *   Each ring is 64px wide, spaced 16px apart, centered on screen.
 */
public final class CooldownHUD {

    // ===== Layout =====
    private static final float RING_SIZE      = 64f;
    private static final float ICON_SIZE      = 40f;
    private static final float GAP            = 16f;
    private static final float BOTTOM_MARGIN  = 20f;
    private static final int   RING_SEGMENTS  = 48;

    // ===== Colors (dark fantasy palette) =====
    private static final ColorRGBA RING_BG     = new ColorRGBA(0.08f, 0.06f, 0.06f, 0.75f);
    private static final ColorRGBA RING_READY  = new ColorRGBA(0.72f, 0.58f, 0.22f, 1.00f); // gold
    private static final ColorRGBA RING_FILL   = new ColorRGBA(0.55f, 0.14f, 0.14f, 1.00f); // dark red
    private static final ColorRGBA ICON_TINT   = new ColorRGBA(1.00f, 1.00f, 1.00f, 0.90f);
    private static final ColorRGBA ICON_DIM    = new ColorRGBA(0.35f, 0.35f, 0.35f, 0.70f);

    private final Node   guiNode;
    private final float  screenW;
    private final float  screenH;

    // Dodge
    private final RadialRing dodgeRing;
    private final Geometry   dodgeIcon;
    private final Material   dodgeIconMat;

    // Parry
    private final RadialRing parryRing;
    private final Geometry   parryIcon;
    private final Material   parryIconMat;

    public CooldownHUD(final Node guiNode, final AssetManager assets,
                       final float screenW, final float screenH) {
        this.guiNode  = guiNode;
        this.screenW  = screenW;
        this.screenH  = screenH;

        final float totalW  = RING_SIZE * 2 + GAP;
        final float startX  = screenW / 2f - totalW / 2f;
        final float ringY   = BOTTOM_MARGIN;

        final float dodgeCX = startX + RING_SIZE / 2f;
        final float parryCX = startX + RING_SIZE + GAP + RING_SIZE / 2f;
        final float ringCY  = ringY + RING_SIZE / 2f;

        dodgeRing    = new RadialRing(assets, RING_SIZE, RING_SEGMENTS);
        dodgeIcon    = buildIcon(assets, "Textures/hud/icon_dodge.png");
        dodgeIconMat = dodgeIcon.getMaterial();
        positionRing(dodgeRing, dodgeCX, ringCY);
        positionIcon(dodgeIcon, dodgeCX, ringCY);

        parryRing    = new RadialRing(assets, RING_SIZE, RING_SEGMENTS);
        parryIcon    = buildIcon(assets, "Textures/hud/icon_parry.png");
        parryIconMat = parryIcon.getMaterial();
        positionRing(parryRing, parryCX, ringCY);
        positionIcon(parryIcon, parryCX, ringCY);

        guiNode.attachChild(dodgeRing.getNode());
        guiNode.attachChild(dodgeIcon);
        guiNode.attachChild(parryRing.getNode());
        guiNode.attachChild(parryIcon);
    }

    public void update() {
        final PlayerActions actions = Main.PLAYER.getActions();
        final float dodgeFrac = actions.getDodgeCooldownFraction();
        final float parryFrac = actions.getParryCooldownFraction();

        dodgeRing.setFraction(dodgeFrac, dodgeFrac >= 1f ? RING_READY : RING_FILL);
        parryRing.setFraction(parryFrac, parryFrac >= 1f ? RING_READY : RING_FILL);

        dodgeIconMat.setColor("Color", dodgeFrac >= 1f ? ICON_TINT : ICON_DIM);
        parryIconMat.setColor("Color", parryFrac >= 1f ? ICON_TINT : ICON_DIM);
    }

    private void positionRing(final RadialRing ring, final float cx, final float cy) {
        ring.getNode().setLocalTranslation(cx - RING_SIZE / 2f, cy - RING_SIZE / 2f, 0f);
    }

    private void positionIcon(final Geometry icon, final float cx, final float cy) {
        icon.setLocalTranslation(cx - ICON_SIZE / 2f, cy - ICON_SIZE / 2f, 1f);
    }

    private Geometry buildIcon(final AssetManager assets, final String path) {
        final Quad     quad = new Quad(ICON_SIZE, ICON_SIZE);
        final Geometry geo  = new Geometry("HudIcon", quad);
        final Material mat  = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        try {
            final Texture tex = assets.loadTexture(path);
            mat.setTexture("ColorMap", tex);
        } catch (final Exception ignored) {
            // Icon texture missing — will render as tinted white square
        }
        mat.setColor("Color", ICON_TINT);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        return geo;
    }

    // =========================================================
    //  Inner class — builds and updates one radial ring mesh
    // =========================================================

    private static final class RadialRing {

        private final Node     node = new Node("RadialRing");
        private final Geometry bg;
        private final Geometry fill;
        private final int      segments;
        private final float    radius;

        RadialRing(final AssetManager assets, final float size, final int segments) {
            this.segments = segments;
            this.radius   = size / 2f;

            final Material bgMat = flatMat(assets, RING_BG);
            bg   = buildDisc(assets, bgMat, 1f);

            final Material fillMat = flatMat(assets, RING_FILL);
            fill = buildDisc(assets, fillMat, 0f);

            node.attachChild(bg);
            node.attachChild(fill);
        }

        void setFraction(final float fraction, final ColorRGBA color) {
            fill.getMaterial().setColor("Color", color);
            rebuildArc(fill, FastMath.clamp(fraction, 0f, 1f));
        }

        Node getNode() { return node; }

        private Geometry buildDisc(final AssetManager assets,
                                   final Material mat, final float fraction) {
            final Geometry geo = new Geometry("Disc", buildArcMesh(fraction));
            geo.setMaterial(mat);
            geo.setLocalTranslation(radius, radius, 0f);
            return geo;
        }

        private void rebuildArc(final Geometry geo, final float fraction) {
            geo.setMesh(buildArcMesh(fraction));
        }

        private Mesh buildArcMesh(final float fraction) {
            final int    verts    = segments + 2;
            final float  angleEnd = FastMath.TWO_PI * fraction;
            final float  start    = FastMath.HALF_PI; // 12 o'clock

            final FloatBuffer pos    = BufferUtils.createFloatBuffer(verts * 3);
            final FloatBuffer norm   = BufferUtils.createFloatBuffer(verts * 3);
            final FloatBuffer uv     = BufferUtils.createFloatBuffer(verts * 2);
            final ShortBuffer idx    = BufferUtils.createShortBuffer(segments * 3);

            // Center vertex
            pos.put(0f).put(0f).put(0f);
            norm.put(0f).put(0f).put(1f);
            uv.put(0.5f).put(0.5f);

            final int steps = (int)(segments * fraction);
            for (int i = 0; i <= steps; i++) {
                final float angle = start - (angleEnd * i / segments);
                final float vx    = FastMath.cos(angle) * radius;
                final float vy    = FastMath.sin(angle) * radius;
                pos.put(vx).put(vy).put(0f);
                norm.put(0f).put(0f).put(1f);
                uv.put(0.5f + vx / radius * 0.5f).put(0.5f + vy / radius * 0.5f);
            }

            for (int i = 0; i < steps; i++) {
                idx.put((short) 0)
                   .put((short)(i + 1))
                   .put((short)(i + 2));
            }

            pos.flip(); norm.flip(); uv.flip(); idx.flip();

            final Mesh mesh = new Mesh();
            mesh.setBuffer(VertexBuffer.Type.Position, 3, pos);
            mesh.setBuffer(VertexBuffer.Type.Normal,   3, norm);
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, uv);
            mesh.setBuffer(VertexBuffer.Type.Index,    3, idx);
            mesh.updateBound();
            return mesh;
        }

        private static Material flatMat(final AssetManager assets, final ColorRGBA color) {
            final Material mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            return mat;
        }
    }
}