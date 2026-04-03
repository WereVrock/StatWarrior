package render3d;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import main.Main;
import dungeon.DungeonCell;

public final class DungeonRenderer3D {

    private static final float TILE_SIZE = 1f;

    private DungeonRenderer3D() {}

    public static void renderDungeon() {

        final var grid = Main.DUNGEON.getGrid();

        for (int y = 0; y < grid.length; y++) {
            DungeonCell[] row = grid[y];

            for (int x = 0; x < row.length; x++) {

                DungeonCell cell = row[x];

                if (!cell.isActive()) continue;

                Box box = new Box(0.5f, 0.1f, 0.5f);

                Geometry geo = new Geometry("Tile", box);

                geo.setLocalTranslation(
                        x * TILE_SIZE,
                        0,
                        y * TILE_SIZE
                );

                Material mat = new Material(
                        GameApplication.APP.getAssetManager(),
                        "Common/MatDefs/Misc/Unshaded.j3md"
                );

                geo.setMaterial(mat);

                GameApplication.APP.getRootNode().attachChild(geo);
            }
        }
    }
}