// ===== entity/PlayerActions.java =====
package entity.player;

import controls.InputController;
import main.Main;

public final class PlayerActions {

    private PlayerState state      = PlayerState.NORMAL;
    private float       stateTimer = 0f;

    private float dodgeCooldown  = 0f;
    private float parryCooldown  = 0f;

    private float   dodgeDirX    = 0f;
    private float   dodgeDirY    = -1f;
    private boolean parryConsumed = false;

    private boolean lastX = false;
    private boolean lastA = false;
    private boolean lastB = false;

    private final InputController controller;
    private final PlayerMelee     playerMelee;

    public PlayerActions(final InputController controller) {
        this.controller  = controller;
        this.playerMelee = new PlayerMelee();
    }

    public float[] update(final float tpf,
                          final float inputDirX, final float inputDirY,
                          final float currentVx,  final float currentVy) {
        dodgeCooldown -= tpf;
        parryCooldown -= tpf;
        if (dodgeCooldown < 0f) dodgeCooldown = 0f;
        if (parryCooldown < 0f) parryCooldown = 0f;

        final boolean xPressed     = controller.isButtonPressed("X");
        final boolean aPressed     = controller.isButtonPressed("A");
        final boolean bPressed     = controller.isButtonPressed("B");
        final boolean xJustPressed = xPressed && !lastX;
        final boolean aJustPressed = aPressed && !lastA;
        final boolean bJustPressed = bPressed && !lastB;
        lastX = xPressed;
        lastA = aPressed;
        lastB = bPressed;

        final float[] impulse = { 0f, 0f };

        if (!isActive()) {
            playerMelee.update(tpf, bJustPressed);
        }

        switch (state) {
            case NORMAL -> {
                if (xJustPressed && dodgeCooldown <= 0f) {
                    startDodge(inputDirX, inputDirY);
                } else if (aJustPressed && parryCooldown <= 0f) {
                    startParry();
                }
            }
            case DODGING -> {
                stateTimer -= tpf;
                impulse[0] = dodgeDirX * PlayerConstants.DODGE_SPEED;
                impulse[1] = dodgeDirY * PlayerConstants.DODGE_SPEED;
                if (stateTimer <= 0f) {
                    state      = PlayerState.DODGE_FREEZE;
                    stateTimer = PlayerConstants.DODGE_FREEZE;
                }
            }
            case DODGE_FREEZE -> {
                stateTimer -= tpf;
                if (stateTimer <= 0f) {
                    state         = PlayerState.NORMAL;
                    dodgeCooldown = PlayerConstants.DODGE_COOLDOWN;
                }
            }
            case PARRYING -> {
                stateTimer -= tpf;
                if (stateTimer <= 0f) {
                    state         = PlayerState.NORMAL;
                    parryCooldown = PlayerConstants.PARRY_COOLDOWN;
                    parryConsumed = false;
                }
            }
        }

        return impulse;
    }

    private void startDodge(final float inputDirX, final float inputDirY) {
        final float len = (float) Math.sqrt(
                inputDirX * inputDirX + inputDirY * inputDirY);
        if (len > 0.1f) {
            dodgeDirX = inputDirX / len;
            dodgeDirY = inputDirY / len;
        } else {
            final float yaw = Main.THIRD_PERSON_CAMERA.getYaw();
            dodgeDirX = -(float) Math.sin(yaw);
            dodgeDirY = -(float) Math.cos(yaw);
        }
        state      = PlayerState.DODGING;
        stateTimer = PlayerConstants.DODGE_DURATION;
    }

    private void startParry() {
        System.out.println("parry button pressed");
        state         = PlayerState.PARRYING;
        stateTimer    = PlayerConstants.PARRY_DURATION;
        parryConsumed = false;
    }

    public boolean tryParry(final float attackerX, final float attackerY) {
        if (state != PlayerState.PARRYING || parryConsumed) return false;

        final float px  = Main.PLAYER.centerX();
        final float py  = Main.PLAYER.centerY();
        final float dx  = attackerX - px;
        final float dy  = attackerY - py;
        final float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) return false;

        final float yaw     = Main.THIRD_PERSON_CAMERA.getYaw();
        final float facingX = (float) Math.sin(yaw);
        final float facingY = (float) Math.cos(yaw);

        final float dot = (dx / len) * facingX + (dy / len) * facingY;
        if (dot <= 0f) return false;

        parryConsumed = true;
        state         = PlayerState.NORMAL;
        parryCooldown = 0f;
        stateTimer    = 0f;
        return true;
    }

    private boolean isActive() {
        return state != PlayerState.NORMAL;
    }

    public boolean isDodging()     { return state == PlayerState.DODGING;      }
    public boolean isDodgeFreeze() { return state == PlayerState.DODGE_FREEZE; }
    public boolean isParrying()    { return state == PlayerState.PARRYING;     }
    public boolean isInvincible()  { return state == PlayerState.DODGING;      }
    public boolean isMeleeActive() { return playerMelee.isActive();            }

    public float getDodgeCooldownFraction() {
        return PlayerConstants.DODGE_COOLDOWN > 0f
                ? 1f - (dodgeCooldown / PlayerConstants.DODGE_COOLDOWN) : 1f;
    }
    public float getParryCooldownFraction() {
        return PlayerConstants.PARRY_COOLDOWN > 0f
                ? 1f - (parryCooldown / PlayerConstants.PARRY_COOLDOWN) : 1f;
    }

    /**
     * How much parry duration remains: 1.0 = just started, 0.0 = expired.
     * Only meaningful while isParrying() is true.
     */
    public float getParryDurationFraction() {
        return PlayerConstants.PARRY_DURATION > 0f
                ? stateTimer / PlayerConstants.PARRY_DURATION : 0f;
    }

    public float getMeleeCooldownFraction() {
        return playerMelee.getCooldownFraction();
    }

    public PlayerState getState() { return state; }
}