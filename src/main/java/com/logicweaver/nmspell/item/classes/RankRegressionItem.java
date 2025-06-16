package com.logicweaver.nmspell.item.classes;

import com.logicweaver.nmspell.soul.PlayerSoulProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicBoolean;

public class RankRegressionItem extends Item {

    public RankRegressionItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack item = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide()) {
            AtomicBoolean success = new AtomicBoolean(false);
            pPlayer.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
               if (soul.getRank() < soul.GLOBAL_MAX && soul.getSoul_Fragments() < soul.getMax_soul_fragments()) { return; }

               soul.setRank(1);

               if (soul.getSoul_cores() == soul.GLOBAL_MAX) {
                   soul.setSoul_fragments((int) soul.getMax_soul_fragments());
               } else {
                   soul.setClass(soul.getSoul_cores() + 1);
               }

               success.set(true);
            });
            if (success.get()) {
                item.shrink(1);
                return InteractionResultHolder.success(item);
            } else {
                pPlayer.sendSystemMessage(Component.literal("You can not use this item."));
                return InteractionResultHolder.pass(item);
            }
        }

        return InteractionResultHolder.pass(item);
    }
}
