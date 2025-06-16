package com.logicweaver.nmspell.item;

import com.logicweaver.nmspell.NMSpell;
import com.logicweaver.nmspell.util.HierarchyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NMSpell.MODID);

    public static final RegistryObject<CreativeModeTab> NMSPELL_TAB = CREATIVE_MODE_TABS.register("nmspell_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SOUL_SHARDS.get(6).get()))
                    .title(Component.translatable("creativetab.nmspell"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.RANK_REGRESSION.get());
                        for (RegistryObject<Item> soulShard : ModItems.SOUL_SHARDS) {
                            pOutput.accept(soulShard.get());
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
