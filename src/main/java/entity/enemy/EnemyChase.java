package entity.enemy;

import balance.Balance;
import main.Main;

public final class EnemyChase {

    private float   lostPlayerTimer;
    private float   lastKnownX, lastKnownY;
    private boolean hasLastKnown;

    private final EnemyBody     body;
    private final EnemyDetector detector;
    private final EnemySteer    steer;
    private final String        id;

    public EnemyChase(final EnemyBody body, final EnemyDetector detector,
                      final EnemySteer steer, final String id) {
        this.body     = body;
        this.detector = detector;
        this.steer    = steer;
        this.id       = id;
    }

    public void reset() {
        lostPlayerTimer = 0f;
        updateLastKnown();
    }

    public void clearLastKnown() {
        hasLastKnown = false;
    }

    /** Returns true if lost-player timeout expired and enemy should wander. */
    public boolean update(final float tpf) {
        if (detector.canDetectPlayer()) {
            lostPlayerTimer = 0f;
            updateLastKnown();

            steer.seekTarget(
                    Main.PLAYER.getX() + body.getTileSize() / 2f,
                    Main.PLAYER.getY() + body.getTileSize() / 2f,
                    Balance.ENEMY_CHASE_SPEED
            );
            steer.applyWallRepulsion();

        } else {
            lostPlayerTimer += tpf;
            log("lost player, timeout in "
                    + String.format("%.1f", Balance.ENEMY_LOST_PLAYER_TIMEOUT - lostPlayerTimer) + "s");

            if (lostPlayerTimer >= Balance.ENEMY_LOST_PLAYER_TIMEOUT) {
                return true;
            }

            if (hasLastKnown) {
                final float distToLK = dist(body.centerX(), body.centerY(), lastKnownX, lastKnownY);
                if (distToLK < Balance.ENEMY_WAYPOINT_REACH_DIST) {
                    body.stop();
                    return false;
                }
                steer.seekTarget(lastKnownX, lastKnownY, Balance.ENEMY_CHASE_SPEED);
                steer.applyWallRepulsion();
            }
        }
        return false;
    }

    private void updateLastKnown() {
        lastKnownX = Main.PLAYER.getX() + body.getTileSize() / 2f;
        lastKnownY = Main.PLAYER.getY() + body.getTileSize() / 2f;
        hasLastKnown = true;
    }

    private static float dist(final float x1, final float y1,
                               final float x2, final float y2) {
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void log(final String msg) {
        System.out.println("[" + id + "][Chase] " + msg);
    }
}