package com.logicweaver.nmspell.networking.packet;

import com.logicweaver.nmspell.client.ClientCorruptSoulData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CorruptSoulDataSyncS2CPacket {
    private UUID entityUUID;
    private double soul_fragments;
    private double max_soul_fragments;

    private int rank;
    private int soul_cores;

    public CorruptSoulDataSyncS2CPacket(UUID uuid, double soul_fragments, double max_soul_fragments, int rank, int soul_cores) {
        this.entityUUID = uuid;
        this.soul_fragments = soul_fragments;
        this.max_soul_fragments = max_soul_fragments;

        this.rank = rank;
        this.soul_cores = soul_cores;
    }

    public CorruptSoulDataSyncS2CPacket(FriendlyByteBuf buf) {
        this.entityUUID = buf.readUUID();
        this.soul_fragments = buf.readDouble();
        this.max_soul_fragments = buf.readDouble();

        this.rank = buf.readInt();
        this.soul_cores = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityUUID);
        buf.writeDouble(soul_fragments);
        buf.writeDouble(max_soul_fragments);

        buf.writeInt(rank);
        buf.writeInt(soul_cores);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            System.out.println("Packet received on client!"); // Add this debug line
            ClientCorruptSoulData.setEntityData(entityUUID, soul_fragments, max_soul_fragments, rank, soul_cores);
            System.out.println("Client data updated for entity: " + entityUUID); // Add this debug line
        });
        return true; // Make sure this returns true
    }
}
