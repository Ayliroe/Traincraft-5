package train.common.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import train.common.Traincraft;
import train.common.core.handlers.ConfigHandler;
import train.common.core.managers.TierRecipeManager;
import train.common.inventory.TrainCraftingManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RecipeRecord {

    public static void init() {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream("assets/tc/data/RecipeRecords.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        Gson gson = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackDeserializer()).create();

        for (RecipeRecord record : gson.fromJson(reader, RecipeRecord[].class)) {

            boolean allowedByConfig = true;

            if (record.config.equals("ComputerCraft"))
                allowedByConfig = Loader.isModLoaded("ComputerCraft");
            if (record.config.equals("hardMode"))
                allowedByConfig = !ConfigHandler.MAKE_MODPACKS_GREAT_AGAIN;
            if (record.config.equals("ingotCopper"))
                allowedByConfig = OreDictionary.getOres("ingotCopper") != null;
            if (record.config.equals("workbench"))
                allowedByConfig = !ConfigHandler.DISABLE_TRAIN_WORKBENCH;
            if (record.config.equals("zeppelin"))
                allowedByConfig = ConfigHandler.ENABLE_ZEPPELIN;


            if (allowedByConfig) {

                /*
                 * Builds up an Object[] in the format used by the minecraft crafting table.
                 * Empty lines are pruned to reduce the crafting area (ex. for slabs). The crafting table seems to handle itself recipe widths, so we don't change that.
                 * One difference is we don't remove duplicates, so every item gets assigned a unique character.
                 * Example shaped output:       new Object[] { "A  ", " B ", "  C", 'A', stackA, 'B', stackB, 'C', stackC };
                 * Example shapeless output:    new Object[] { stackA, stackB, stackC };
                 **/

                if (record.shape.equals("shaped")) {

                    String line1 =
                            (record.inputs[0] != null ? "A" : " ") +
                            (record.inputs[1] != null ? "B" : " ") +
                            (record.inputs[2] != null ? "C" : " ");

                    String line2 =
                            (record.inputs[3] != null ? "D" : " ") +
                            (record.inputs[4] != null ? "E" : " ") +
                            (record.inputs[5] != null ? "F" : " ");

                    String line3 =
                            (record.inputs[6] != null ? "G" : " ") +
                            (record.inputs[7] != null ? "H" : " ") +
                            (record.inputs[8] != null ? "I" : " ");

                    List<Object> inputs = new ArrayList<>(Arrays.asList(
                            line1.trim().isEmpty() ? null : line1,
                            line2.trim().isEmpty() ? null : line2,
                            line3.trim().isEmpty() ? null : line3,
                            record.inputs[0] != null ? 'A' : null, record.inputs[0],
                            record.inputs[1] != null ? 'B' : null, record.inputs[1],
                            record.inputs[2] != null ? 'C' : null, record.inputs[2],
                            record.inputs[3] != null ? 'D' : null, record.inputs[3],
                            record.inputs[4] != null ? 'E' : null, record.inputs[4],
                            record.inputs[5] != null ? 'F' : null, record.inputs[5],
                            record.inputs[6] != null ? 'G' : null, record.inputs[6],
                            record.inputs[7] != null ? 'H' : null, record.inputs[7],
                            record.inputs[8] != null ? 'I' : null, record.inputs[8]));

                    inputs.removeAll(Collections.singleton(null));

                    if (record.crafter.equals("vanilla") || record.crafter.equals("any"))
                        GameRegistry.addRecipe(record.output, inputs.toArray());
                    if (record.crafter.equals("workbench") || record.crafter.equals("any")) {
                        TrainCraftingManager.instance.addRecipe(record.output, inputs.toArray());
                    }
                }
                else {
                    List<ItemStack> inputs = new ArrayList<>(Arrays.asList(
                            record.inputs[0],
                            record.inputs[1],
                            record.inputs[2],
                            record.inputs[3],
                            record.inputs[4],
                            record.inputs[5],
                            record.inputs[6],
                            record.inputs[7],
                            record.inputs[8]));

                    inputs.removeAll(Collections.singleton(null));

                    if (record.crafter.equals("vanilla") || record.crafter.equals("any"))
                        GameRegistry.addShapelessRecipe(record.output, inputs.toArray());
                    if (record.crafter.equals("workbench") || record.crafter.equals("any"))
                        TrainCraftingManager.instance.addShapelessRecipe(record.output, inputs.toArray());
                }
            }
        }
    }

    private ItemStack output;
    private ItemStack[] inputs;
    private String shape;
    private String crafter;
    private String type; // This is just for reference, if needed later
    private String config;
}