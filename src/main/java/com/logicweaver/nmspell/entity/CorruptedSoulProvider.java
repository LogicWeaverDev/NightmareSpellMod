package com.logicweaver.nmspell.entity;

import com.logicweaver.nmspell.entity.CorruptedSoul;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CorruptedSoulProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<CorruptedSoul> CORRUPTED_SOUL = CapabilityManager.get(new CapabilityToken<CorruptedSoul>() { });

    private CorruptedSoul soul = null;
    private final LazyOptional<CorruptedSoul> optional = LazyOptional.of(this::createCorruptedSoul);
    private final LivingEntity entity; // Store reference to the entity this capability is attached to

    public CorruptedSoulProvider(LivingEntity entity) {
        this.entity = entity;
    }

    private CorruptedSoul createCorruptedSoul() {
        if (this.soul == null) {
            this.soul = new CorruptedSoul();
            // Set the associated entity immediately when creating the soul
//            if (entity != null) {
//                this.soul.setAssociatedEntity(entity);
//                this.soul.setInitialized(true);
//            }
        }
        return this.soul;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if (capability == CORRUPTED_SOUL) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (this.soul != null) {
            // Use the new INBTSerializable methods
            CompoundTag soulData = this.soul.serializeNBT();
            nbt.put("corrupted_soul", soulData);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("corrupted_soul")) {
            createCorruptedSoul().deserializeNBT(nbt.getCompound("corrupted_soul"));

            // Ensure entity association is maintained after loading
            if (entity != null && this.soul != null) {
                this.soul.setAssociatedEntity(entity);
            }
        }
    }

    /**
     * Call this method to invalidate the capability when the entity is removed
     */
    public void invalidate() {
        optional.invalidate();
    }

    /**
     * Get the soul instance (can be null if not created yet)
     */
    @Nullable
    public CorruptedSoul getSoul() {
        return this.soul;
    }
}