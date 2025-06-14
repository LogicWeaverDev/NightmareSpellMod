package com.logicweaver.nmspell.item;

import com.logicweaver.nmspell.NMSpell;
import com.logicweaver.nmspell.item.classes.SoulShardItem;
import com.logicweaver.nmspell.util.HierarchyUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NMSpell.MODID);

    public static final List<RegistryObject<Item>> SOUL_SHARDS = new ArrayList<>();

    static {
        for (Map.Entry<Integer, String> entry : HierarchyUtils.getAscensionPath().entrySet()) {
            int rank = entry.getKey();
            String name = entry.getValue().toLowerCase()+"_soul_shard";

            RegistryObject<Item> soul_shard = ITEMS.register(name, () -> {
                SoulShardItem item = new SoulShardItem(
                        new Item.Properties()
                                .fireResistant(),
                        rank);;
                return item;
            });

            SOUL_SHARDS.add(soul_shard);

        }
    }

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
