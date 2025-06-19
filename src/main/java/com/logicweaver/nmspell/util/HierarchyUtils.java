package com.logicweaver.nmspell.util;

import com.logicweaver.nmspell.soul.PlayerSoulProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class HierarchyUtils {

    private static final Map<Integer, String> ascensionMap = new HashMap<>();
    private static final Map<Integer, String> corruptionMap = new HashMap<>();
    private static final Map<Integer, String> classMap = new HashMap<>();
    private static final Map<Integer, Float> baseRarityMap = new HashMap<>();
    private static final Map<Integer, ChatFormatting> colorMap = new HashMap<>();

    private static final Random random = new Random();

    static {
        ascensionMap.put(1, "Dormant");
        ascensionMap.put(2, "Awakened");
        ascensionMap.put(3, "Ascended");
        ascensionMap.put(4, "Transcendent");
        ascensionMap.put(5, "Supreme");
        ascensionMap.put(6, "Sacred");
        ascensionMap.put(7, "Divine");

        corruptionMap.put(1, "Dormant");
        corruptionMap.put(2, "Awakened");
        corruptionMap.put(3, "Fallen");
        corruptionMap.put(4, "Corrupted");
        corruptionMap.put(5, "Great");
        corruptionMap.put(6, "Cursed");
        corruptionMap.put(7, "Unholy");

        classMap.put(1, "Beast");
        classMap.put(2, "Monster");
        classMap.put(3, "Demon");
        classMap.put(4, "Devil");
        classMap.put(5, "Tyrant");
        classMap.put(6, "Terror");
        classMap.put(7, "Titan");

        baseRarityMap.put(1, 0.6f);
        baseRarityMap.put(2, 0.19f);
        baseRarityMap.put(3, 0.1f);
        baseRarityMap.put(4, 0.05f);
        baseRarityMap.put(5, 0.03f);
        baseRarityMap.put(6, 0.02f);
        baseRarityMap.put(7, 0.01f);

        colorMap.put(1, ChatFormatting.AQUA);
        colorMap.put(2, ChatFormatting.GREEN);
        colorMap.put(3, ChatFormatting.DARK_PURPLE);
        colorMap.put(4, ChatFormatting.YELLOW);
        colorMap.put(5, ChatFormatting.GOLD);
        colorMap.put(6, ChatFormatting.DARK_RED);
        colorMap.put(7, ChatFormatting.DARK_GRAY);
    }

    public static Map<Integer, Float> getAdjustedRarityMap(double scalingFactor) {
        double averageCapability = getAveragePlayerCapability();
        return createScaledRarityMap(averageCapability, scalingFactor);
    }

    public static double getAveragePlayerCapability() {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return 1.0; // Default if no server available
        }

        List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();

        if (players.isEmpty()) {
            return 1.0;
        }

        AtomicReference<Double> totalCapability = new AtomicReference<>(0.0);
        for (ServerPlayer player : players) {
            player.getCapability(PlayerSoulProvider.PLAYER_SOUL).ifPresent(soul -> {
                totalCapability.updateAndGet(v -> new Double((double) (v + soul.getRank())));
            });
        }

        return totalCapability.get() / players.size();
    }

    private static Map<Integer, Float> createScaledRarityMap(double averageCapability, double scalingFactor) {
        Map<Integer, Float> scaledMap = new HashMap<>();

        // Round average capability to nearest tier (1-7)
        int averageTier = Math.max(1, Math.min(7, (int) Math.round(averageCapability)));

        for (int tier = 1; tier <= 7; tier++) {
            float baseWeight = baseRarityMap.get(tier);
            float scaledWeight;

            if (tier == averageTier) {
                // Average tier becomes much more common
                scaledWeight = baseWeight * (float) (2.0 + scalingFactor);
            } else if (tier < averageTier) {
                // Lower tiers become rarer (exponentially decreasing)
                int distance = averageTier - tier;
                scaledWeight = baseWeight * (float) Math.pow(0.5, distance * scalingFactor);
            } else {
                // Higher tiers become slightly more common (diminishing returns)
                int distance = tier - averageTier;
                scaledWeight = baseWeight * (float) (1.0 + (0.3 * scalingFactor * Math.pow(0.8, distance)));
            }

            scaledMap.put(tier, Math.max(0.001f, scaledWeight)); // Minimum weight
        }

        // Normalize weights to sum to 1.0
        return normalizeWeights(scaledMap);
    }

    private static Map<Integer, Float> normalizeWeights(Map<Integer, Float> weights) {
        float totalWeight = 0.0f;
        for (float weight : weights.values()) {
            totalWeight += weight;
        }

        Map<Integer, Float> normalizedMap = new HashMap<>();
        for (Map.Entry<Integer, Float> entry : weights.entrySet()) {
            normalizedMap.put(entry.getKey(), entry.getValue() / totalWeight);
        }

        return normalizedMap;
    }

    /**
     * Gets a random tier using the base rarity map
     */
    public static int getRandom() {
        return getRandomFromMap(baseRarityMap);
    }

    public static int getRandomFromMap(Map<Integer, Float> baseRarityMap) {
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (int tier = 1; tier <= 7; tier++) {
            cumulativeProbability += baseRarityMap.get(tier);
            if (randomValue <= cumulativeProbability) {
                return tier;
            }
        }

        return 1;
    }

    public static int getScaledRandom() {
        Map<Integer, Float> adjustedMap = getAdjustedRarityMap(2);
        System.out.println("Scaled map: "+adjustedMap);
        return getRandomFromMap(adjustedMap);
    }

    public static Map<Integer, String> getAscensionPath() {
        return ascensionMap;
    }

    public static Map<Integer, String> getCorruptionPath() {
        return corruptionMap;
    }

    public static Map<Integer, ChatFormatting> getColorMap() {
        return colorMap;
    }

    public static String getAscensionRank(int num) {
        return ascensionMap.get(num);
    }

    public static String getCorruptionRank(int num) {
        return corruptionMap.get(num);
    }

    public static String getClass(int num) {
        return classMap.get(num);
    }
    public static ChatFormatting getColor(int num) {
        return colorMap.get(num);
    }
}
