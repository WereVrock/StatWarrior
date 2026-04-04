package render3d;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;

public final class HitFlash {

    private static final float FLASH_DURATION = 0.18f;
    private static final float MAX_ALPHA      = 0.45f;

    private final Geometry quad;
    private final Material mat;
    private float timer = 0f;

    public HitFlash(final AppSettings settings) {
        final Quad shape = new Quad(settings.getWidth(), settings.getHeight());
        quad = new Geometry("HitFlash", shape);

        mat = new Material(
                GameApplication.APP.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", new ColorRGBA(1f, 0f, 0f, 0f));
        mat.getAdditionalRenderState().setBlendMode(
                com.jme3.material.RenderState.BlendMode.Alpha
        );

        quad.setMaterial(mat);
        quad.setLocalTranslation(0f, 0f, 0f);

        GameApplication.APP.getGuiNode().attachChild(quad);
    }

    public void trigger() {
        timer = FLASH_DURATION;
    }

    public void update(final float tpf) {
        if (timer <= 0f) return;

        timer -= tpf;
        final float alpha = Math.max(0f, (timer / FLASH_DURATION) * MAX_ALPHA);
        mat.setColor("Color", new ColorRGBA(1f, 0f, 0f, alpha));
    }
}