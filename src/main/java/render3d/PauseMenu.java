// ===== render3d/PauseMenu.java =====
package render3d;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import main.Main;

/**
 * In-game pause menu rendered via jME guiNode.
 * Dark fantasy aesthetic — charcoal overlay, gold selection.
 */
public final class PauseMenu {

    // ===== Colors =====
    private static final ColorRGBA OVERLAY_COLOR   = new ColorRGBA(0.04f, 0.02f, 0.02f, 0.82f);
    private static final ColorRGBA PANEL_COLOR     = new ColorRGBA(0.10f, 0.07f, 0.07f, 0.95f);
    private static final ColorRGBA TEXT_NORMAL     = new ColorRGBA(0.72f, 0.65f, 0.55f, 1.00f);
    private static final ColorRGBA TEXT_SELECTED   = new ColorRGBA(0.92f, 0.78f, 0.32f, 1.00f); // gold
    private static final ColorRGBA TEXT_SELECTED_S = new ColorRGBA(0.60f, 0.20f, 0.20f, 1.00f); // blood red accent
    private static final ColorRGBA SEPARATOR_COLOR = new ColorRGBA(0.45f, 0.30f, 0.15f, 0.60f);

    // ===== Layout =====
    private static final float PANEL_W    = 320f;
    private static final float PANEL_H    = 260f;
    private static final float ITEM_H     = 48f;
    private static final float FONT_SCALE = 1.4f;

    private final Node     root      = new Node("PauseMenu");
    private final Node     optRoot   = new Node("OptionsMenu");
    private boolean        visible   = false;
    private boolean        inOptions = false;

    private int menuIndex    = 0;
    private int optionsIndex = 0;

    private final String[] menuItems    = { "Continue", "Restart", "Options", "Exit" };
    private final String[] optionsItems = { "First Person: ON", "First Person: OFF", "Back" };

    private final BitmapText[] menuTexts;
    private final BitmapText[] optionsTexts;

    private final Runnable onContinue;
    private final Runnable onRestart;

    public PauseMenu(final BitmapFont font,
                     final float screenW, final float screenH,
                     final AssetManager assets,
                     final Runnable onContinue,
                     final Runnable onRestart) {
        this.onContinue = onContinue;
        this.onRestart  = onRestart;

        final float cx = screenW / 2f;
        final float cy = screenH / 2f;

        // Full-screen dark overlay
        final Geometry overlay = buildRect(assets, screenW, screenH, OVERLAY_COLOR, 0f);
        overlay.setLocalTranslation(0f, 0f, 2f);
        root.attachChild(overlay);

        // Panel background
        final float panelX = cx - PANEL_W / 2f;
        final float panelY = cy - PANEL_H / 2f;
        final Geometry panel = buildRect(assets, PANEL_W, PANEL_H, PANEL_COLOR, 3f);
        panel.setLocalTranslation(panelX, panelY, 3f);
        root.attachChild(panel);

        // Separator line under title area
        final Geometry sep = buildRect(assets, PANEL_W - 40f, 1f, SEPARATOR_COLOR, 4f);
        sep.setLocalTranslation(panelX + 20f, panelY + PANEL_H - 60f, 4f);
        root.attachChild(sep);

        // Title
        final BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 1.6f);
        title.setColor(TEXT_SELECTED);
        title.setText("— PAUSED —");
        title.setLocalTranslation(
                cx - title.getLineWidth() / 2f,
                panelY + PANEL_H - 18f,
                5f
        );
        root.attachChild(title);

        // Menu items
        menuTexts = new BitmapText[menuItems.length];
        for (int i = 0; i < menuItems.length; i++) {
            final BitmapText t = new BitmapText(font);
            t.setSize(font.getCharSet().getRenderedSize() * FONT_SCALE);
            t.setLocalTranslation(
                    panelX + 40f,
                    panelY + PANEL_H - 80f - i * ITEM_H,
                    5f
            );
            menuTexts[i] = t;
            root.attachChild(t);
        }

        // Options sub-panel
        optionsTexts = new BitmapText[optionsItems.length];
        for (int i = 0; i < optionsItems.length; i++) {
            final BitmapText t = new BitmapText(font);
            t.setSize(font.getCharSet().getRenderedSize() * FONT_SCALE);
            t.setLocalTranslation(
                    panelX + 40f,
                    panelY + PANEL_H - 80f - i * ITEM_H,
                    5f
            );
            optionsTexts[i] = t;
            optRoot.attachChild(t);
        }
        root.attachChild(optRoot);

        refreshMenuText();
        refreshOptionsText();

        root.setCullHint(Node.CullHint.Always);
        optRoot.setCullHint(Node.CullHint.Always);
    }

    public Node getNode() { return root; }

    public void show() {
        visible   = true;
        inOptions = false;
        menuIndex = 0;
        refreshMenuText();
        root.setCullHint(Node.CullHint.Inherit);
        optRoot.setCullHint(Node.CullHint.Always);
    }

    public void hide() {
        visible = false;
        root.setCullHint(Node.CullHint.Always);
    }

    public boolean isVisible() { return visible; }

    public void moveUp() {
        if (inOptions) {
            optionsIndex = (optionsIndex - 1 + optionsItems.length) % optionsItems.length;
            refreshOptionsText();
        } else {
            menuIndex = (menuIndex - 1 + menuItems.length) % menuItems.length;
            refreshMenuText();
        }
    }

    public void moveDown() {
        if (inOptions) {
            optionsIndex = (optionsIndex + 1) % optionsItems.length;
            refreshOptionsText();
        } else {
            menuIndex = (menuIndex + 1) % menuItems.length;
            refreshMenuText();
        }
    }

    public void select() {
        if (inOptions) {
            selectOptions();
        } else {
            selectMenu();
        }
    }

    public void back() {
        if (inOptions) {
            inOptions = false;
            refreshMenuText();
            optRoot.setCullHint(Node.CullHint.Always);
            showMenuTexts();
        } else {
            hide();
            onContinue.run();
        }
    }

    private void selectMenu() {
        switch (menuIndex) {
            case 0 -> { hide(); onContinue.run(); }
            case 1 -> { hide(); onRestart.run(); }
            case 2 -> openOptions();
            case 3 -> AppLifecycle.exit();
        }
    }

    private void openOptions() {
        inOptions    = true;
        optionsIndex = 0;
        refreshOptionsText();
        hideMenuTexts();
        optRoot.setCullHint(Node.CullHint.Inherit);
    }

    private void selectOptions() {
        switch (optionsIndex) {
            case 0 -> Main.THIRD_PERSON_CAMERA.setFirstPersonAllowed(true);
            case 1 -> Main.THIRD_PERSON_CAMERA.setFirstPersonAllowed(false);
            case 2 -> back();
        }
        refreshOptionsText();
    }

    private void refreshMenuText() {
        for (int i = 0; i < menuItems.length; i++) {
            final boolean sel = (i == menuIndex);
            menuTexts[i].setColor(sel ? TEXT_SELECTED : TEXT_NORMAL);
            menuTexts[i].setText(sel ? "> " + menuItems[i] : "  " + menuItems[i]);
        }
    }

    private void refreshOptionsText() {
        final boolean fpAllowed = Main.THIRD_PERSON_CAMERA.isFirstPersonAllowed();
        optionsItems[0] = "First Person: " + (fpAllowed ? "ON" : "OFF");
        for (int i = 0; i < optionsItems.length; i++) {
            final boolean sel = (i == optionsIndex);
            optionsTexts[i].setColor(sel ? TEXT_SELECTED : TEXT_NORMAL);
            optionsTexts[i].setText(sel ? "> " + optionsItems[i] : "  " + optionsItems[i]);
        }
    }

    private void hideMenuTexts() {
        for (final BitmapText t : menuTexts) t.setCullHint(Node.CullHint.Always);
    }

    private void showMenuTexts() {
        for (final BitmapText t : menuTexts) t.setCullHint(Node.CullHint.Inherit);
    }

    private static Geometry buildRect(final AssetManager assets,
                                      final float w, final float h,
                                      final ColorRGBA color, final float z) {
        final Quad     quad = new Quad(w, h);
        final Geometry geo  = new Geometry("Rect", quad);
        final Material mat  = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        return geo;
    }
}