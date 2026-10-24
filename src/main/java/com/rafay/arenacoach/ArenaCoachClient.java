package com.rafay.arenacoach;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class ArenaCoachClient implements ClientModInitializer {
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("arena_coach", "main"));
    private static KeyBinding toggleModeKey;
    private static KeyBinding lockTargetKey;
    private static KeyBinding clearTargetKey;

    private static boolean enabled = false;
    private static Entity lockedTarget = null;
    private static Entity glowingTarget = null;
    private static int combo = 0;
    private static int previousHurtTime = 0;
    private static long lastHitMillis = 0;
    private static String statusMessage = "";
    private static long statusUntil = 0;

    @Override
    public void onInitializeClient() {
        toggleModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.arena_coach.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));
        lockTargetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.arena_coach.lock", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY));
        clearTargetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.arena_coach.clear", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
        HudElementRegistry.addLast(Identifier.of("arena_coach", "hud"),
                (drawContext, tickCounter) -> renderHud(MinecraftClient.getInstance(), drawContext));
    }

    private static void tick(MinecraftClient client) {
        while (toggleModeKey.wasPressed()) {
            enabled = !enabled;
            if (!enabled) clearLockedTarget();
            message(enabled ? "Arena Coach enabled" : "Arena Coach disabled", client);
        }

        if (client.player == null || client.world == null) {
            clearLockedTarget();
            return;
        }

        while (lockTargetKey.wasPressed()) {
            if (!enabled) {
                message("Press M to enable Arena Coach first", client);
                continue;
            }
            Entity lookingAt = getLookedAtEntity(client);
            if (lookingAt instanceof PlayerEntity && lookingAt != client.player) {
                setLockedTarget(lookingAt);
                message("Locked: " + lookingAt.getName().getString(), client);
            } else if (lookingAt != null && lookingAt != client.player) {
                setLockedTarget(lookingAt);
                message("Target locked", client);
            } else {
                message("Aim at an entity, then press N", client);
            }
        }

        while (clearTargetKey.wasPressed()) {
            clearLockedTarget();
            message("Target cleared", client);
        }

        if (!enabled) return;

        if (lockedTarget != null && (!lockedTarget.isAlive() || lockedTarget.isRemoved() || lockedTarget.getEntityWorld() != client.world)) {
            clearLockedTarget();
            message("Target lost", client);
        }

        // Combo is a practice metric only: it observes the selected target's hurt animation.
        if (lockedTarget != null) {
            int hurt = lockedTarget instanceof LivingEntity living
                    ? living.hurtTime
                    : 0;
            if (hurt > 0 && previousHurtTime == 0) {
                long now = System.currentTimeMillis();
                if (now - lastHitMillis <= 2200) combo++;
                else combo = 1;
                lastHitMillis = now;
            }
            previousHurtTime = hurt;
        } else {
            previousHurtTime = 0;
            combo = 0;
        }
    }

    private static Entity getLookedAtEntity(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit) return entityHit.getEntity();
        return null;
    }

    private static void setLockedTarget(Entity target) {
        if (glowingTarget != null) glowingTarget.setGlowing(false);
        lockedTarget = target;
        glowingTarget = target;
        if (glowingTarget != null) glowingTarget.setGlowing(true);
        combo = 0;
        previousHurtTime = 0;
    }

    private static void clearLockedTarget() {
        if (glowingTarget != null) glowingTarget.setGlowing(false);
        glowingTarget = null;
        lockedTarget = null;
        combo = 0;
        previousHurtTime = 0;
    }

    private static void message(String text, MinecraftClient client) {
        statusMessage = text;
        statusUntil = System.currentTimeMillis() + 2200;
        if (client.player != null) client.player.sendMessage(Text.literal("§6[Arena Coach] §f" + text), true);
    }

    private static void renderHud(MinecraftClient client, net.minecraft.client.gui.DrawContext ctx) {
        if (!enabled || client.player == null) return;

        final int x = 8;
        int y = 8;
        final int panelWidth = 196;
        final int lineHeight = 12;
        ctx.fill(x - 5, y - 5, x + panelWidth, y + 102, 0xC9101728);
        ctx.fill(x - 5, y - 5, x + panelWidth, y + 13, 0xFFB8862B);
        ctx.drawTextWithShadow(client.textRenderer, "ARENA COACH", x, y, 0xFFFFFFFF);
        y += 20;

        ctx.drawTextWithShadow(client.textRenderer, "Mode: ON | Manual only", x, y, 0xFFBFE7C5);
        y += lineHeight;

        if (lockedTarget != null && lockedTarget.isAlive()) {
            double distance = client.player.distanceTo(lockedTarget);
            ctx.drawTextWithShadow(client.textRenderer,
                    "Target: " + lockedTarget.getName().getString(), x, y, 0xFFFFD76A);
            y += lineHeight;
            ctx.drawTextWithShadow(client.textRenderer,
                    String.format(java.util.Locale.ROOT, "Distance: %.1fm", distance), x, y, 0xFFE4EAF5);
            y += lineHeight;
            ctx.drawTextWithShadow(client.textRenderer, "Combo: " + combo, x, y, 0xFFE4EAF5);
        } else {
            ctx.drawTextWithShadow(client.textRenderer, "Target: none (aim + N)", x, y, 0xFFE4EAF5);
            y += lineHeight;
            ctx.drawTextWithShadow(client.textRenderer, "Combo: --", x, y, 0xFFE4EAF5);
        }

        y += lineHeight;
        float cooldown = client.player.getAttackCooldownProgress(tickDelta(client));
        String cooldownText = cooldown >= 0.9F ? "READY"
                : String.format(java.util.Locale.ROOT, "%.0f%%", cooldown * 100.0F);
        ctx.drawTextWithShadow(client.textRenderer, "Attack cooldown: " + cooldownText, x, y,
                cooldown >= 0.9F ? 0xFF75F59A : 0xFFFFD76A);
        y += lineHeight;

        boolean falling = client.player.getVelocity().y < -0.08;
        boolean critWindow = !client.player.isOnGround()
                && falling
                && !client.player.isClimbing()
                && !client.player.isTouchingWater()
                && !client.player.hasVehicle()
                && !client.player.isSprinting();
        ctx.drawTextWithShadow(client.textRenderer,
                "Critical timing: " + (critWindow ? "POSSIBLE" : "not now"), x, y,
                critWindow ? 0xFF75F59A : 0xFFFF8C8C);

        if (System.currentTimeMillis() < statusUntil && !statusMessage.isEmpty()) {
            ctx.drawCenteredTextWithShadow(client.textRenderer, statusMessage,
                    ctx.getScaledWindowWidth() / 2, ctx.getScaledWindowHeight() - 50, 0xFFFFD76A);
        }
    }

    private static float tickDelta(MinecraftClient client) {
        return client.getRenderTickCounter().getTickDelta(false);
    }
}\n}\n