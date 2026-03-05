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
import java.util.List;

class TrainDeserializer implements JsonDeserializer<Class<AbstractTrains>> {
    @Override
    public Class<AbstractTrains> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return (Class<AbstractTrains>) Class.forName(json.getAsString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

class ModelDeserializer implements JsonDeserializer<ModelBase> {
    @Override
    public ModelBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ((Class<ModelBase>) Class.forName(json.getAsString())).newInstance();
        } catch (ClassNotFoundException e) {
            return BEOModelLoader.load(json.getAsString());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

class ItemDeserializer implements JsonDeserializer<Item> {
    @Override
    public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ItemIDs.valueOf(json.getAsString()).item;
        } catch (IllegalArgumentException e) {
            Item item = Item.getItemFromBlock(GameRegistry.findBlock(Info.modID, json.getAsString()));
            if (item != null)
                return item;
            else
                throw new RuntimeException(e);
        }
    }
}

class ItemStackDeserializer implements JsonDeserializer<ItemStack> {
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // If a single element, check for items and blocks for TC (used for achievements stack icon)
        if(!json.isJsonArray()) {
            try {
                return new ItemStack(ItemIDs.valueOf(json.getAsString()).item);
            } catch (IllegalArgumentException e) {
                ItemStack stack = new ItemStack(GameRegistry.findBlock(Info.modID, json.getAsString()));
                if (stack.getItem() != null)
                    return stack;
                else
                    throw new RuntimeException(e);
            }
        }
        // If an array, check if for oreDict entries, TC items, vanilla items, and vanilla blocks (used for assembly records)
        // The second element is expected to be the stack size as int
        else {
            if (json.getAsJsonArray().size() != 0) {
                String element = json.getAsJsonArray().get(0).getAsString();
                int size = json.getAsJsonArray().get(1).getAsInt();

                List<ItemStack> oreStack = OreDictionary.getOres(element);
                if (!oreStack.isEmpty()) {
                    int itemDamage = (element.equals("logWood") || element.equals("plankWood")) ? OreDictionary.WILDCARD_VALUE : oreStack.get(0).getItemDamage();

                    // getItemDamage() is important for items like dyes and ingots where it defines the variant (dye color, steel/copper...)
                    return new ItemStack(oreStack.get(0).getItem(), size, itemDamage);
                } else try {
                    return new ItemStack(ItemIDs.valueOf(element).item, size);   // TC items
                } catch (IllegalArgumentException e) {
                    ItemStack stack = new ItemStack((Item) Item.itemRegistry.getObject(element), size);  // Vanilla items
                    if (stack.getItem() != null)
                        return stack;
                    else {
                        stack = new ItemStack((Block) Block.blockRegistry.getObject(element), size); // Vanilla blocks

                        if (stack.getItem() != null)
                            return stack;
                        else
                            throw new RuntimeException(e);
                    }
                }
            }
            else
                return null;    // Arrays are used for assembly recipes, where an empty array equals an empty slot (so we don't throw there)
        }
    }
}