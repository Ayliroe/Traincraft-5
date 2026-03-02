package train.common.entity.rollingStockOld.freight;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.Freight;

public class EntityBoxCartUS extends Freight {

    public EntityBoxCartUS(World world) {
        super(world);
    }

    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return 1.65F;
    }

}