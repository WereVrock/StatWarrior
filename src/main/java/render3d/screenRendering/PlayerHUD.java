// ===== render3d/PlayerHUD.java =====
package render3d.screenRendering;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import entity.player.PlayerStats;
import main.Main;

/**
 * Three horizontal resource bars in the bottom-left corner.
 * Health (red), Muscle (orange), Stamina (green).
 */
public final class PlayerHUD {

    private static final float BAR_W      = 200f;
    private static final float BAR_H      = 14f;
    private static final float BAR_GAP    = 6f;
    private static final float LEFT_MARGIN = 20f;
    private static final float BOT_MARGIN  = 20f;

    private static final ColorRGBA TRACK_COLOR   = new ColorRGBA(0.08f, 0.06f, 0.06f, 0.80f);
    private static final ColorRGBA HEALTH_COLOR  = new ColorRGBA(0.75f, 0.12f, 0.12f, 1.00f);
    private static final ColorRGBA MUSCLE_COLOR  = new ColorRGBA(0.80f, 0.45f, 0.10f, 1.00f);
    private static final ColorRGBA STAMINA_COLOR = new ColorRGBA(0.20f, 0.65f, 0.25f, 1.00f);

    private final Geometry healthFill;
    private final Geometry muscleFill;
    private final Geometry staminaFill;

    public PlayerHUD(final Node guiNode, final AssetManager assets,
                     final float screenH) {

        final float y0 = BOT_MARGIN + (BAR_H + BAR_GAP) * 2f; // health (top of stack)
        final float y1 = BOT_MARGIN + (BAR_H + BAR_GAP);       // muscle
        final float y2 = BOT_MARGIN;                            // stamina

        // Tracks
        guiNode.attachChild(track(assets, LEFT_MARGIN, y0));
        guiNode.attachChild(track(assets, LEFT_MARGIN, y1));
        guiNode.attachChild(track(assets, LEFT_MARGIN, y2));

        // Fills
        healthFill  = fill(assets, LEFT_MARGIN, y0, HEALTH_COLOR);
        muscleFill  = fill(assets, LEFT_MARGIN, y1, MUSCLE_COLOR);
        staminaFill = fill(assets, LEFT_MARGIN, y2, STAMINA_COLOR);

        guiNode.attachChild(healthFill);
        guiNode.attachChild(muscleFill);
        guiNode.attachChild(staminaFill);
    }

    public void update() {
        final PlayerStats s = Main.PLAYER.getStats();
        setFill(healthFill,  s.getHealthFraction());
        setFill(muscleFill,  s.getMuscleFraction());
        setFill(staminaFill, s.getStaminaFraction());
    }

    private static void setFill(final Geometry geo, final float fraction) {
        geo.setLocalScale(Math.max(0f, fraction), 1f, 1f);
    }

    private static Geometry track(final AssetManager assets,
                                   final float x, final float y) {
        return buildBar(assets, x, y, BAR_W, BAR_H, TRACK_COLOR, 0f);
    }

    private static Geometry fill(final AssetManager assets,
                                  final float x, final float y,
                                  final ColorRGBA color) {
        return buildBar(assets, x, y, BAR_W, BAR_H, color, 0.5f);
    }

    private static Geometry buildBar(final AssetManager assets,
                                      final float x, final float y,
                                      final float w, final float h,
                                      final ColorRGBA color,
                                      final float zOffset) {
        final Geometry geo = new Geometry("Bar", new Quad(w, h));
        final Material mat = new Material(
                assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        geo.setLocalTranslation(x, y, zOffset);
        return geo;
    }
}