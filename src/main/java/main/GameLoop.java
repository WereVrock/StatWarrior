package main;

public final class GameLoop implements Runnable {

    private static final int FPS = 60;
    private static final long FRAME_TIME = 1000 / FPS;

    private Thread thread;
    private boolean running;

    public void start() {
        if (running) return;

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        while (running) {

            final long start = System.currentTimeMillis();

            // 🔥 INPUT FIRST
            Main.CONTROLLER.update();

            // 🔥 GAME UPDATE
            Main.PLAYER.update();
            Main.CAMERA.update(Main.PLAYER);

            // 🔥 RENDER REQUEST (safe for Swing)
//            Main.FRAME.repaint();

            final long elapsed = System.currentTimeMillis() - start;
            final long sleep = FRAME_TIME - elapsed;

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}