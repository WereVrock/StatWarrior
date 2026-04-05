// ===== render3d/AppLifecycle.java =====
package render3d;

/**
 * Central shutdown point.
 * Call AppLifecycle.exit() from anywhere — menu buttons, window listeners,
 * or the jME app's destroy callback — to terminate cleanly.
 */
public final class AppLifecycle {

    private AppLifecycle() {}

    public static void exit() {
        // Stop the jME engine if it is running
        if (GameApplication.APP != null) {
            try {
                GameApplication.APP.stop(false);
            } catch (final Exception ignored) {}
        }
        // Terminate the process — closes all JFrames and threads
        System.exit(0);
    }
}