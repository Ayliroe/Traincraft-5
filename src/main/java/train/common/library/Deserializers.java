package train.common.library;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import cpw.mods.fml.common.registry.GameRegistry;
import fexcraft.fvtm.BEOModelLoader;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import train.common.api.AbstractTrains;

import java.lang.reflect.Type;

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
}