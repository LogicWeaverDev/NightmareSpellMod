package com.logicweaver.nmspell.networking.packet;

import com.logicweaver.nmspell.client.ClientSoulData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SoulDataSyncS2CPacket {
    private double soul_fragments;
    private double max_soul_fragments;

    private int rank;
    private int soul_cores;
    private int sek;

    public SoulDataSyncS2CPacket(double soul_fragments, double max_soul_fragments, int rank, int soul_cores, int sek) {
        this.soul_fragments = soul_fragments;
        this.max_soul_fragments = max_soul_fragments;

        this.rank = rank;
        this.soul_cores = soul_cores;
        this.sek = sek;
    }

    public SoulDataSyncS2CPacket(FriendlyByteBuf buf) {
        this.soul_fragments = buf.readDouble();
        this.max_soul_fragments = buf.readDouble();

        this.rank = buf.readInt();
        this.soul_cores = buf.readInt();
        this.sek = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(soul_fragments);
        buf.writeDouble(max_soul_fragments);

        buf.writeInt(rank);
        buf.writeInt(soul_cores);
        buf.writeInt(sek);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientSoulData.setPlayerSoul_Fragments(soul_fragments);
            ClientSoulData.setPlayerMax_Soul_Fragments(max_soul_fragments);

            ClientSoulData.setPlayerRank(rank);
            ClientSoulData.setPlayerSoul_Cores(soul_cores);
            ClientSoulData.setPlayerSEK(sek);
        });
        return true;
    }
}
