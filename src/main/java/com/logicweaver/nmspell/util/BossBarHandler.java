package com.logicweaver.nmspell.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarHandler {
    public static final Map<UUID, ServerBossEvent> BOSS_EVENT = new HashMap<>();

    public static void createBossBar(LivingEntity entity, Component name) {
        ServerBossEvent bossEvent  = new ServerBossEvent(
                name,
                BossEvent.BossBarColor.WHITE,
                BossEvent.BossBarOverlay.NOTCHED_10
        );

        BOSS_EVENT.put(entity.getUUID(), bossEvent);
    }

    public static ServerBossEvent removeBossBar(LivingEntity entity) {
        return BOSS_EVENT.remove(entity.getUUID());
    }

    public static ServerBossEvent getBossBar(LivingEntity entity) {
        return BOSS_EVENT.get(entity.getUUID());
    }


}
