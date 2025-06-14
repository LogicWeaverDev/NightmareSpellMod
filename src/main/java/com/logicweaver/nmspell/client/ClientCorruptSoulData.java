package com.logicweaver.nmspell.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientCorruptSoulData {
    private static final Map<UUID, EntitySoulData> entityDataMap = new HashMap<>();

    public static void setEntityData(UUID entityUUID, double soulFragments, double maxSoulFragments, int rank, int soulCores) {
        entityDataMap.put(entityUUID, new EntitySoulData(soulFragments, maxSoulFragments, rank, soulCores));
    }

    public static EntitySoulData getEntityData(UUID entityUUID) {
        return entityDataMap.get(entityUUID);
    }

    public static void removeEntityData(UUID entityUUID) {
        entityDataMap.remove(entityUUID);
    }

    public static void clearAllData() {
        entityDataMap.clear();
    }

    public static class EntitySoulData {
        private final double soulFragments;
        private final double maxSoulFragments;
        private final int rank;
        private final int soulCores;

        public EntitySoulData(double soulFragments, double maxSoulFragments, int rank, int soulCores) {
            this.soulFragments = soulFragments;
            this.maxSoulFragments = maxSoulFragments;
            this.rank = rank;
            this.soulCores = soulCores;
        }

        public double getSoulFragments() { return soulFragments; }
        public double getMaxSoulFragments() { return maxSoulFragments; }
        public int getRank() { return rank; }
        public int getSoulCores() { return soulCores; }
    }
}