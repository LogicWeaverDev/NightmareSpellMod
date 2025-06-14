package com.logicweaver.nmspell.entity;

import com.logicweaver.nmspell.networking.ModNetworking;
import com.logicweaver.nmspell.networking.packet.CorruptSoulDataSyncS2CPacket;
import com.logicweaver.nmspell.networking.packet.SoulDataSyncS2CPacket;
import com.logicweaver.nmspell.soul.SoulStatManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CorruptedSoul implements INBTSerializable<CompoundTag> {
    private double soul_fragments;
    private double max_soul_fragments = 1000;

    private int rank = 0;
    private int soul_cores = 0;

    private final int GLOBAL_MIN = 0;
    private final int GLOBAL_MAX = 7;

    private List<Consumer<CorruptedSoul>> changeListeners = new ArrayList<>();

    private double BASE_HEALTH;
    private double BASE_ATTACK;
    private double BASE_SPEED;
    private double BASE_DEFENSE;

    private LivingEntity associatedEntity;
    private boolean isInitialized;
    private UUID entityUUID; // Store the UUID separately for loading

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public LivingEntity getAssociatedEntity() {
        return associatedEntity;
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
        return (((soul_cores + ((soul_fragments / 1000.0)/soul_cores))/GLOBAL_MAX) * (rank * rank * rank))*2;
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

    public void createNewSoulCore() {
        if (soul_cores == GLOBAL_MAX) {return;}
        soul_fragments -= max_soul_fragments;
        soul_cores += 1;
        if (rank < GLOBAL_MAX) {
            rank += 1;
        }
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

    public void setAssociatedEntity(LivingEntity associatedEntity) {
        if (associatedEntity == null) return;
        this.associatedEntity = associatedEntity;
        this.entityUUID = associatedEntity.getUUID();

        this.BASE_HEALTH = associatedEntity.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
        this.BASE_ATTACK = associatedEntity.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
        this.BASE_SPEED = associatedEntity.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        this.BASE_DEFENSE = associatedEntity.getAttribute(Attributes.ARMOR).getBaseValue();

        notifyChange();
    }

    /**
     * Attempts to restore the associated entity from the stored UUID
     * Call this method when the world/level is available (e.g., on world load, tick, etc.)
     */
    public void tryRestoreEntity(ServerLevel level) {
        if (associatedEntity == null && entityUUID != null) {
            Entity entity = level.getEntity(entityUUID);
            if (entity instanceof LivingEntity livingEntity) {
                setAssociatedEntity(livingEntity);
                System.out.println("Successfully restored entity: " + entity.getType().getDescription().getString());
            }
        }
    }

    public void setSoul_fragments(int amount) {
        if (amount < 0) {
            amount = 0;
        }

        if (soul_cores == GLOBAL_MAX) {
            soul_fragments = Math.min(amount, max_soul_fragments);
        }else {
            soul_fragments += amount;
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
        if (rank <= GLOBAL_MIN || soul_cores <= GLOBAL_MIN) return;
        updateStats();
        CorruptSoulDataSyncS2CPacket packet = new CorruptSoulDataSyncS2CPacket(
                entityUUID,
                soul_fragments,
                max_soul_fragments,
                rank,
                soul_cores
        );
        ModNetworking.sendToNearEntity(packet, associatedEntity, 10);
        for (Consumer<CorruptedSoul> listener : changeListeners) {
            listener.accept(this);
        }
    }

    // Calculate stats based on soul progression
    private void updateStats() {
        if (associatedEntity != null && BASE_HEALTH > 0) {
            System.out.println("Updating stats for " + associatedEntity.getType().getDescription().getString());
            System.out.println("Rank: " + rank + ", Soul Cores: " + soul_cores + ", Fragments: " + soul_fragments);
            SoulStatManager.applyStatsToEntity(associatedEntity, this);
        } else {
            System.out.println("Cannot update stats - entity: " + (associatedEntity != null) + ", base health: " + BASE_HEALTH);
        }
    }

    public void copyFrom(CorruptedSoul soul) {
        this.soul_fragments = soul.soul_fragments;
        this.max_soul_fragments = soul.max_soul_fragments;
        this.rank = soul.rank;
        this.soul_cores = soul.soul_cores;
        this.entityUUID = soul.entityUUID;
        // Note: Don't copy the entity reference directly, let it be restored via tryRestoreEntity
    }

    // INBTSerializable implementation
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        if (entityUUID != null) {
            nbt.putUUID("entityUUID", entityUUID);
        }

        nbt.putDouble("soul_fragments", soul_fragments);
        nbt.putDouble("max_soul_fragments", max_soul_fragments);
        nbt.putInt("rank", rank);
        nbt.putInt("soul_cores", soul_cores);

        // Save base stats for restoration
        nbt.putDouble("BASE_HEALTH", BASE_HEALTH);
        nbt.putDouble("BASE_ATTACK", BASE_ATTACK);
        nbt.putDouble("BASE_SPEED", BASE_SPEED);
        nbt.putDouble("BASE_DEFENSE", BASE_DEFENSE);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.hasUUID("entityUUID")) {
            entityUUID = nbt.getUUID("entityUUID");
        }

        soul_fragments = nbt.getDouble("soul_fragments");
        max_soul_fragments = nbt.getDouble("max_soul_fragments");
        rank = nbt.getInt("rank");
        soul_cores = nbt.getInt("soul_cores");

        // Restore base stats
        BASE_HEALTH = nbt.getDouble("BASE_HEALTH");
        BASE_ATTACK = nbt.getDouble("BASE_ATTACK");
        BASE_SPEED = nbt.getDouble("BASE_SPEED");
        BASE_DEFENSE = nbt.getDouble("BASE_DEFENSE");

        // Entity will be restored later via tryRestoreEntity when level is available
    }

    // Legacy methods for backward compatibility
    public void saveNBTData(CompoundTag nbt) {
        CompoundTag soulData = serializeNBT();
        nbt.put("corrupted_soul", soulData);
    }

    public void loadNBTData(CompoundTag nbt) {
        if (nbt.contains("corrupted_soul")) {
            deserializeNBT(nbt.getCompound("corrupted_soul"));
        }
    }
}
