package com.rafay.arenacoach;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public final class ArenaCoachServer implements ModInitializer {
    private static final Map<UUID, Boolean> ORIGINAL_ALLOW_FLYING = new HashMap<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("arenacoach")
                        .requires(source -> source.getPermissions().test(2))
                        .then(literal("flight")
                                .executes(context -> toggleFlight(context.getSource().getPlayerOrThrow())))));
    }

    private static int toggleFlight(ServerPlayerEntity player) {
        PlayerAbilities abilities = player.getAbilities();
        UUID id = player.getUuid();

        if (ORIGINAL_ALLOW_FLYING.containsKey(id)) {
            boolean originallyAllowed = ORIGINAL_ALLOW_FLYING.remove(id);
            abilities.flying = false;
            abilities.allowFlying = originallyAllowed;
            player.sendAbilitiesUpdate();
            player.sendMessage(Text.literal("Arena Coach flight disabled."), false);
            return 1;
        }

        ORIGINAL_ALLOW_FLYING.put(id, abilities.allowFlying);
        abilities.allowFlying = true;
        abilities.flying = true;
        abilities.setFlySpeed(0.05F);
        player.sendAbilitiesUpdate();
        player.sendMessage(Text.literal("Arena Coach flight enabled. Run /arenacoach flight again to disable."), false);
        return 1;
    }
}
