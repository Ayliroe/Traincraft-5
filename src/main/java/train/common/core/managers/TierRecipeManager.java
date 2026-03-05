/*******************************************************************************
 * Copyright (c) 2012 Mrbrutal. All rights reserved.
 *
 * @name TrainCraft
 * @author Mrbrutal
 ******************************************************************************/

package train.common.core.managers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import train.common.api.crafting.ITierCraftingManager;
import train.common.api.crafting.ITierRecipe;

import java.util.ArrayList;
import java.util.List;

public class TierRecipeManager implements ITierCraftingManager {

    private final List<ITierRecipe> recipeList;
    private static final TierRecipeManager instance = new TierRecipeManager();

    public TierRecipeManager() {
        recipeList = new ArrayList<>();
    }

    public static ITierCraftingManager getInstance() {
        return instance;
    }

    @Override
    public void addRecipe(int tier, ItemStack planks, ItemStack wheels,
                          ItemStack frame, ItemStack coupler, ItemStack chimney,
                          ItemStack cab, ItemStack boiler, ItemStack firebox,
                          ItemStack additional, ItemStack dye, ItemStack output,
                          int outputSize) {

        recipeList.add(new TierRecipe(Math.max(1,(Math.min(3,tier))), planks, wheels, frame, coupler,
                chimney, cab, boiler, firebox, additional, dye, output,
                outputSize));
    }

    @Override
    public ITierRecipe getTierRecipe(int tier, ItemStack output) {
        if (output == null) {
            return null;
        }

        for (ITierRecipe recipe : recipeList) {
            if (Item.getIdFromItem(recipe.getOutput().getItem()) == Item.getIdFromItem(output.getItem()) &&
                    recipe.getTier() == tier) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public List<ITierRecipe> getRecipeList() {
        return new ArrayList<>(this.recipeList);
    }

    @Override
    public List<ITierRecipe> getTierRecipeList(int tier) {
        List<ITierRecipe> list = new ArrayList<>();
        for (ITierRecipe recipe : recipeList) {
            if (recipe.getTier() == tier) {
                list.add(recipe);
            }
        }

        return list;
    }

}
