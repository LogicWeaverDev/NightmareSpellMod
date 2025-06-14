package com.logicweaver.nmspell.commands;

import com.logicweaver.nmspell.entity.CorruptedSoulProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class SummonNIghtmareCreatureCommand {
    public SummonNIghtmareCreatureCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("summon_nightmare_creature")
                .then(Commands.argument("entity", ResourceLocationArgument.id())
                        .suggests((context, builder) -> {
                            return SharedSuggestionProvider.suggestResource(
                                    ForgeRegistries.ENTITY_TYPES.getKeys().stream()
                                            .filter(key -> {
                                                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(key);
                                                if (type == null || !type.canSummon()) {
                                                    return false;
                                                }

                                                // Check if the entity type creates Monster instances
                                                try {
                                                    Entity testEntity = type.create(context.getSource().getLevel());
                                                    return testEntity instanceof Monster;
                                                } catch (Exception e) {
                                                    return false;
                                                }
                                            }), builder);
                        })
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("rank", IntegerArgumentType.integer())
                                        .then(Commands.argument("class", IntegerArgumentType.integer())
                                                .executes(this::summonNightmareCreature))))));
    }

    private int summonNightmareCreature(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        CommandSourceStack source = command.getSource();
        ServerLevel level = source.getLevel();

        ResourceLocation resourceLocation = ResourceLocationArgument.getId(command, "entity");
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
        BlockPos blockPos = BlockPosArgument.getBlockPos(command, "pos");

        int rank = IntegerArgumentType.getInteger(command, "rank");
        int soul_cores = IntegerArgumentType.getInteger(command, "class");

        if (entityType == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Invalid entity type!"));
            return 0;
        }

        // Create entity WITH the NBT data
        Entity entity = entityType.create(level);

        // Create the NBT compound with the data FIRST
        if (entity == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Entity not created!"));
            return 0;
        }

        CompoundTag spawnData = entity.getPersistentData();
        spawnData.putInt("nightmare_creature_rank", rank);
        spawnData.putInt("nightmare_creature_class", soul_cores);
        spawnData.putBoolean("summoned_nightmare_creature", true);

        if (!(entity instanceof LivingEntity livingEntity)) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Failed to create living entity!"));
            return 0;
        }

        entity.setPos(blockPos.getX()+0.5f, blockPos.getY(), blockPos.getZ()+0.5f);

        // Add to world - this should trigger capability attachment with proper data
        level.addFreshEntity(entity);

        // Schedule capability check for next tick to ensure everything is processed
//        level.getServer().execute(() -> {
//            System.out.println("Checking capability after entity spawn...");
//
//            if (livingEntity.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).isPresent()) {
//                System.out.println("SUCCESS: Capability attached!");
//
//                livingEntity.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
//                    System.out.println("Capability values:");
//                    System.out.println("  Rank: " + soul.getRank());
//                    System.out.println("  Class: " + soul.getSoul_cores());
//                    System.out.println("  Health bonus: " + soul.getHealthBonus());
//                    System.out.println("  Attack bonus: " + soul.getAttackDamageBonus());
//                });
//            } else {
//                System.out.println("ERROR: Capability was not attached!");
//                System.out.println("Final persistent data check:");
//                CompoundTag finalData = livingEntity.getPersistentData();
//                System.out.println("  Keys: " + finalData.getAllKeys());
//                System.out.println("  summoned_nightmare_creature: " + finalData.getBoolean("summoned_nightmare_creature"));
//                System.out.println("  nightmare_creature_rank: " + finalData.getInt("nightmare_creature_rank"));
//                System.out.println("  nightmare_creature_class: " + finalData.getInt("nightmare_creature_class"));
//            }
//        });

        return 1;
    }
}