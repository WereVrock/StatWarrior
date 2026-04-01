package dungeon;

public final class DungeonCell {

    private boolean active;

    public DungeonCell(final boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}