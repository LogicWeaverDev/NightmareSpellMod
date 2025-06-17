package com.logicweaver.nmspell.soul;

import com.logicweaver.nmspell.networking.ModNetworking;
import com.logicweaver.nmspell.networking.packet.SoulDataSyncS2CPacket;
import com.logicweaver.nmspell.util.SoulStatManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayerSoul {
    private double soul_fragments;
    private double max_soul_fragments = 1000;

    private int rank = 1;
    private int soul_cores = 1;

    private int stronger_entities_killed = 0;

    private final int GLOBAL_MIN = 0;
    public final int GLOBAL_MAX = 7;

    private List<Consumer<PlayerSoul>> changeListeners = new ArrayList<>();

    private double BASE_HEALTH;
    private double BASE_ATTACK;
    private double BASE_SPEED;
    private double BASE_DEFENSE;

    private Player associatedPlayer;

    public void setAssociatedPlayer(Player player) {
        this.associatedPlayer = player;
        this.BASE_HEALTH = player.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
        this.BASE_ATTACK = player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
        this.BASE_SPEED = player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        this.BASE_DEFENSE = player.getAttribute(Attributes.ARMOR).getBaseValue();

        updateStats();
    }

    public PlayerSoul() {
        updateStats();
    }

    public int getRank() {
        return rank;
    }

    public int getSoul_cores() {
        return soul_cores;
    }

    public double getSoul_Fragments() {
        return soul_fragments;
    }

    public double getMax_soul_fragments() {
        return max_soul_fragments;
    }

    public double getBonus() {
        return ((soul_cores + ((soul_fragments / 1000.0)/soul_cores))/GLOBAL_MAX) * (rank * rank * rank);
    }

    public void addSoulFragments(double amount) {
        if (amount <= 0) return;

        if (soul_cores == GLOBAL_MAX) {
            soul_fragments = Math.min(soul_fragments + amount, max_soul_fragments);
        }else {
            soul_fragments += amount;
        }
        if (soul_fragments >= max_soul_fragments) {
            createNewSoulCore();
        }
        notifyChange();
    }

    public void incrementStrongerEntitiesKilled() {
        stronger_entities_killed ++;

        if (stronger_entities_killed >= GLOBAL_MAX) {
            if (rank < GLOBAL_MAX) {
                rank ++;
                stronger_entities_killed  = GLOBAL_MIN;
            }
        }

        notifyChange();
    }

    public int getStrongerEntitiesKilled() {
        return stronger_entities_killed;
    }

    public void createNewSoulCore() {
        if (soul_cores == GLOBAL_MAX) {return;}
        soul_fragments -= max_soul_fragments;
        soul_cores += 1;
        max_soul_fragments = soul_cores * 1000;
        if (soul_fragments >= max_soul_fragments) {
            createNewSoulCore();
        }
    }

    public void setRank(int rank) {
        if (rank < 1) {
            rank = 1;
        } else if (rank > GLOBAL_MAX) {
            rank = GLOBAL_MAX;
        }
        this.rank = rank;
        notifyChange();
    }

    public void setClass(int soul_cores) {
        if (soul_cores < 1) {
            soul_cores = 1;
        } else if (soul_cores > GLOBAL_MAX) {
            soul_cores = GLOBAL_MAX;
        }
        this.soul_cores = soul_cores;
        double fragmentPercent = soul_fragments/max_soul_fragments;
        max_soul_fragments = soul_cores * 1000;
        soul_fragments = max_soul_fragments * fragmentPercent;
        if (soul_cores < GLOBAL_MAX && soul_fragments >= max_soul_fragments) {
            createNewSoulCore();
        }
        notifyChange();
    }

    public void setSoul_fragments(int amount) {
        if (amount < 0) {
            amount = 0;
        }

        if (soul_cores == GLOBAL_MAX) {
            soul_fragments = Math.min(amount, max_soul_fragments);
        }else {
            soul_fragments = amount;
        }
        if (soul_fragments >= max_soul_fragments) {
            createNewSoulCore();
            if (soul_cores == GLOBAL_MAX) {
                soul_fragments = Math.min(amount, max_soul_fragments);
            }
        }
        notifyChange();
    }

    private void notifyChange() {
        updateStats();
        ModNetworking.sendToPlayer(new SoulDataSyncS2CPacket(
                soul_fragments,
                max_soul_fragments,
                rank,
                soul_cores,
                stronger_entities_killed
        ), (ServerPlayer) associatedPlayer);
        for (Consumer<PlayerSoul> listener : changeListeners) {
            listener.accept(this);
        }
    }

    // Calculate stats based on soul progression
    private void updateStats() {
        SoulStatManager.applyStatsToEntity(associatedPlayer, this);
    }

    public void copyFrom(PlayerSoul soul) {
        this.soul_fragments = soul.soul_fragments;
        this.max_soul_fragments = soul.max_soul_fragments;

        this.rank = soul.rank;
        this.soul_cores = soul.soul_cores;
        this.stronger_entities_killed = soul.stronger_entities_killed;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putDouble("soul_fragments", soul_fragments);
        nbt.putDouble("max_soul_fragments", max_soul_fragments);

        nbt.putInt("rank", rank);
        nbt.putInt("soul_cores", soul_cores);
        nbt.putInt("stronger_entities_killed", stronger_entities_killed);
    }

    public void loadNBTData(CompoundTag nbt) {
        soul_fragments = nbt.getDouble("soul_fragments");
        max_soul_fragments = nbt.getDouble("max_soul_fragments");

        rank = nbt.getInt("rank");
        soul_cores = nbt.getInt("soul_cores");
        stronger_entities_killed = nbt.getInt("stronger_entities_killed");
    }

}
