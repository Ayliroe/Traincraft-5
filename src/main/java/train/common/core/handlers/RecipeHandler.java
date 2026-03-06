/*******************************************************************************
 * Copyright (c) 2012 Mrbrutal. All rights reserved.
 *
 * @name TrainCraft
 * @author Mrbrutal
 ******************************************************************************/

package train.common.core.handlers;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import train.common.inventory.TrainCraftingManager;
import train.common.library.BlockIDs;
import train.common.library.ItemIDs;
import train.common.recipes.RecipesArmorDyes;

import java.util.ArrayList;

public class RecipeHandler {

    public static void initBlockRecipes() {
        TrainCraftingManager.instance.getRecipeList().add(new RecipesArmorDyes());
    }

    public static void initSmeltingRecipes() {

        /* OpenHearthFurnace recipes */
        if (!ConfigHandler.MAKE_MODPACKS_GREAT_AGAIN) {
            ArrayList<ItemStack> steel = OreDictionary.getOres("ingotSteel");
            ArrayList<ItemStack> iron = OreDictionary.getOres("ingotIron");
            for (ItemStack s : steel) {
                for (ItemStack ironitm : iron) {
                    TrainCraftingManager.instance.addHearthFurnaceRecipe(ironitm,
                            new ItemStack(ItemIDs.graphite.item), s, 2F, 1000);
                }
            }
        }

        /* Vanilla Furnace recipes */
        GameRegistry.addSmelting(new ItemStack(Item.getItemFromBlock(BlockIDs.oreTC.block), 0), OreDictionary.getOres("ingotCopper").get(0), 0.7f);
    }
}
