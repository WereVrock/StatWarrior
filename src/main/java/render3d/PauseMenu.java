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

public final class PauseMenu {

    private static final ColorRGBA OVERLAY_COLOR = new ColorRGBA(0.04f, 0.02f, 0.02f, 0.82f);
    private static final ColorRGBA PANEL_COLOR   = new ColorRGBA(0.10f, 0.07f, 0.07f, 0.95f);
    private static final ColorRGBA TEXT_NORMAL   = new ColorRGBA(0.72f, 0.65f, 0.55f, 1.00f);
    private static final ColorRGBA TEXT_SELECTED = new ColorRGBA(0.92f, 0.78f, 0.32f, 1.00f);
    private static final ColorRGBA SEPARATOR     = new ColorRGBA(0.45f, 0.30f, 0.15f, 0.60f);
    private static final ColorRGBA TEXT_HINT     = new ColorRGBA(0.55f, 0.50f, 0.42f, 1.00f);

    private static final float PANEL_W   = 360f;
    private static final float PANEL_H   = 300f;
    private static final float ITEM_H    = 46f;
    private static final float FONT_MAIN = 1.4f;
    private static final float FONT_HINT = 0.9f;

    private final Node root    = new Node("PauseMenu");
    private final Node optRoot = new Node("OptionsMenu");

    private boolean visible   = false;
    private boolean inOptions = false;
    private int     menuIndex    = 0;
    private int     optionsIndex = 0;

    private static final String[] MENU_ITEMS = { "Continue", "Restart", "Options", "Exit" };

    // Options has one toggle item + Back
    private static final int OPT_FP   = 0;
    private static final int OPT_BACK = 1;

    private final BitmapText[] menuTexts;
    private final BitmapText[] optTexts;
    private final BitmapText   fpHintText;   // "Press RS twice for first person"

    private final Runnable onContinue;
    private final Runnable onRestart;

    public PauseMenu(final BitmapFont font,
                     final float screenW, final float screenH,
                     final AssetManager assets,
                     final Runnable onContinue,
                     final Runnable onRestart) {
        this.onContinue = onContinue;
        this.onRestart  = onRestart;

        final float panelX = screenW / 2f - PANEL_W / 2f;
        final float panelY = screenH / 2f - PANEL_H / 2f;

        // Full-screen overlay
        final Geometry overlay = rect(assets, screenW, screenH, OVERLAY_COLOR);
        overlay.setLocalTranslation(0f, 0f, 2f);
        root.attachChild(overlay);

        // Panel background
        final Geometry panel = rect(assets, PANEL_W, PANEL_H, PANEL_COLOR);
        panel.setLocalTranslation(panelX, panelY, 3f);
        root.attachChild(panel);

        // Separator
        final Geometry sep = rect(assets, PANEL_W - 40f, 1f, SEPARATOR);
        sep.setLocalTranslation(panelX + 20f, panelY + PANEL_H - 58f, 4f);
        root.attachChild(sep);

        // Title
        final BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 1.6f);
        title.setColor(TEXT_SELECTED);
        title.setText("— PAUSED —");
        title.setLocalTranslation(
                screenW / 2f - title.getLineWidth() / 2f,
                panelY + PANEL_H - 16f, 5f);
        root.attachChild(title);

        // Main menu texts
        menuTexts = new BitmapText[MENU_ITEMS.length];
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            final BitmapText t = new BitmapText(font);
            t.setSize(font.getCharSet().getRenderedSize() * FONT_MAIN);
            t.setLocalTranslation(
                    panelX + 40f,
                    panelY + PANEL_H - 74f - i * ITEM_H,
                    5f);
            menuTexts[i] = t;
            root.attachChild(t);
        }

        // Options texts — 2 items: toggle + back
        optTexts = new BitmapText[2];
        for (int i = 0; i < 2; i++) {
            final BitmapText t = new BitmapText(font);
            t.setSize(font.getCharSet().getRenderedSize() * FONT_MAIN);
            t.setLocalTranslation(
                    panelX + 40f,
                    panelY + PANEL_H - 74f - i * ITEM_H,
                    5f);
            optTexts[i] = t;
            optRoot.attachChild(t);
        }

        // Hint text shown below the FP toggle when it's ON
        fpHintText = new BitmapText(font);
        fpHintText.setSize(font.getCharSet().getRenderedSize() * FONT_HINT);
        fpHintText.setColor(TEXT_HINT);
        fpHintText.setText("Press Right Stick twice for first person");
        fpHintText.setLocalTranslation(
                panelX + 40f,
                panelY + PANEL_H - 74f - ITEM_H - 22f,
                5f);
        optRoot.attachChild(fpHintText);

        root.attachChild(optRoot);

        refreshMenu();
        refreshOptions();

        root.setCullHint(Node.CullHint.Always);
        optRoot.setCullHint(Node.CullHint.Always);
    }

    public Node getNode() { return root; }

    public void show() {
        visible = true;
        inOptions = false;
        menuIndex = 0;
        refreshMenu();
        root.setCullHint(Node.CullHint.Inherit);
        optRoot.setCullHint(Node.CullHint.Always);
        showTexts(menuTexts);
    }

    public void hide() {
        visible = false;
        root.setCullHint(Node.CullHint.Always);
    }

    public boolean isVisible() { return visible; }

    public void moveUp() {
        if (inOptions) {
            optionsIndex = (optionsIndex - 1 + 2) % 2;
            refreshOptions();
        } else {
            menuIndex = (menuIndex - 1 + MENU_ITEMS.length) % MENU_ITEMS.length;
            refreshMenu();
        }
    }

    public void moveDown() {
        if (inOptions) {
            optionsIndex = (optionsIndex + 1) % 2;
            refreshOptions();
        } else {
            menuIndex = (menuIndex + 1) % MENU_ITEMS.length;
            refreshMenu();
        }
    }

    public void select() {
        if (inOptions) {
            switch (optionsIndex) {
                case OPT_FP -> {
                    // Toggle first person allowed
                    final boolean next = !Main.THIRD_PERSON_CAMERA.isFirstPersonAllowed();
                    Main.THIRD_PERSON_CAMERA.setFirstPersonAllowed(next);
                    refreshOptions();
                }
                case OPT_BACK -> back();
            }
        } else {
            switch (menuIndex) {
                case 0 -> { hide(); onContinue.run(); }
                case 1 -> { hide(); onRestart.run(); }
                case 2 -> openOptions();
                case 3 -> AppLifecycle.exit();
            }
        }
    }

    public void back() {
        if (inOptions) {
            inOptions = false;
            optRoot.setCullHint(Node.CullHint.Always);
            showTexts(menuTexts);
            refreshMenu();
        } else {
            hide();
            onContinue.run();
        }
    }

    private void openOptions() {
        inOptions    = true;
        optionsIndex = 0;
        hideTexts(menuTexts);
        optRoot.setCullHint(Node.CullHint.Inherit);
        refreshOptions();
    }

    private void refreshMenu() {
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            final boolean sel = (i == menuIndex);
            menuTexts[i].setColor(sel ? TEXT_SELECTED : TEXT_NORMAL);
            menuTexts[i].setText((sel ? "> " : "  ") + MENU_ITEMS[i]);
        }
    }

    private void refreshOptions() {
        final boolean fp = Main.THIRD_PERSON_CAMERA.isFirstPersonAllowed();

        final String[] labels = {
            "First Person: " + (fp ? "ON" : "OFF"),
            "Back"
        };

        for (int i = 0; i < 2; i++) {
            final boolean sel = (i == optionsIndex);
            optTexts[i].setColor(sel ? TEXT_SELECTED : TEXT_NORMAL);
            optTexts[i].setText((sel ? "> " : "  ") + labels[i]);
        }

        // Show hint only when FP is enabled
        fpHintText.setCullHint(
                fp ? Node.CullHint.Inherit : Node.CullHint.Always);
    }

    private static void hideTexts(final BitmapText[] texts) {
        for (final BitmapText t : texts) t.setCullHint(Node.CullHint.Always);
    }

    private static void showTexts(final BitmapText[] texts) {
        for (final BitmapText t : texts) t.setCullHint(Node.CullHint.Inherit);
    }

    private static Geometry rect(final AssetManager assets,
                                  final float w, final float h,
                                  final ColorRGBA color) {
        final Geometry geo = new Geometry("Rect", new Quad(w, h));
        final Material mat = new Material(
                assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setMaterial(mat);
        return geo;
    }
}