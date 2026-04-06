package render3d;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public final class YouDiedOverlay {

    private static final float FADE_SPEED         = 0.6f;
    private static final float TITLE_DELAY         = 0.8f;
    private static final float OPTION_DELAY        = 1.6f;

    private static final float BG_TARGET_ALPHA     = 0.78f;
    private static final float VIGNETTE_BASE_ALPHA = 0.55f;
    private static final float VIGNETTE_PULSE_AMP  = 0.18f;
    private static final float VIGNETTE_PULSE_SPEED = 1.8f; // radians per second

    private static final ColorRGBA SELECT_COLOR   = new ColorRGBA(0.9f,  0.9f,  0.9f,  1f);
    private static final ColorRGBA UNSELECT_COLOR = new ColorRGBA(0.45f, 0.45f, 0.45f, 1f);
    private static final ColorRGBA TITLE_COLOR    = new ColorRGBA(0.75f, 0.05f, 0.05f, 0f);

    // Vignette is four edge quads (top, bottom, left, right) that fade to black
    private static final int VIGNETTE_THICKNESS = 220;

    private final Node     guiNode;
    private final int      screenW;
    private final int      screenH;
    private final Runnable onRestart;
    private final Runnable onExit;

    private Geometry   bgGeo;
    private Geometry[] vignetteGeos; // [top, bottom, left, right]
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
        bgGeo = buildColorQuad(screenW, screenH, new ColorRGBA(0f, 0f, 0f, 0f));
        bgGeo.setLocalTranslation(0f, 0f, 0f);

        vignetteGeos = new Geometry[4];
        vignetteGeos[0] = buildVignetteQuad(screenW, VIGNETTE_THICKNESS, false); // top
        vignetteGeos[1] = buildVignetteQuad(screenW, VIGNETTE_THICKNESS, false); // bottom
        vignetteGeos[2] = buildVignetteQuad(VIGNETTE_THICKNESS, screenH, true);  // left
        vignetteGeos[3] = buildVignetteQuad(VIGNETTE_THICKNESS, screenH, true);  // right

        // top — sits at top of screen (jME GUI Y=0 is bottom)
        vignetteGeos[0].setLocalTranslation(0f, screenH - VIGNETTE_THICKNESS, 2f);
        // bottom
        vignetteGeos[1].setLocalTranslation(0f, 0f, 2f);
        // left
        vignetteGeos[2].setLocalTranslation(0f, 0f, 2f);
        // right
        vignetteGeos[3].setLocalTranslation(screenW - VIGNETTE_THICKNESS, 0f, 2f);

        titleText = new BitmapText(font, false);
        titleText.setSize(font.getCharSet().getRenderedSize() * 4f);
        titleText.setText("YOU DIED");
        titleText.setColor(TITLE_COLOR.clone());

        restartText = new BitmapText(font, false);
        restartText.setSize(font.getCharSet().getRenderedSize() * 1.8f);

        exitText = new BitmapText(font, false);
        exitText.setSize(font.getCharSet().getRenderedSize() * 1.8f);

        // Position after setText so getLineWidth is valid
        titleText.setLocalTranslation(
                screenW / 2f - titleText.getLineWidth() / 2f,
                screenH / 2f + 80f,
                3f
        );
        restartText.setLocalTranslation(screenW / 2f - 80f, screenH / 2f - 20f, 3f);
        exitText.setLocalTranslation(   screenW / 2f - 80f, screenH / 2f - 60f, 3f);
    }

    private Geometry buildColorQuad(final float w, final float h, final ColorRGBA color) {
        final Quad     quad = new Quad(w, h);
        final Geometry geo  = new Geometry("YouDiedQuad", quad);
        final Material mat  = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", color.clone());
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        return geo;
    }

    private Geometry buildVignetteQuad(final float w, final float h, final boolean vertical) {
        final Geometry geo = buildColorQuad(w, h, new ColorRGBA(0.5f, 0f, 0f, 0f));
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
        for (final Geometry v : vignetteGeos) guiNode.attachChild(v);
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
        for (final Geometry v : vignetteGeos) guiNode.detachChild(v);
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

        if (timer >= OPTION_DELAY) {
            handleInput();
        }
    }

    private void updateBackground(final float tpf) {
        if (bgAlpha < BG_TARGET_ALPHA) {
            bgAlpha = Math.min(bgAlpha + FADE_SPEED * tpf, BG_TARGET_ALPHA);
            bgGeo.getMaterial().setColor("Color", new ColorRGBA(0f, 0f, 0f, bgAlpha));
        }
    }

    private void updateVignette(final float tpf) {
        // Fade in vignette quickly, then pulse
        if (vignetteAlpha < VIGNETTE_BASE_ALPHA) {
            vignetteAlpha = Math.min(
                    vignetteAlpha + FADE_SPEED * 1.5f * tpf,
                    VIGNETTE_BASE_ALPHA
            );
        }

        // Pulsing sine wave on top of base alpha
        final float pulse = (float) Math.sin(timer * VIGNETTE_PULSE_SPEED)
                * VIGNETTE_PULSE_AMP;
        final float alpha = Math.max(0f, vignetteAlpha + pulse);

        final ColorRGBA vigColor = new ColorRGBA(0.55f, 0f, 0f, alpha);
        for (final Geometry v : vignetteGeos) {
            v.getMaterial().setColor("Color", vigColor.clone());
        }
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

        if (upNow && !lastUp[0]) {
            selectedIndex = (selectedIndex - 1 + 2) % 2;
            refreshSelection();
        }
        if (downNow && !lastDown[0]) {
            selectedIndex = (selectedIndex + 1) % 2;
            refreshSelection();
        }
        if (aNow && !lastA[0]) {
            if (selectedIndex == 0) onRestart.run();
            else                    onExit.run();
        }

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