package com.logicweaver.nmspell.datagen;

import com.logicweaver.nmspell.NMSpell;
import com.logicweaver.nmspell.item.ModItems;
import com.logicweaver.nmspell.util.HierarchyUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, NMSpell.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        for (RegistryObject<Item> soulShard : ModItems.SOUL_SHARDS) {
            simpleItem(soulShard);
        }

    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(NMSpell.MODID, "item/" + item.getId().getPath()));
    }
}
