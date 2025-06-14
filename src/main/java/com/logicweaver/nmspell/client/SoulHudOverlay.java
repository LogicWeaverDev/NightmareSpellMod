package com.logicweaver.nmspell.client;

import com.logicweaver.nmspell.NMSpell;
import com.logicweaver.nmspell.event.ClientEvents;
import com.logicweaver.nmspell.util.HierarchyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.Console;

@Mod.EventBusSubscriber(modid = NMSpell.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulHudOverlay {

    // Colors (ARGB format)
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int TEXT_COLOR = 0xFFFFFFFF; // White text

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!ClientEvents.ClientForgeEvents.isRunesEnabled()) { return; }

        // Only render on the main HUD layer
        if (!event.getOverlay().id().toString().equals("minecraft:hotbar")) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        // Don't render if player is null or if debug screen is open
        if (minecraft.player == null || minecraft.options.renderDebug) {
            return;
        }

        // Don't render if chat is open or in a GUI
        if (minecraft.screen != null) {
            return;
        }

        renderSoulOverlay(event.getGuiGraphics(), minecraft);
    }

    private static void renderSoulOverlay(GuiGraphics guiGraphics, Minecraft minecraft) {
        Font font = minecraft.font;

        // Get soul data from ClientSoulData
        double soulFragments = ClientSoulData.getPlayerSoul_Fragments();
        double maxSoulFragments = ClientSoulData.getPlayerMax_Soul_Fragments();
        int rank = ClientSoulData.getPlayerRank();
        int soulCores = ClientSoulData.getPlayerSoul_Cores();

        // Create text components
        String rankName = HierarchyUtils.getAscensionRank(rank);
        Component rankText = Component.literal("Rank: " + rankName);
        Component sekText = Component.literal(String.format("Rank Requirement: %s/%s", ClientSoulData.getPlayerSEK(), "7"));
        String className = HierarchyUtils.getClass(soulCores);
        Component classText = Component.literal("Class: " + className);
        Component fragmentsText = Component.literal(String.format("Soul Fragments: %.0f/%.0f", soulFragments, maxSoulFragments));
        Component coresText = Component.literal(String.format("Soul Cores: %s/%s", soulCores, "7"));

        // Calculate dimensions
        int maxWidth = Math.max(
                Math.max(font.width(rankText), font.width(fragmentsText)),
                font.width(coresText)
        ) + 16; // Add padding

        int height = (font.lineHeight * 5) + 16;
        int overlay_x = 0;
        int overlay_y = height/2;

        // Draw background
        guiGraphics.fill(overlay_x, overlay_y, overlay_x + maxWidth, overlay_y + height, BACKGROUND_COLOR);

        // Draw text
        int textY = overlay_y + 8;
        guiGraphics.drawString(font, Component.literal("Name: ").append(minecraft.player.getName()), overlay_x + 8, textY, TEXT_COLOR, false);
        textY += font.lineHeight;
        guiGraphics.drawString(font, rankText, overlay_x + 8, textY, TEXT_COLOR, false);
        textY += font.lineHeight;
        guiGraphics.drawString(font, sekText, overlay_x + 8, textY, TEXT_COLOR, false);
        textY += font.lineHeight;
        guiGraphics.drawString(font, classText, overlay_x + 8, textY, TEXT_COLOR, false);
        textY += font.lineHeight;
        guiGraphics.drawString(font, coresText, overlay_x + 8, textY, TEXT_COLOR, false);
        textY += font.lineHeight;
        guiGraphics.drawString(font, fragmentsText, overlay_x + 8, textY, TEXT_COLOR, false);

    }
}
