package com.logicweaver.nmspell.event;

import com.logicweaver.nmspell.NMSpell;
import com.logicweaver.nmspell.networking.ModNetworking;
import com.logicweaver.nmspell.networking.packet.TogglePerceptionC2SPacket;
import com.logicweaver.nmspell.util.SoulStatManager;
import com.logicweaver.nmspell.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = NMSpell.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        private static boolean runesEnabled = false;

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (KeyBinding.PERCEPTION_KEY.consumeClick()) {
                ModNetworking.sendToServer(new TogglePerceptionC2SPacket());
            } else if (KeyBinding.RUNES_KEY.consumeClick()) {
                runesEnabled = !runesEnabled;
                // Optional: Send feedback message
                minecraft.player.displayClientMessage(
                        Component.literal("Overlay " + (runesEnabled ? "enabled" : "disabled")),
                        true
                );
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

            Player player = event.player;
            UUID playerUUID = player.getUUID();
            boolean isSprinting = player.isSprinting();
            boolean wasSprinting = SoulStatManager.wasSprintingMap.getOrDefault(playerUUID, false);

            if (isSprinting != wasSprinting) {
                SoulStatManager.updateSprintSpeed(player);
                SoulStatManager.wasSprintingMap.put(playerUUID, isSprinting);
            }
        }

        public static boolean isRunesEnabled() {
            return runesEnabled;
        }

//        @SubscribeEvent
//        public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
//            LivingEntity entity = event.getEntity();
//
//            if (entity.tickCount % 20 == 0) {
//                System.out.println("1 - Checking entity: " + entity.getClass().getSimpleName());
//            }
//
//            // Use the synced client data instead of capability
//            ClientCorruptSoulData.EntitySoulData soulData = ClientCorruptSoulData.getEntityData(entity.getUUID());
//
//            if (soulData != null) {
//                if (entity.tickCount % 20 == 0) {
//                    System.out.println("2 - Found synced data for entity: " + entity.getUUID());
//                }
//
//                boolean shouldGlow = (soulData.getRank() > 0 && soulData.getSoulCores() > 0);
//
//                if (shouldGlow) {
//                    entity.setGlowingTag(true);
//                } else {
//                    entity.setGlowingTag(false);
//                }
//            } else if (entity.tickCount % 20 == 0) {
//                System.out.println("No synced data found for entity: " + entity.getUUID());
//            }
//        }
    }

    @Mod.EventBusSubscriber(modid = NMSpell.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.PERCEPTION_KEY);
            event.register(KeyBinding.RUNES_KEY);
        }
    }
}
