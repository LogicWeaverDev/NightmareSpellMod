package com.logicweaver.nmspell.networking;

import com.logicweaver.nmspell.NMSpell;
import com.logicweaver.nmspell.networking.packet.CorruptSoulDataSyncS2CPacket;
import com.logicweaver.nmspell.networking.packet.SoulDataSyncS2CPacket;
import com.logicweaver.nmspell.networking.packet.TogglePerceptionC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(NMSpell.MODID, "networking"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(TogglePerceptionC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(TogglePerceptionC2SPacket::new)
                .encoder(TogglePerceptionC2SPacket::toBytes)
                .consumerMainThread(TogglePerceptionC2SPacket::handle)
                .add();

        net.messageBuilder(SoulDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SoulDataSyncS2CPacket::new)
                .encoder(SoulDataSyncS2CPacket::toBytes)
                .consumerMainThread(SoulDataSyncS2CPacket::handle)
                .add();

        net.messageBuilder(CorruptSoulDataSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CorruptSoulDataSyncS2CPacket::new)
                .encoder(CorruptSoulDataSyncS2CPacket::toBytes)
                .consumerMainThread(CorruptSoulDataSyncS2CPacket::handle)
                .add();
    }

    public static <PACK> void sendToServer(PACK packet) {
        INSTANCE.sendToServer(packet);
    }

    public static <PACK> void sendToPlayer(PACK packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToNearEntity(Object packet, Entity entity, double range) {
        System.out.println("Sending packet to players within " + range + " blocks: " + packet.getClass().getSimpleName());
        System.out.println("Entity: " + entity.getClass().getSimpleName() + " at " + entity.position());

        INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                range,
                entity.level().dimension()
        )), packet);

        System.out.println("Packet sent to nearby players successfully");
    }
}
