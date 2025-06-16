package com.logicweaver.nmspell.util;

import com.logicweaver.nmspell.entity.CorruptedSoul;
import com.logicweaver.nmspell.soul.PlayerSoul;
import com.logicweaver.nmspell.soul.PlayerSoulProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulStatManager {
    // Unique UUIDs for our modifiers to avoid conflicts
    private static final UUID NMSPELL_HEALTH = UUID.fromString("fa0798f6-d801-4ce4-91ad-1c92eb2d0694");
    private static final UUID NMSPELL_ATTACK = UUID.fromString("7f80feba-54ca-4a29-9494-aa857a070dc2");
    private static final UUID NMSPELL_SPEED = UUID.fromString("89e8d1df-31da-4335-9fdf-ddb88a4aec54");
    private static final UUID NMSPELL_ARMOR = UUID.fromString("12345678-1234-5678-9abc-def012345678");
    private static final UUID NMSPELL_STEP_UP = UUID.fromString("5b2a0250-8d1c-48d4-881c-067ba87650c2");

    // Track entity sprinting state to avoid unnecessary updates
    public static final Map<UUID, Boolean> wasSprintingMap = new HashMap<>();

    /**
     * Apply all soul-based stat bonuses to a entity
     */
    public static void applyStatsToEntity(LivingEntity entity, PlayerSoul soul) {
        if (entity == null || soul == null) return;

        // Store current health percentage to maintain it after max health changes
        double healthPercent = entity.getHealth() / entity.getMaxHealth();

        // Apply health bonus
        applyAttributeModifier(entity, Attributes.MAX_HEALTH,
                NMSPELL_HEALTH, "Nightmare Spell Health",
                soul.getBonus(), AttributeModifier.Operation.ADDITION);

        // Restore health percentage (but don't exceed new max health)
        float newHealth = (float) Math.min(
                entity.getMaxHealth() * healthPercent,
                entity.getMaxHealth()
        );
        entity.setHealth(newHealth);

        // Apply attack damage bonus
        applyAttributeModifier(entity, Attributes.ATTACK_DAMAGE,
                NMSPELL_ATTACK, "Nightmare Spell Attack",
                soul.getBonus(), AttributeModifier.Operation.ADDITION);

        // Apply base movement speed bonus (not sprint bonus)
        applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED,
                NMSPELL_SPEED, "Nightmare Spell Speed",
                (soul.getBonus()/150)*0.77, AttributeModifier.Operation.ADDITION);

        // Apply armor bonus
        applyAttributeModifier(entity, Attributes.ARMOR,
                NMSPELL_ARMOR, "Nightmare Spell Armor",
                soul.getBonus(), AttributeModifier.Operation.ADDITION);

        // Apply step up
        if (soul.getRank() >= 4) {
            applyAttributeModifier(entity, ForgeMod.STEP_HEIGHT_ADDITION.get(),
                    NMSPELL_STEP_UP, "Nightmare Spell Step Up",
                    0.6, AttributeModifier.Operation.ADDITION);
        }
    }

    public static void applyStatsToEntity(LivingEntity entity, CorruptedSoul soul) {
        if (entity == null || soul == null) {
            System.out.println("Cannot apply stats - entity: " + (entity != null) + ", soul: " + (soul != null));
            return;
        }

        // Store current health percentage to maintain it after max health changes
        double healthPercent = entity.getHealth() / entity.getMaxHealth();

        // Apply health bonus
        applyAttributeModifier(entity, Attributes.MAX_HEALTH,
                NMSPELL_HEALTH, "Nightmare Spell Health",
                soul.getBonus() * 2, AttributeModifier.Operation.ADDITION);

        // Restore health percentage (but don't exceed new max health)
        float newHealth = (float) Math.min(
                entity.getMaxHealth() * healthPercent,
                entity.getMaxHealth()
        );
        entity.setHealth(newHealth);

        // Apply attack damage bonus
        applyAttributeModifier(entity, Attributes.ATTACK_DAMAGE,
                NMSPELL_ATTACK, "Nightmare Spell Attack",
                soul.getBonus(), AttributeModifier.Operation.ADDITION);

        // Apply base movement speed bonus (not sprint bonus)
        applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED,
                NMSPELL_SPEED, "Nightmare Spell Speed",
                soul.getBonus()/200, AttributeModifier.Operation.ADDITION);

        // Apply armor bonus
        applyAttributeModifier(entity, Attributes.ARMOR,
                NMSPELL_ARMOR, "Nightmare Spell Armor",
                soul.getBonus(), AttributeModifier.Operation.ADDITION);

        // Apply step up
        if (soul.getRank() >= 4) {
            applyAttributeModifier(entity, ForgeMod.STEP_HEIGHT_ADDITION.get(),
                    NMSPELL_STEP_UP, "Nightmare Spell Step Up",
                    0.6, AttributeModifier.Operation.ADDITION);
        }
    }

    /**
     * Remove all soul-based stat bonuses from a entity
     */

    public static void removeStatsFromPlayer(LivingEntity entity) {
        if (entity == null) return;

        removeAttributeModifier(entity, Attributes.MAX_HEALTH, NMSPELL_HEALTH);
        removeAttributeModifier(entity, Attributes.ATTACK_DAMAGE, NMSPELL_ATTACK);
        removeAttributeModifier(entity, Attributes.MOVEMENT_SPEED, NMSPELL_SPEED);
        removeAttributeModifier(entity, Attributes.ARMOR, NMSPELL_ARMOR);

        // Clean up sprinting state tracking
        wasSprintingMap.remove(entity.getUUID());
    }

    /**
     * Update sprint speed bonus based on entity's current sprinting state
     * Call this from a entity tick event or similar
     */
    public static void updateSprintSpeed(LivingEntity entity) {
        if (entity == null) return;

        // Get entity's soul capability
        entity.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
            UUID entityUUID = entity.getUUID();
            boolean currentlySprinting = entity.isSprinting();
            boolean wasSprinting = wasSprintingMap.getOrDefault(entityUUID, false);

            // Only update if sprinting state changed
            if (currentlySprinting != wasSprinting) {
                if (currentlySprinting) {
                    // Apply sprint speed bonus
                    removeAttributeModifier(entity, Attributes.MOVEMENT_SPEED, NMSPELL_SPEED);
                    applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED,
                            NMSPELL_SPEED, "Nightmare Spell Sprint Speed",
                            (soul.getBonus()/150), AttributeModifier.Operation.ADDITION);
                } else {
                    // Remove sprint speed bonus
                    removeAttributeModifier(entity, Attributes.MOVEMENT_SPEED, NMSPELL_SPEED);
                    applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED,
                            NMSPELL_SPEED, "Nightmare Spell Sprint Speed",
                            (soul.getBonus()/150)*0.77, AttributeModifier.Operation.ADDITION);
                }

                // Update tracking
                wasSprintingMap.put(entityUUID, currentlySprinting);
            }
        });
    }

    /**
     * Apply or update an attribute modifier
     */
    private static void applyAttributeModifier(LivingEntity entity,
                                               Attribute attribute,
                                               UUID uuid, String name, double amount,
                                               AttributeModifier.Operation operation) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) return;

        // Always remove existing modifier first to avoid stacking
        attributeInstance.removeModifier(uuid);

        // Only add new modifier if amount is meaningful
        if (Math.abs(amount) > 0.001) { // Use small epsilon for floating point comparison
            AttributeModifier modifier = new AttributeModifier(uuid, name, amount, operation);
            attributeInstance.addPermanentModifier(modifier);
        }
    }

    /**
     * Remove an attribute modifier
     */
    private static void removeAttributeModifier(LivingEntity entity, Attribute attribute, UUID uuid) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(uuid);
        }
    }

    /**
     * Get the total value of an attribute (base + all modifiers)
     * Useful for debugging or display purposes
     */
    public static double getAttributeValue(LivingEntity entity, Attribute attribute) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        return attributeInstance != null ? attributeInstance.getValue() : 0.0;
    }

    /**
     * Check if a entity has our soul modifier applied to an attribute
     */
    public static boolean hasModifier(LivingEntity entity, Attribute attribute, UUID modifierUUID) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        return attributeInstance != null && attributeInstance.getModifier(modifierUUID) != null;
    }

    /**
     * Force refresh all stats for a entity - useful after loading from NBT
     */
    public static void refreshPlayerStats(LivingEntity entity) {
        entity.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
            applyStatsToEntity(entity, soul);
        });
    }
}