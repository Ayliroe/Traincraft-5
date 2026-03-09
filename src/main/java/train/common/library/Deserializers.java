package train.common.library;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import cpw.mods.fml.common.registry.GameRegistry;
import fexcraft.fvtm.BEOModelLoader;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import train.common.api.AbstractTrains;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

class TrainDeserializer implements JsonDeserializer<Class<AbstractTrains>> {
    @Override
    public Class<AbstractTrains> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return DeserializingUtils.trainClassForString(json.getAsString());
    }
}

class ModelDeserializer implements JsonDeserializer<ModelBase> {
    @Override
    public ModelBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return DeserializingUtils.modelForString(json.getAsString());
    }
}

class ItemDeserializer implements JsonDeserializer<Item> {
    @Override
    public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return DeserializingUtils.stackForString(json.getAsString(), 1).getItem();
    }
}

/**
 * Universal ItemStack deserializer that can handle stacks as a direct string ("namespace:item" or "namespace:item:damage"), or as an item-size pair (["namespace:item:damage", size])
 **/
class ItemStackDeserializer implements JsonDeserializer<ItemStack> {
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // If not an array, it's a simple stack
        if(!json.isJsonArray()) {
            if (!json.getAsString().isEmpty())
                return DeserializingUtils.stackForString(json.getAsString(), 1);

            // Empty strings return null (for empty slots in vanilla/workbench recipes)
            else
                return null;
        }

        // If an array, the second element is the size
        else {
            if (json.getAsJsonArray().size() != 0)
                return DeserializingUtils.stackForString(json.getAsJsonArray().get(0).getAsString(), json.getAsJsonArray().get(1).getAsInt());

            // Empty arrays return null (for empty slots in assembly recipes)
            else
                return null;
        }
    }
}

class DeserializingUtils {

    public static Class<AbstractTrains> trainClassForString(String string) {
        try {
            return (Class<AbstractTrains>) Class.forName(string);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModelBase modelForString(String string) {
        try {
            return ((Class<ModelBase>) Class.forName(string)).newInstance();
        } catch (ClassNotFoundException e) {
            return BEOModelLoader.load(string);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Util for converting a serialized ItemStack string into a stack of the matching mod/ore item, stack size and stack damage
     * Currently supports tc, minecraft and oreDict entries; can be extended later.
     **/
    public static ItemStack stackForString(String string, int size) {
        List<String> strings = Arrays.asList(string.split(":"));

        // Stacks are serialized as "namespace:item:damage", with damage being optional (often defines item variants, for example dye color, steel/copper ingot...)
        String namespace = strings.get(0);
        String item = strings.get(1);
        int damage = strings.size() > 2 ? Integer.parseInt(strings.get(2)) : 0;

        ItemStack stack;

        if (namespace.equals(Info.modID)) {
            try {
                stack = new ItemStack(BlockIDs.valueOf(item).block, size, damage);  // Check BlockIDs first, because some blocks are double-registered as ItemIDs with null items
            } catch (IllegalArgumentException e) {
                try {
                    stack = new ItemStack(ItemIDs.valueOf(item).item, size, damage);
                } catch (IllegalArgumentException f) {
                    stack = new ItemStack(GameRegistry.findBlock(Info.modID, item), size, damage); // For TCBlocks that are not registered as enum
                    if (stack.getItem() == null) {
                        stack = new ItemStack(GameRegistry.findItem(Info.modID, item), size, damage);
                    }
                }
            }
        }
        else if(namespace.equals("ore")) {
            ItemStack ore = OreDictionary.getOres(item).get(0);

            // Use the item damage from the found ore
            stack = new ItemStack(ore.getItem(), size, ore.getItemDamage());
        }
        else if (namespace.equals("minecraft")) {
            stack = new ItemStack((Item) Item.itemRegistry.getObject(item), size, damage);
            if (stack.getItem() == null) {
                stack = new ItemStack((Block) Block.blockRegistry.getObject(item), size, damage);
            }
        }
        else
            throw new NullPointerException("Namespace could not be found for: " + string);

        if (stack.getItem() != null)
            return stack;
        else
            throw new NullPointerException("Item could not be found for: " + string);
    }
}