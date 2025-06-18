package com.logicweaver.nmspell.event;

import com.logicweaver.nmspell.NMSpell;
import com.logicweaver.nmspell.commands.SetPlayerClassCommand;
import com.logicweaver.nmspell.commands.SetPlayerRankCommand;
import com.logicweaver.nmspell.commands.SetPlayerSoulFragmentsCommand;
import com.logicweaver.nmspell.commands.SummonNIghtmareCreatureCommand;
import com.logicweaver.nmspell.entity.CorruptedSoul;
import com.logicweaver.nmspell.entity.CorruptedSoulProvider;
import com.logicweaver.nmspell.item.ModItems;
import com.logicweaver.nmspell.networking.ModNetworking;
import com.logicweaver.nmspell.networking.packet.CorruptSoulDataSyncS2CPacket;
import com.logicweaver.nmspell.networking.packet.SoulDataSyncS2CPacket;
import com.logicweaver.nmspell.soul.PlayerSoul;
import com.logicweaver.nmspell.soul.PlayerSoulProvider;
import com.logicweaver.nmspell.util.BossBarHandler;
import com.logicweaver.nmspell.util.HierarchyUtils;
import com.logicweaver.nmspell.util.SlowTimeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.*;

@Mod.EventBusSubscriber(modid = NMSpell.MODID)
public class ModEvents {
    private static final Map<UUID, Set<UUID>> playerEngagedEntities = new HashMap<>();

    private static final Random random = new Random();

    // PLAYER EVENTS

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            event.player.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
                int rank = soul.getRank();
                float breathingEfficiency = 1.0f - (rank * 0.1f); // 10% slower oxygen loss per rank
                float hungerEfficiency = 1.0f - (rank * 0.1f); // 10% slower hunger loss per rank

                if (event.player.isUnderWater()) {
                    // Only reduce air every few ticks based on rank
                    if (event.player.tickCount % Math.max(1, (int)(1.0f / breathingEfficiency)) != 0) {
                        event.player.setAirSupply(event.player.getAirSupply() + 1); // Counteract normal loss
                    }
                }
//                // Reduce hunger depletion rate
//                if (event.player.tickCount % Math.max(1, (int)(1.0f / hungerEfficiency)) == 0) {
//                    FoodData foodData = event.player.getFoodData();
//
//                    System.out.println(foodData.getExhaustionLevel());
//
//                    // Reduce exhaustion only if there is some
//                    if (foodData.getExhaustionLevel() > 0.0f) {
//                        System.out.println(foodData.getExhaustionLevel() * hungerEfficiency);
//                        foodData.setExhaustion(foodData.getExhaustionLevel() * hungerEfficiency);
//                    }
//
//                    // Cap saturation to food level to avoid infinite growth
//                    float saturation = foodData.getSaturationLevel();
//                    float maxSaturation = foodData.getFoodLevel();
//                    foodData.setSaturation(Math.min(maxSaturation, saturation + 0.05f));
//                }

            });
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();

        PlayerSoul soul = event.getOriginal().getCapability(PlayerSoulProvider.PLAYER_SOUL).resolve().orElse(null);

        if (soul != null) {
            System.out.println("Original soul bonus: " + soul.getBonus());

            event.getEntity().getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(newSoul -> {
                newSoul.copyFrom(soul);

                newSoul.setAssociatedPlayer(event.getEntity());
                System.out.println("Successfully copied soul data to new player");
            });
        };

        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();

        SlowTimeHandler.resetPlayerTime((ServerPlayer) event.getEntity());

        playerEngagedEntities.remove(playerUUID);
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getTarget() instanceof LivingEntity target) {
            ServerBossEvent bossEvent = BossBarHandler.getBossBar(target);
            if (bossEvent != null && !bossEvent.getPlayers().contains(player)) {
                double distSqr = player.distanceToSqr(target);
                if (distSqr <= 75 * 75) {
                    bossEvent.addPlayer(player);
                    playerEngagedEntities.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(target.getUUID());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerStopTracking(PlayerEvent.StopTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getEntity() != null) {
            ServerBossEvent bossEvent = BossBarHandler.getBossBar(event.getEntity());
            if (bossEvent != null) {
                bossEvent.removePlayer(player);
            }
        }
    }

    // ENTITY EVENTS

    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();

        if (entity.level().isClientSide()) return;

        if (entity instanceof Player) {
            PlayerSoulProvider provider = new PlayerSoulProvider();
            event.addCapability(new ResourceLocation(NMSpell.MODID, "properties"), provider);
        } else if (entity instanceof Monster) {
            CorruptedSoulProvider provider = new CorruptedSoulProvider((LivingEntity) entity);
            event.addCapability(new ResourceLocation(NMSpell.MODID, "properties"), provider);

            event.addListener(provider::invalidate);
        }
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {

        if (event.getSource().getEntity() instanceof Player player) {
            if (event.getEntity() instanceof Monster monster) {
                monster.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
                    if (soul.getAssociatedEntity() == null || soul.getRank() <= 0 || soul.getSoul_cores() <= 0) {
                        return;
                    }

                    UUID monsterUUID = monster.getUUID();
                    UUID playerUUID = player.getUUID();

                    // Add entity to player's engaged list
                    playerEngagedEntities.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(monsterUUID);

                    // Mark creature as engaged globally
                    if (BossBarHandler.getBossBar(monster) == null) {
                        // Create boss bar for first hit
                        BossBarHandler.createBossBar(
                                monster,
                                Component.literal(HierarchyUtils.getCorruptionRank(soul.getRank()) + " " + HierarchyUtils.getClass(soul.getSoul_cores()) +  " ").withStyle(HierarchyUtils.getColor(soul.getRank())).append(monster.getDisplayName())
                        );

                        // Add all nearby players to the boss bar AND to their engaged lists
                        ServerBossEvent bossEvent = BossBarHandler.getBossBar(monster);
                        if (bossEvent != null) {
                            for (ServerPlayer nearbyPlayer : monster.level().getEntitiesOfClass(ServerPlayer.class, monster.getBoundingBox().inflate(50))) {
                                bossEvent.addPlayer(nearbyPlayer);
                                // Also add to their engaged list
                                playerEngagedEntities.computeIfAbsent(nearbyPlayer.getUUID(), k -> new HashSet<>()).add(monsterUUID);
                            }
                            System.out.println(bossEvent.getPlayers());
                        }
                    } else {
                        // Even if creature is already engaged, make sure this player is added to boss bar if it exists
                        ServerBossEvent bossEvent = BossBarHandler.getBossBar(monster);
                        if (bossEvent != null && !bossEvent.getPlayers().contains((ServerPlayer) player)) {
                            bossEvent.addPlayer((ServerPlayer) player);
                            System.out.println(bossEvent.getPlayers());
                        }
                    }
                });
            }
            player.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
                if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                    System.out.println(event.getAmount());
                    System.out.println((float) (event.getAmount() + soul.getBonus()));
                    event.setAmount((float) (event.getAmount() + soul.getBonus()));
                }
            });
        } else if (event.getSource().getEntity() instanceof Monster monster) {
            monster.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
                if (soul.getAssociatedEntity() == null || soul.getRank() <= 0 || soul.getSoul_cores() <= 0) { return; }
                if (event.getSource().is(DamageTypeTags.IS_PROJECTILE) || event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
                    event.setAmount((float) (event.getAmount() + soul.getBonus()));
                }
            });
        }

        if (event.getEntity() instanceof Player player) {
            player.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
                int rank = soul.getRank(); // Get player's rank
                float reduction = 1.0f - (rank * 0.1f); // 10% less damage per rank
                event.setAmount(event.getAmount() * reduction);
            });
        } else if (event.getEntity() instanceof Monster monster) {
            monster.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
                if (soul.getAssociatedEntity() == null || soul.getRank() <= 0 || soul.getSoul_cores() <= 0) { return; }
                int rank = soul.getRank(); // Get player's rank
                float reduction = 1.0f - (rank * 0.1f); // 10% less damage per rank
                event.setAmount(event.getAmount() * reduction);
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            PlayerSoul soul = player.getCapability(PlayerSoulProvider.PLAYER_SOUL).resolve().orElse(null);
            if (soul != null) {
                CompoundTag soulData = new CompoundTag();
                soul.saveNBTData(soulData);
            } else {
                System.out.println("ERROR: Could not get soul capability on death!");
            }
        } else if (entity.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).isPresent()) {
            if (!entity.level().isClientSide()) {
                entity.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
                    if (soul.getAssociatedEntity() == null || soul.getRank() <= 0 || soul.getSoul_cores() <= 0) { return; }

                    ServerBossEvent bossEvent = BossBarHandler.removeBossBar(event.getEntity());
                    if (bossEvent != null) {
                        bossEvent.removeAllPlayers();
                    }

                    UUID entityUUID = entity.getUUID();
//                    engagedCreatures.remove(entityUUID);
                    playerEngagedEntities.values().forEach(set -> set.remove(entityUUID));

                    Entity deathSource =  event.getSource().getEntity();

                    if (deathSource instanceof Player player) {
                        player.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(playerSoul -> {
                            int soulDiff = soul.getRank() - playerSoul.getRank();
                            if (soulDiff > 0) {
                                for (int i = 0; i < soulDiff; i ++) {
                                    playerSoul.incrementStrongerEntitiesKilled();
                                }
                            }
                        });
                    }

                    ItemStack shardStack = new ItemStack(ModItems.SOUL_SHARDS.get(soul.getRank()-1).get());
                    shardStack.setCount(soul.getSoul_cores());

                    ItemEntity itemEntity = new ItemEntity(
                            entity.level(),
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            shardStack
                    );

                    entity.level().addFreshEntity(itemEntity);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEntityRemove(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof  LivingEntity) {
            ServerBossEvent bossEvent = BossBarHandler.removeBossBar((LivingEntity) event.getEntity());
            if (bossEvent != null) {
                bossEvent.removeAllPlayers();
            }

            UUID entityUUID = event.getEntity().getUUID();
//            engagedCreatures.remove(entityUUID);
            playerEngagedEntities.values().forEach(set -> set.remove(entityUUID));
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            Entity entity = event.getEntity();
            if (entity instanceof ServerPlayer serverPlayer) {
                // Handle player soul initialization (existing code)
                PlayerSoul soul = serverPlayer.getCapability(PlayerSoulProvider.PLAYER_SOUL).resolve().orElse(null);
                if (soul != null) {
                    ModNetworking.sendToPlayer(new SoulDataSyncS2CPacket(
                            soul.getSoul_Fragments(),
                            soul.getMax_soul_fragments(),
                            soul.getRank(),
                            soul.getSoul_cores(),
                            soul.getStrongerEntitiesKilled()
                    ), serverPlayer);
                    soul.setAssociatedPlayer(serverPlayer);
                } else {
                    System.out.println("WARNING: Player has no soul capability on join!");
                }
                playerEngagedEntities.putIfAbsent(serverPlayer.getUUID(), new HashSet<>());
            } else if (isCommandSpawned(entity)) {
                CompoundTag data = entity.getPersistentData();
                int rank = data.getInt("nightmare_creature_rank");
                int soul_cores = data.getInt("nightmare_creature_class");
                entity.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
                    soul.setAssociatedEntity((LivingEntity) entity);
                    soul.setRank(rank);
                    soul.setClass(soul_cores);

                    setUpNightmareCreature(entity, soul);
                });

            } else if (random.nextFloat() < 0.05f) {
                entity.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
                    if (soul.getAssociatedEntity() == null) {
                        soul.setAssociatedEntity((LivingEntity) entity);
                        soul.setRank(HierarchyUtils.getScaledRandom());
                        soul.setInitialized(true);
                        soul.setClass(HierarchyUtils.getRandom());

                        setUpNightmareCreature(entity, soul);
                    }else {
                        System.out.println("soul exists");
                    }

                    if (!soul.isInitialized()) {
                        soul.tryRestoreEntity((ServerLevel) event.getLevel());
                    }
                });
            }
        }
    }

    // TICK EVENTS

    private static int glowUpdateTimer = 0;
    private static final int GLOW_UPDATE_INTERVAL = 10; // Update every second (20 ticks)

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            glowUpdateTimer++;

            // Only update glow every GLOW_UPDATE_INTERVAL ticks for performance
            if (glowUpdateTimer >= GLOW_UPDATE_INTERVAL) {
                glowUpdateTimer = 0;

                ServerLifecycleHooks.getCurrentServer().getAllLevels().forEach(level -> {
                    level.getAllEntities().forEach(entity -> {
                        if (entity instanceof LivingEntity livingEntity) {
                            entity.getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
//                                boolean shouldGlow = isPlayerNearby(livingEntity) && soul.getAssociatedEntity() != null;

                                boolean playerNearby = false;
                                int maxDistance = 10 * soul.getRank();

                                for (Player player : level.players()) {
                                    if (player.distanceToSqr(entity) <= maxDistance * maxDistance) {
                                        playerNearby = true;
                                    }
                                }

                                boolean shouldGlow = (soul.getAssociatedEntity() != null && soul.getRank() > 0 && soul.getSoul_cores() > 0 && playerNearby);
                                if (shouldGlow != livingEntity.isCurrentlyGlowing()) {
                                    livingEntity.setGlowingTag(shouldGlow);
                                }

                                ServerBossEvent bossEvent = BossBarHandler.getBossBar(livingEntity);
                                if (bossEvent != null) {
                                    manageBossBarDistance(livingEntity, bossEvent, maxDistance);
                                }
                            });
                        }
                    });
                });
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        event.getEntity().getCapability(CorruptedSoulProvider.CORRUPTED_SOUL).ifPresent(soul -> {
            if (soul.getAssociatedEntity() == null && event.getEntity().tickCount % 200 == 0) {
                soul.tryRestoreEntity((ServerLevel) event.getEntity().level());
            } else {
                ServerBossEvent bossEvent = BossBarHandler.getBossBar(event.getEntity());
                if (bossEvent != null) {
                    bossEvent.setProgress(event.getEntity().getHealth() / event.getEntity().getMaxHealth());
                }
            }
        });
    }

    // LEVEL EVENTS

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        Scoreboard scoreboard = level.getScoreboard();

        for (Map.Entry<Integer, ChatFormatting> entry : HierarchyUtils.getColorMap().entrySet()) {
            String teamName1 = HierarchyUtils.getAscensionRank(entry.getKey());
            String teamName2 = HierarchyUtils.getCorruptionRank(entry.getKey());
            if (scoreboard.getPlayersTeam(teamName1) == null) {
                PlayerTeam team =  scoreboard.addPlayerTeam(teamName1);
                team.setColor(entry.getValue());
                scoreboard.addPlayerToTeam("dummy_" + teamName1, team);
            }
            if (scoreboard.getPlayersTeam(teamName2) == null) {
                PlayerTeam team =  scoreboard.addPlayerTeam(teamName2);
                team.setColor(entry.getValue());
                scoreboard.addPlayerToTeam("dummy_" + teamName2, team);
            }
            System.out.println("Created team: " + teamName1 + " and " + teamName2);
        }
    }

    // REGISTER EVENTS

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerSoul.class);
        event.register(CorruptedSoul.class);
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new SetPlayerRankCommand(event.getDispatcher());
        new SetPlayerClassCommand(event.getDispatcher());
        new SetPlayerSoulFragmentsCommand(event.getDispatcher());
        new SummonNIghtmareCreatureCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

    // METHODS

    private static void manageBossBarDistance(LivingEntity entity, ServerBossEvent bossEvent, int maxDistance) {
        double maxDistanceSquared = maxDistance * maxDistance;
        UUID entityUUID = entity.getUUID();

        // Get all players currently on the boss bar
        Set<ServerPlayer> playersToRemove = new HashSet<>();

        // Check each player on the boss bar - remove if too far
        for (ServerPlayer player : bossEvent.getPlayers()) {
            double distanceSquared = player.distanceToSqr(entity);

            if (distanceSquared > maxDistanceSquared) {
                playersToRemove.add(player);
            }
        }

        // Check ALL players in the level who are engaged with this entity
        // Don't rely on getBoundingBox().inflate() which may not work as expected
        for (ServerPlayer player : ((ServerLevel)entity.level()).players()) {
            UUID playerUUID = player.getUUID();
            Set<UUID> engagedEntities = playerEngagedEntities.get(playerUUID);

            if (player.distanceToSqr(entity) <= maxDistanceSquared) {
                bossEvent.addPlayer(player);
                playerEngagedEntities.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(entityUUID);
            }
        }

        // Remove players who are too far
        for (ServerPlayer player : playersToRemove) {
            bossEvent.removePlayer(player);
        }

        // If no players remain on the boss bar, remove it entirely
        if (bossEvent.getPlayers().isEmpty()) {
            BossBarHandler.removeBossBar(entity);
        }
    }

    private static void setUpNightmareCreature(Entity entity, CorruptedSoul soul) {

        CorruptSoulDataSyncS2CPacket packet = new CorruptSoulDataSyncS2CPacket(
                soul.getEntityUUID(),
                soul.getSoul_Fragments(),
                soul.getMax_soul_fragments(),
                soul.getRank(),
                soul.getSoul_cores()
        );

        ServerLevel serverLevel = (ServerLevel) entity.level();
        Scoreboard scoreboard = serverLevel.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(HierarchyUtils.getCorruptionRank(soul.getRank()));

        if (team != null) {
            scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
        } else {
            System.out.println("Team does not exist");
        }

        ModNetworking.sendToNearEntity(packet, soul.getAssociatedEntity(), 10);
    }

    private static boolean isCommandSpawned(Entity entity) {
        CompoundTag compoundTag = entity.getPersistentData();

        return compoundTag.getBoolean("summoned_nightmare_creature");
    }

}