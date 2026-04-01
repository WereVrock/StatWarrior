package dungeon;

public final class Dungeon {

    private final DungeonCell[][] grid;

    public Dungeon() {
        this.grid = DungeonGenerator.generate(DungeonType.PLUS);
    }

    public DungeonCell[][] getGrid() {
        return grid;
    }
}