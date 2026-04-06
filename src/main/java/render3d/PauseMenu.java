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

public final class PauseMenu {

    private static final ColorRGBA OVERLAY_COLOR = new ColorRGBA(0.04f, 0.02f, 0.02f, 0.82f);
    private static final ColorRGBA PANEL_COLOR   = new ColorRGBA(0.10f, 0.07f, 0.07f, 0.95f);
    private static final ColorRGBA TEXT_NORMAL   = new ColorRGBA(0.72f, 0.65f, 0.55f, 1.00f);
    private static final ColorRGBA TEXT_SELECTED = new ColorRGBA(0.92f, 0.78f, 0.32f, 1.00f);
    private static final ColorRGBA SEPARATOR     = new ColorRGBA(0.45f, 0.30f, 0.15f, 0.60f);

    private static final float PANEL_W   = 360f;
    private static final float PANEL_H   = 300f;
    private static final float ITEM_H    = 46f;
    private static final float FONT_MAIN = 1.4f;

    private final Node root    = new Node("PauseMenu");
    private final Node optRoot = new Node("OptionsMenu");

    private boolean visible   = false;
    private boolean inOptions = false;
    private int     menuIndex    = 0;
    private int     optionsIndex = 0;

    private static final String[] MENU_ITEMS = { "Continue", "Restart", "Options", "Exit" };

    private final BitmapText[] menuTexts;
    private final BitmapText[] optTexts;

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

        final Geometry overlay = rect(assets, screenW, screenH, OVERLAY_COLOR);
        overlay.setLocalTranslation(0f, 0f, 2f);
        root.attachChild(overlay);

        final Geometry panel = rect(assets, PANEL_W, PANEL_H, PANEL_COLOR);
        panel.setLocalTranslation(panelX, panelY, 3f);
        root.attachChild(panel);

        final Geometry sep = rect(assets, PANEL_W - 40f, 1f, SEPARATOR);
        sep.setLocalTranslation(panelX + 20f, panelY + PANEL_H - 58f, 4f);
        root.attachChild(sep);

        final BitmapText title = new BitmapText(font);
        title.setSize(font.getCharSet().getRenderedSize() * 1.6f);
        title.setColor(TEXT_SELECTED);
        title.setText("— PAUSED —");
        title.setLocalTranslation(
                screenW / 2f - title.getLineWidth() / 2f,
                panelY + PANEL_H - 16f, 5f);
        root.attachChild(title);

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

        optTexts = new BitmapText[1];

        final BitmapText backText = new BitmapText(font);
        backText.setSize(font.getCharSet().getRenderedSize() * FONT_MAIN);
        backText.setLocalTranslation(
                panelX + 40f,
                panelY + PANEL_H - 74f,
                5f);

        optTexts[0] = backText;
        optRoot.attachChild(backText);

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

    // ✅ RESTORED (required by your project)
    public boolean isVisible() {
        return visible;
    }

    public void moveUp() {
        if (inOptions) {
            optionsIndex = 0;
            refreshOptions();
        } else {
            menuIndex = (menuIndex - 1 + MENU_ITEMS.length) % MENU_ITEMS.length;
            refreshMenu();
        }
    }

    public void moveDown() {
        if (inOptions) {
            optionsIndex = 0;
            refreshOptions();
        } else {
            menuIndex = (menuIndex + 1) % MENU_ITEMS.length;
            refreshMenu();
        }
    }

    public void select() {
        if (inOptions) {
            back();
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
        final boolean sel = (optionsIndex == 0);
        optTexts[0].setColor(sel ? TEXT_SELECTED : TEXT_NORMAL);
        optTexts[0].setText((sel ? "> " : "  ") + "Back");
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