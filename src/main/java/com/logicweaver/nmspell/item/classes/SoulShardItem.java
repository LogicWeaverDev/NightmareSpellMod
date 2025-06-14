package com.logicweaver.nmspell.item.classes;

import com.logicweaver.nmspell.networking.ModNetworking;
import com.logicweaver.nmspell.networking.packet.SoulDataSyncS2CPacket;
import com.logicweaver.nmspell.soul.PlayerSoulProvider;
import com.logicweaver.nmspell.util.HierarchyUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicReference;

public class SoulShardItem extends Item {

    private final int rank;

    public SoulShardItem(Properties p_41383_, int rank) {
        super(p_41383_);
        this.rank = rank;
    }

    private double calculateFragmentValue(int shardRank, int playerRank) {
        int rankDifference = shardRank - playerRank;

        if (rankDifference < 0) {
            return 0.0;
        } else if (rankDifference == 0) {
            return 1.0;
        } else if (rankDifference <= 2) {
            return Math.pow(2, rankDifference);
        } else {
            return Math.pow(2, rankDifference + 1); // adds a "jump" at diff 3
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        boolean playerShifting = player.isShiftKeyDown();

        if (!level.isClientSide()) {
            try {
                AtomicReference<Double> shardsBeforeUse = new AtomicReference<>(0.0);
                AtomicReference<Double> shardsAfter = new AtomicReference<>(0.0);
                AtomicReference<Boolean> success = new AtomicReference<>(false);

                player.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
                    shardsBeforeUse.set(soul.getSoul_Fragments());
                    int count = playerShifting ? item.getCount() : 1;

                    // Calculate fragment value once
                    double fragmentValue = calculateFragmentValue(rank, soul.getRank());

                    // Only proceed if the shard can actually give fragments
                    if (fragmentValue > 0) {
                        // Add fragments for the number of shards being consumed
                        soul.addSoulFragments(fragmentValue * count);
                        shardsAfter.set(soul.getSoul_Fragments());

                        // Consume the items
                        item.shrink(count);
                        success.set(true);
                    }
                });

                if (success.get()) {
                    // Sync soul data to client if this is a server player
                    return InteractionResultHolder.consume(item);
                } else {
                    return InteractionResultHolder.pass(item);
                }
            } catch (Exception e) {
                System.err.println("Error in soul shard use: " + e.getMessage());
                e.printStackTrace();
                return InteractionResultHolder.fail(item);
            }
        }

        return InteractionResultHolder.pass(item);
    }

    @Override
    public Component getName(ItemStack stack) {
        ChatFormatting color = HierarchyUtils.getColor(rank);

        return Component.literal(HierarchyUtils.getAscensionRank(rank)+" Soul Shard")
                .withStyle(color);
    }

    public int getRank() {
        return rank;
    }
}
