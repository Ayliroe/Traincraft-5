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
    public String transportcountry() {
        return "us";
    }

    @Override
    public String transportYear() {
        return "";
    }

    @Override
    public float[][] getRiderOffsets(){return new float[][]{{1f,0.5f, 0.2f}};}

    @Override
    public float[] getHitboxSize() {
        return new float[]{3.9375f,1.875f,1.375f};
    }
}
