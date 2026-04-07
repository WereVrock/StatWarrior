package render3d.screenRendering;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Random;
import render3d.GameApplication;

public final class YouDiedOverlay {

    private static final float FADE_SPEED          = 0.6f;
    private static final float TITLE_DELAY          = 0.8f;
    private static final float OPTION_DELAY         = 1.6f;
    private static final float BG_TARGET_ALPHA      = 0.78f;
    private static final float VIGNETTE_BASE_ALPHA  = 0.85f;
    private static final float VIGNETTE_PULSE_AMP   = 0.12f;
    private static final float VIGNETTE_PULSE_SPEED = 1.4f;

    private static final int   VIGNETTE_TEX_W = 512;
    private static final int   VIGNETTE_TEX_H = 512;
    private static final int   SPLAT_SEED     = 0xDEAD;

    private static final ColorRGBA SELECT_COLOR   = new ColorRGBA(0.95f, 0.95f, 0.95f, 1f);
    private static final ColorRGBA UNSELECT_COLOR = new ColorRGBA(0.40f, 0.40f, 0.40f, 1f);
    private static final ColorRGBA TITLE_COLOR    = new ColorRGBA(0.80f, 0.04f, 0.04f, 0f);

    private final Node     guiNode;
    private final int      screenW;
    private final int      screenH;
    private final Runnable onRestart;
    private final Runnable onExit;

    private Geometry   bgGeo;
    private Geometry   vignetteGeo;  // single fullscreen quad with blood texture
    private BitmapText titleText;
    private BitmapText restartText;
    private BitmapText exitText;

    private boolean visible       = false;
    private float   timer         = 0f;
    private float   bgAlpha       = 0f;
    private float   vignetteAlpha = 0f;
    private int     selectedIndex = 0;

    private final boolean[] lastUp   = {false};
    private final boolean[] lastDown = {false};
    private final boolean[] lastA    = {false};

    public YouDiedOverlay(final Node guiNode,
                          final BitmapFont font,
                          final int screenW,
                          final int screenH,
                          final Runnable onRestart,
                          final Runnable onExit) {
        this.guiNode   = guiNode;
        this.screenW   = screenW;
        this.screenH   = screenH;
        this.onRestart = onRestart;
        this.onExit    = onExit;
        buildUI(font);
    }

    private void buildUI(final BitmapFont font) {
        bgGeo = buildColorQuad(screenW, screenH, new ColorRGBA(0f, 0f, 0f, 0f), 0f);

        vignetteGeo = buildVignetteQuad();

        titleText = new BitmapText(font, false);
        titleText.setSize(font.getCharSet().getRenderedSize() * 4f);
        titleText.setText("YOU DIED");
        titleText.setColor(TITLE_COLOR.clone());
        titleText.setLocalTranslation(
                screenW / 2f - titleText.getLineWidth() / 2f,
                screenH / 2f + 80f,
                3f
        );

        restartText = new BitmapText(font, false);
        restartText.setSize(font.getCharSet().getRenderedSize() * 1.8f);

        exitText = new BitmapText(font, false);
        exitText.setSize(font.getCharSet().getRenderedSize() * 1.8f);

        restartText.setLocalTranslation(screenW / 2f - 80f, screenH / 2f - 20f, 3f);
        exitText.setLocalTranslation(   screenW / 2f - 80f, screenH / 2f - 60f, 3f);
    }

    /**
     * Builds the blood vignette as a fullscreen quad with a procedural
     * radial-gradient + splatter texture baked into a BufferedImage.
     * Alpha is controlled per-frame via the material color's alpha channel.
     */
    private Geometry buildVignetteQuad() {
        final BufferedImage img = paintBloodVignette(VIGNETTE_TEX_W, VIGNETTE_TEX_H);
        final Texture2D     tex = bufferedImageToTexture(img);

        final Quad     quad = new Quad(screenW, screenH);
        final Geometry geo  = new Geometry("BloodVignette", quad);

        final Material mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setTexture("ColorMap", tex);
        // We modulate the whole texture alpha via Color (white = full texture alpha)
        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        geo.setLocalTranslation(0f, 0f, 2f);
        return geo;
    }

    /**
     * Paints the blood vignette texture:
     * - Deep radial gradient darkening the edges
     * - Organic drip/splatter shapes around the border painted in blood red
     * - All alpha encoded in the image so the material color alpha scales it uniformly
     */
    private static BufferedImage paintBloodVignette(final int w, final int h) {
        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D    g   = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Clear to fully transparent
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, w, h);
        g.setComposite(AlphaComposite.SrcOver);

        final float cx = w / 2f;
        final float cy = h / 2f;

        // 2. Radial gradient — opaque dark red at edges, transparent at center
        final float[] fractions = { 0.0f, 0.45f, 0.72f, 1.0f };
        final Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(60, 0, 0, 0),
            new Color(100, 0, 0, 160),
            new Color(20, 0, 0, 240),
        };
        final RadialGradientPaint gradient = new RadialGradientPaint(
                cx, cy,
                Math.max(w, h) * 0.62f,
                fractions, colors
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, w, h);

        // 3. Blood splatter drips around the perimeter
        final Random rng = new Random(SPLAT_SEED);
        paintBloodSplatters(g, w, h, rng);

        // 4. Vein-like tendrils creeping inward from edges
        paintBloodTendrils(g, w, h, rng);

        g.dispose();
        return img;
    }

    private static void paintBloodSplatters(final Graphics2D g,
                                            final int w, final int h,
                                            final Random rng) {
        final int SPLAT_COUNT = 28;
        for (int i = 0; i < SPLAT_COUNT; i++) {
            // Place splats along the edges with some inward offset
            final float edge   = rng.nextFloat();
            final float inset  = rng.nextFloat() * 0.18f * Math.min(w, h);
            final float sx, sy;

            final int edgeSide = (int)(edge * 4);
            switch (edgeSide) {
                case 0  -> { sx = rng.nextFloat() * w;        sy = inset;          }
                case 1  -> { sx = rng.nextFloat() * w;        sy = h - inset;      }
                case 2  -> { sx = inset;                       sy = rng.nextFloat() * h; }
                default -> { sx = w - inset;                   sy = rng.nextFloat() * h; }
            }

            final int   drops     = 3 + rng.nextInt(5);
            final float baseR     = (4f + rng.nextFloat() * 18f);
            final float alphaBase = 0.55f + rng.nextFloat() * 0.40f;
            final Color blood     = new Color(
                    0.45f + rng.nextFloat() * 0.15f,
                    0f,
                    0f,
                    alphaBase
            );

            g.setColor(blood);

            // Main blob
            final int blobR = (int) baseR;
            g.fillOval((int) sx - blobR, (int) sy - blobR, blobR * 2, blobR * 2);

            // Satellite droplets
            for (int d = 0; d < drops; d++) {
                final float angle  = rng.nextFloat() * (float)(Math.PI * 2);
                final float dist   = baseR * (0.8f + rng.nextFloat() * 2.2f);
                final float dropSX = sx + (float) Math.cos(angle) * dist;
                final float dropSY = sy + (float) Math.sin(angle) * dist;
                final int   dropR  = Math.max(1, (int)(baseR * (0.15f + rng.nextFloat() * 0.45f)));

                g.fillOval((int) dropSX - dropR, (int) dropSY - dropR,
                        dropR * 2, dropR * 2);
            }

            // Drip streak downward (gravity)
            if (rng.nextFloat() > 0.4f) {
                paintDrip(g, sx, sy + baseR, baseR * 0.35f,
                        baseR * (1f + rng.nextFloat() * 3f), blood);
            }
        }
    }

    private static void paintDrip(final Graphics2D g,
                                   final float x, final float y,
                                   final float width, final float length,
                                   final Color color) {
        final Path2D.Float drip = new Path2D.Float();
        drip.moveTo(x - width / 2f, y);
        drip.curveTo(
                x - width,      y + length * 0.3f,
                x + width,      y + length * 0.6f,
                x,              y + length
        );
        drip.curveTo(
                x - width * 0.5f, y + length * 0.6f,
                x + width * 0.5f, y + length * 0.3f,
                x + width / 2f,   y
        );
        drip.closePath();
        g.setColor(color);
        g.fill(drip);
        // Rounded tip
        final int tipR = Math.max(1, (int)(width * 0.6f));
        g.fillOval((int)(x - tipR), (int)(y + length - tipR), tipR * 2, tipR * 2);
    }

    private static void paintBloodTendrils(final Graphics2D g,
                                            final int w, final int h,
                                            final Random rng) {
        final int TENDRIL_COUNT = 14;
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        for (int i = 0; i < TENDRIL_COUNT; i++) {
            final float startAngle = (float)(i * Math.PI * 2 / TENDRIL_COUNT)
                    + rng.nextFloat() * 0.4f;
            final float radius     = Math.min(w, h) * 0.5f;

            // Start from edge
            final float startX = w / 2f + (float) Math.cos(startAngle) * radius;
            final float startY = h / 2f + (float) Math.sin(startAngle) * radius;

            // Creep inward a random amount
            final float reach  = radius * (0.2f + rng.nextFloat() * 0.45f);
            final float endX   = w / 2f + (float) Math.cos(startAngle) * (radius - reach);
            final float endY   = h / 2f + (float) Math.sin(startAngle) * (radius - reach);

            // Slight curve via control point
            final float cpAngle = startAngle + (rng.nextFloat() - 0.5f) * 0.8f;
            final float cpDist  = radius * (0.5f + rng.nextFloat() * 0.3f);
            final float cpX     = w / 2f + (float) Math.cos(cpAngle) * cpDist;
            final float cpY     = h / 2f + (float) Math.sin(cpAngle) * cpDist;

            final float alpha     = 0.35f + rng.nextFloat() * 0.45f;
            final float thickness = 1.5f  + rng.nextFloat() * 3.5f;
            final Color blood     = new Color(0.5f, 0f, 0f, alpha);

            g.setColor(blood);
            g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));

            final Path2D.Float path = new Path2D.Float();
            path.moveTo(startX, startY);
            path.quadTo(cpX, cpY, endX, endY);
            g.draw(path);
        }
    }

    /** Converts a BufferedImage into a jME3 Texture2D via raw ARGB bytes. */
    private static Texture2D bufferedImageToTexture(final BufferedImage img) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        final ByteBuffer buf = com.jme3.util.BufferUtils.createByteBuffer(w * h * 4);

        // jME3 Image expects rows bottom-to-top for GUI quads rendered upright
        for (int row = h - 1; row >= 0; row--) {
            for (int col = 0; col < w; col++) {
                final int argb = img.getRGB(col, row);
                buf.put((byte)((argb >> 16) & 0xFF)); // R
                buf.put((byte)((argb >>  8) & 0xFF)); // G
                buf.put((byte)( argb        & 0xFF)); // B
                buf.put((byte)((argb >> 24) & 0xFF)); // A
            }
        }
        buf.flip();

        final Image jmeImg = new Image(
                Image.Format.RGBA8,
                w, h,
                buf,
                com.jme3.texture.image.ColorSpace.sRGB
        );
        return new Texture2D(jmeImg);
    }

    private Geometry buildColorQuad(final float w, final float h,
                                    final ColorRGBA color, final float z) {
        final Quad     quad = new Quad(w, h);
        final Geometry geo  = new Geometry("YouDiedBG", quad);
        final Material mat  = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", color.clone());
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        geo.setLocalTranslation(0f, 0f, z);
        return geo;
    }

    public void show() {
        if (visible) return;
        visible       = true;
        timer         = 0f;
        bgAlpha       = 0f;
        vignetteAlpha = 0f;
        selectedIndex = 0;

        guiNode.attachChild(bgGeo);
        guiNode.attachChild(vignetteGeo);
        guiNode.attachChild(titleText);
        guiNode.attachChild(restartText);
        guiNode.attachChild(exitText);

        titleText.setColor(TITLE_COLOR.clone());
        refreshSelection();
    }

    public void hide() {
        if (!visible) return;
        visible = false;
        guiNode.detachChild(bgGeo);
        guiNode.detachChild(vignetteGeo);
        guiNode.detachChild(titleText);
        guiNode.detachChild(restartText);
        guiNode.detachChild(exitText);
    }

    public void update(final float tpf) {
        if (!visible) return;
        timer += tpf;
        updateBackground(tpf);
        updateVignette(tpf);
        updateTitle(tpf);
        if (timer >= OPTION_DELAY) handleInput();
    }

    private void updateBackground(final float tpf) {
        if (bgAlpha < BG_TARGET_ALPHA) {
            bgAlpha = Math.min(bgAlpha + FADE_SPEED * tpf, BG_TARGET_ALPHA);
            bgGeo.getMaterial().setColor("Color", new ColorRGBA(0f, 0f, 0f, bgAlpha));
        }
    }

    private void updateVignette(final float tpf) {
        if (vignetteAlpha < VIGNETTE_BASE_ALPHA) {
            vignetteAlpha = Math.min(
                    vignetteAlpha + FADE_SPEED * 1.8f * tpf,
                    VIGNETTE_BASE_ALPHA
            );
        }

        // Sine pulse layered on top — breathing blood effect
        final float pulse = (float) Math.sin(timer * VIGNETTE_PULSE_SPEED) * VIGNETTE_PULSE_AMP;
        final float alpha = Math.max(0f, Math.min(1f, vignetteAlpha + pulse));

        vignetteGeo.getMaterial().setColor("Color", new ColorRGBA(1f, 1f, 1f, alpha));
    }

    private void updateTitle(final float tpf) {
        if (timer < TITLE_DELAY) return;
        final ColorRGBA tc = titleText.getColor().clone();
        if (tc.a < 1f) {
            tc.a = Math.min(tc.a + FADE_SPEED * tpf, 1f);
            titleText.setColor(tc);
        }
    }

    private void handleInput() {
        final boolean upNow   = main.Main.CONTROLLER.isUpPressed();
        final boolean downNow = main.Main.CONTROLLER.isDownPressed();
        final boolean aNow    = main.Main.CONTROLLER.isButtonPressed("A");

        if (upNow   && !lastUp[0])   { selectedIndex = (selectedIndex - 1 + 2) % 2; refreshSelection(); }
        if (downNow && !lastDown[0]) { selectedIndex = (selectedIndex + 1) % 2;      refreshSelection(); }
        if (aNow    && !lastA[0])    { if (selectedIndex == 0) onRestart.run(); else onExit.run(); }

        lastUp[0]   = upNow;
        lastDown[0] = downNow;
        lastA[0]    = aNow;
    }

    private void refreshSelection() {
        restartText.setText(selectedIndex == 0 ? "> Restart" : "  Restart");
        exitText.setText(   selectedIndex == 1 ? "> Exit"    : "  Exit");
        restartText.setColor(selectedIndex == 0 ? SELECT_COLOR.clone() : UNSELECT_COLOR.clone());
        exitText.setColor(   selectedIndex == 1 ? SELECT_COLOR.clone() : UNSELECT_COLOR.clone());
    }

    public boolean isVisible() { return visible; }
}