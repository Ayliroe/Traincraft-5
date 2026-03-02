package train.common.entity.rollingStock;

import ebf.tim.api.SkinRegistry;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import train.common.Traincraft;
import train.common.api.AbstractPassengerCar;
import train.common.items.ItemRollingStock;
import train.common.library.Info;
import train.common.library.ItemIDs;

/**
 * <h1>Pullman's Palace entity</h1>
 * For more information on the overrides and functions:
 * @see EntityPassengerCar1
 * @author Eternal Blue Flame
 */
public class EntityPassengerCar1 extends AbstractPassengerCar {

    public EntityPassengerCar1(World world){
        super(world);
    }

    @Override
    public float[] rotationPoints(){return new float[]{1.7f,-1.7f};}

    @Override
    public String getDefaultSkin(){
        return "Red";
    }

    @Override
    public ItemStack[] getRecipe() {
        ItemStack dyeBlue = OreDictionary.getOres("dyeBlue").get(0);
        ItemStack itemSteel = OreDictionary.getOres("ingotSteel").get(0);

        return new ItemStack[]{
            new ItemStack(itemSteel.getItem(), 5, itemSteel.getItemDamage()),
            new ItemStack(ItemIDs.bogie.item, 2),
            new ItemStack(ItemIDs.steelframe.item, 2),
            new ItemStack(itemSteel.getItem(), 2, itemSteel.getItemDamage()),
            null,
            new ItemStack(ItemIDs.steelcab.item, 1),
            null,
            new ItemStack(ItemIDs.seats.item, 1),
            null,
            new ItemStack(dyeBlue.getItem(), 1, dyeBlue.getItemDamage()),
            new ItemStack(ItemIDs.minecartPassengerBlue.item)

        };
    }

    @Override
    public int getTier(){ return 2; }

    @Override
    public String transportcountry() {
        return "us";
    }

    @Override
    public String transportYear() {
        return "";
    }

    @Override
    public boolean isFictional() {
        return true;
    }

    @Override
    public String[] additionalItemText() {
        return null;
    }

    /**
     * <h2>Inventory Size</h2>
     */
    @Override
    public int getInventoryRows(){return 0;}/**
     * <h2>Rider offsets</h2>
     */
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1f,0.5f, 0.2f}};}

    @Override
    public float[] getHitboxSize() {
        return new float[]{3.9375f,1.875f,1.375f};
    }

    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.client.render.models.ModelPassenger6()};}
    @Override
    public float[][] modelRotations() {
        return new float[][] {{0.0f,180.0f,0.0f}};
    }

    @Override
    public float[][] modelOffsets() { return new float[][] {{0.0f, -0.47f, 0.0f}};}
}
