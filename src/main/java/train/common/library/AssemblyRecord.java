package train.common.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.ItemStack;
import train.common.Traincraft;
import train.common.core.managers.TierRecipeManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class AssemblyRecord {

    public static void init() {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream("assets/tc/data/AssemblyRecords.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        Gson gson = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackDeserializer()).create();

        for (AssemblyRecord record : gson.fromJson(reader, AssemblyRecord[].class)) {
            TierRecipeManager.getInstance().addRecipe(
                    record.tier,
                    record.planks,
                    record.wheels,
                    record.frame,
                    record.coupler,
                    record.chimney,
                    record.cab,
                    record.boiler,
                    record.firebox,
                    record.additional,
                    record.dye,
                    record.output,
                    record.output.stackSize);
        }
    }

    private int tier;
    private ItemStack planks;
    private ItemStack wheels;
    private ItemStack frame;
    private ItemStack coupler;
    private ItemStack chimney;
    private ItemStack cab;
    private ItemStack boiler;
    private ItemStack firebox;
    private ItemStack additional;
    private ItemStack dye;
    private ItemStack output;
}
