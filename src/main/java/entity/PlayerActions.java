// ===== entity/PlayerActions.java =====
package entity;

import controls.InputController;
import main.Main;

/**
 * Handles dodge and parry input, state, and cooldowns for the player.
 * Physics are applied through the Player's velocity fields via callbacks.
 */
public final class PlayerActions {

    private PlayerState state = PlayerState.NORMAL;

    private float dodgeCooldown  = 0f;
    private float parryCooldown  = 0f;
    private float stateTimer     = 0f;

    private float dodgeDirX = 0f;
    private float dodgeDirY = -1f;

    private boolean parryConsumed = false;

    private boolean lastX = false;
    private boolean lastA = false;

    private final InputController controller;

    public PlayerActions(final InputController controller) {
        this.controller = controller;
    }

    /**
     * Called once per frame. Returns a velocity impulse to apply [vx, vy],
     * or [0,0] if none. Also mutates state/timers.
     */
    public float[] update(final float tpf,
                          final float inputDirX, final float inputDirY,
                          final float currentVx,  final float currentVy) {
        dodgeCooldown -= tpf;
        parryCooldown -= tpf;
        if (dodgeCooldown < 0f) dodgeCooldown = 0f;
        if (parryCooldown < 0f) parryCooldown = 0f;

        final boolean xPressed = controller.isButtonPressed("X");
        final boolean aPressed = controller.isButtonPressed("A");

        final boolean xJustPressed = xPressed && !lastX;
        final boolean aJustPressed = aPressed && !lastA;

        lastX = xPressed;
        lastA = aPressed;

        float[] impulse = {0f, 0f};

        switch (state) {

            case NORMAL -> {
                if (xJustPressed && dodgeCooldown <= 0f) {
                    startDodge(inputDirX, inputDirY, currentVx, currentVy);
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

    private void startDodge(final float inputDirX, final float inputDirY,
                             final float currentVx,  final float currentVy) {
        final float hasInput = (float) Math.sqrt(inputDirX * inputDirX + inputDirY * inputDirY);

        if (hasInput > 0.1f) {
            dodgeDirX = inputDirX / hasInput;
            dodgeDirY = inputDirY / hasInput;
        } else {
            // No input — dodge backward relative to camera facing
            final float yaw = Main.THIRD_PERSON_CAMERA.getYaw();
            dodgeDirX = -(float) Math.sin(yaw);
            dodgeDirY = -(float) Math.cos(yaw);
        }

        state      = PlayerState.DODGING;
        stateTimer = PlayerConstants.DODGE_DURATION;
    }

    private void startParry() {
        state         = PlayerState.PARRYING;
        stateTimer    = PlayerConstants.PARRY_DURATION;
        parryConsumed = false;
    }

    /**
     * Called when something would hit the player from a given world direction.
     * Returns true if the parry absorbed the hit (and consuming it).
     * dirX/dirY: normalized vector FROM attacker TO player.
     */
    public boolean tryParry(final float fromX, final float fromY,
                            final float toX,   final float toY) {
        if (state != PlayerState.PARRYING || parryConsumed) return false;

        // Only parry hits from the front: dot(facing, attackDir) > 0
        final float yaw     = Main.THIRD_PERSON_CAMERA.getYaw();
        final float facingX = (float) Math.sin(yaw);
        final float facingY = (float) Math.cos(yaw);

        final float dx   = toX - fromX;
        final float dy   = toY - fromY;
        final float len  = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) return false;

        final float dot = (dx / len) * facingX + (dy / len) * facingY;
        if (dot > 0f) {
            parryConsumed = true;
            parryCooldown = PlayerConstants.PARRY_COOLDOWN;
            state         = PlayerState.NORMAL;
            return true;
        }
        return false;
    }

    public boolean isDodging()      { return state == PlayerState.DODGING; }
    public boolean isDodgeFreeze()  { return state == PlayerState.DODGE_FREEZE; }
    public boolean isParrying()     { return state == PlayerState.PARRYING; }
    public boolean isInvincible()   { return state == PlayerState.DODGING; }

    public float getDodgeCooldownFraction()  {
        return 1f - (dodgeCooldown / PlayerConstants.DODGE_COOLDOWN);
    }
    public float getParryCooldownFraction()  {
        return 1f - (parryCooldown / PlayerConstants.PARRY_COOLDOWN);
    }

    public PlayerState getState() { return state; }
}