package com.rafay.arenacoach;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ArenaCoachServer implements ModInitializer {
    private static final Set<UUID> FLIGHT_GRANTED_BY_MOD = new HashSet<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("arenacoach")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(literal("flight")
                                .executes(context -> toggleFlight(context.getSource().getPlayerOrThrow())))));
    }

    private static int toggleFlight(ServerPlayerEntity player) {
        PlayerAbilities abilities = player.getAbilities();
        UUID id = player.getUuid();

        if (FLIGHT_GRANTED_BY_MOD.remove(id)) {
            abilities.flying = false;
            abilities.allowFlying = false;
            player.sendAbilitiesUpdate();
            player.sendMessage(Text.literal("Arena Coach flight disabled."), false);
            return 1;
        }

        if (!abilities.allowFlying) {
            FLIGHT_GRANTED_BY_MOD.add(id);
        }
        abilities.allowFlying = true;
        abilities.flying = true;
        abilities.setFlySpeed(0.05F);
        player.sendAbilitiesUpdate();
        player.sendMessage(Text.literal("Arena Coach flight enabled. Run /arenacoach flight again to disable."), false);
        return 1;
    }
}
