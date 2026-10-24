package com.rafay.arenacoach;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> renderHud(MinecraftClient.getInstance(), drawContext));
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

        int x = 8;
        int y = 8;
        ctx.fill(x - 4, y - 4, x + 172, y + 83, 0x99070D18);
        ctx.drawTextWithShadow(client.textRenderer, Text.literal("§6§lARENA COACH"), x, y, 0xFFFFFF);
        y += 13;
        ctx.drawTextWithShadow(client.textRenderer, Text.literal("§aMode: ON §8| §fManual combat only"), x, y, 0xFFFFFF);
        y += 13;

        if (lockedTarget != null) {
            double distance = client.player.distanceTo(lockedTarget);
            ctx.drawTextWithShadow(client.textRenderer,
                    Text.literal("§eTarget: §f" + lockedTarget.getName().getString()), x, y, 0xFFFFFF);
            y += 12;
            ctx.drawTextWithShadow(client.textRenderer,
                    Text.literal(String.format(java.util.Locale.ROOT, "§7Distance: §f%.1fm §8| §7Combo: §f%d", distance, combo)), x, y, 0xFFFFFF);
        } else {
            ctx.drawTextWithShadow(client.textRenderer, Text.literal("§7Target: none (look + N)"), x, y, 0xFFFFFF);
        }

        y += 13;
        float cooldown = client.player.getAttackCooldownProgress(0.0F);
        String cooldownText = cooldown >= 0.9F ? "§aREADY" : String.format(java.util.Locale.ROOT, "§e%.0f%%", cooldown * 100.0F);
        ctx.drawTextWithShadow(client.textRenderer, Text.literal("§7Attack cooldown: " + cooldownText), x, y, 0xFFFFFF);
        y += 12;
        boolean critWindow = !client.player.isOnGround()
                && client.player.getVelocity().y < 0.0
                && !client.player.isClimbing()
                && !client.player.isTouchingWater()
                && !client.player.hasVehicle();
        ctx.drawTextWithShadow(client.textRenderer,
                Text.literal("§7Critical timing: " + (critWindow ? "§aPossible" : "§cNot now")), x, y, 0xFFFFFF);

        if (System.currentTimeMillis() < statusUntil && !statusMessage.isEmpty()) {
            int sw = client.textRenderer.getWidth(statusMessage);
            ctx.drawTextWithShadow(client.textRenderer, Text.literal("§6" + statusMessage),
                    (ctx.getScaledWindowWidth() - sw) / 2, ctx.getScaledWindowHeight() - 50, 0xFFFFFF);
        }
    }
}
