package com.logicweaver.nmspell.util;

import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SlowTimeHandler {
    private static List<Player> playersSlowingTime = new ArrayList<>();

    public static void ToggleSlowTime(Player player){
        if (playersSlowingTime.contains(player) ){
            System.out.println("Removing player");
            playersSlowingTime.remove(player);
        } else {
            System.out.println("Adding player");
            playersSlowingTime.add(player);
        }
    }

    public static List<Player> getPlayersSlowingTime() {
        return playersSlowingTime;
    }
}
