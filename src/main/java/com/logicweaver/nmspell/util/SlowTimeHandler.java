package com.logicweaver.nmspell.util;

import com.logicweaver.nmspell.soul.PlayerSoul;
import com.logicweaver.nmspell.soul.PlayerSoulProvider;
import com.min01.tickrateapi.util.TickrateUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class SlowTimeHandler {

    public static final int BASE_TICKRATE = 20;
    private static Map<Player, Float> playersSlowingTime = new HashMap<>();

    public static void ToggleSlowTime(Player player){
        if (playersSlowingTime.containsKey(player) ){
            System.out.println("Removing player");
            resetPlayerTime((ServerPlayer) player);
        } else {
            player.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
                System.out.println("Adding player");
                slowTimeAroundPlayer((ServerPlayer) player, soul);
            });
        }
    }

    public static void slowTimeAroundPlayer(ServerPlayer player, PlayerSoul soul) {
        float tickrate = 20*(1.0f - (soul.getRank() * 0.1f));
        float lowestTickrate = tickrate;

        playersSlowingTime.put(player, tickrate);

        for (Map.Entry<Player, Float> entry : playersSlowingTime.entrySet()) {
            if (entry.getValue() >= tickrate) { continue; }
            lowestTickrate = entry.getValue();
        }

        System.out.println(lowestTickrate);

        TickrateUtil.setLevelTickrate(player.level().dimension(), lowestTickrate);

    }

    public static void  resetPlayerTime(ServerPlayer player) {
        float lowestTickrate = BASE_TICKRATE;
        playersSlowingTime.remove(player);

        for (Map.Entry<Player, Float> entry : playersSlowingTime.entrySet()) {
            if (entry.getValue() >= lowestTickrate) { continue; }
            lowestTickrate = entry.getValue();
        }

        TickrateUtil.setLevelTickrate(player.level().dimension(), lowestTickrate);
    }

    public static Map<Player, Float> getPlayersSlowingTime() {
        return playersSlowingTime;
    }
}