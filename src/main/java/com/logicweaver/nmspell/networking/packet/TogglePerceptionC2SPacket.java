package com.logicweaver.nmspell.networking.packet;

import com.google.common.graph.Network;
import com.logicweaver.nmspell.util.SlowTimeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TogglePerceptionC2SPacket {
    public TogglePerceptionC2SPacket() {

    }

    public TogglePerceptionC2SPacket(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            SlowTimeHandler.ToggleSlowTime(player);
        });
        return true;
    }
}
