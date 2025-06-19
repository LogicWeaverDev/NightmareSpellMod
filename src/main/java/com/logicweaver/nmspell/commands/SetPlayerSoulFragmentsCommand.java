package com.logicweaver.nmspell.commands;

import com.logicweaver.nmspell.networking.ModNetworking;
import com.logicweaver.nmspell.networking.packet.SoulDataSyncS2CPacket;
import com.logicweaver.nmspell.soul.PlayerSoul;
import com.logicweaver.nmspell.soul.PlayerSoulProvider;
import com.logicweaver.nmspell.util.HierarchyUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetPlayerSoulFragmentsCommand {
    public SetPlayerSoulFragmentsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soul_fragments")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("number", IntegerArgumentType.integer()).executes(this::setSoul_Fragments))));
    }

    private int setSoul_Fragments(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        CommandSourceStack source = command.getSource();
        ServerPlayer player = source.getPlayer();
        int number = IntegerArgumentType.getInteger(command, "number");

        PlayerSoul soul = player.getCapability(PlayerSoulProvider.PLAYER_SOUL).resolve().orElse(null);

        if (soul != null) {
            soul.setSoul_fragments(number);
            ModNetworking.sendToPlayer(new SoulDataSyncS2CPacket(
                    soul.getSoul_Fragments(),
                    soul.getMax_soul_fragments(),
                    soul.getRank(),
                    soul.getSoul_cores(),
                    soul.getStrongerEntitiesKilled()
            ), player);
            source.sendSystemMessage(Component.literal("Soul fragments set to "+ number));
            return 1;
        }else {
            return -1;
        }
    }
}
