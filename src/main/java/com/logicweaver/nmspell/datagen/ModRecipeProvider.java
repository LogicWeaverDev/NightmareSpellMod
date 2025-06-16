package com.logicweaver.nmspell.datagen;

import com.logicweaver.nmspell.item.ModItems;
import com.logicweaver.nmspell.item.classes.RankRegressionItem;
import net.minecraft.client.Minecraft;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RANK_REGRESSION.get())
                .pattern("DDD")
                .pattern("DCD")
                .pattern("DDD")
                .define('C', Items.CLOCK)
                .define('D', ModItems.SOUL_SHARDS.get(6).get())
                .unlockedBy(getHasName(ModItems.SOUL_SHARDS.get(6).get()), has(ModItems.SOUL_SHARDS.get(6).get()))
                .save(pWriter);

    }
}
