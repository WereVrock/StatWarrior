package main;

import dungeon.Dungeon;
import ui.GameFrame;

public final class Main {

    public static final Dungeon DUNGEON = new Dungeon();

    private Main() {
        // prevent instantiation
    }

    public static void main(final String[] args) {
        new GameFrame();
    }
}