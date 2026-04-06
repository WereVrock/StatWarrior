package entity.enemy;

import entity.enemy.EnemyBody;
import balance.Balance;

import java.util.Random;

public final class EnemyWander {

    private static final Random RANDOM = new Random();

    private float wanderTimer;
    private float dirX, dirY;

    private final EnemyBody  body;
    private final EnemySteer steer;
    private final String     id;

    public EnemyWander(final EnemyBody body, final EnemySteer steer, final String id) {
        this.body  = body;
        this.steer = steer;
        this.id    = id;
        pickNewDir();
    }

    public void update(final float tpf) {
        wanderTimer -= tpf;
        if (wanderTimer <= 0f) pickNewDir();

        body.accelerate(dirX * Balance.ENEMY_ACCELERATION,
                        dirY * Balance.ENEMY_ACCELERATION);
        steer.applyWallRepulsion();
        body.clampSpeed(Balance.ENEMY_WANDER_SPEED);
    }

    private void pickNewDir() {
        wanderTimer = Balance.ENEMY_WANDER_DIR_CHANGE_INTERVAL
                + RANDOM.nextFloat() * Balance.ENEMY_WANDER_DIR_CHANGE_VARIANCE;
        final float angle = RANDOM.nextFloat() * (float)(Math.PI * 2);
        dirX = (float) Math.cos(angle);
        dirY = (float) Math.sin(angle);
        log("new direction angle=" + String.format("%.2f", Math.toDegrees(angle)) + " deg");
    }

    private void log(final String msg) {
        System.out.println("[" + id + "][Wander] " + msg);
    }
}