package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractBUnit;

public class EntityBUnitEMDF7 extends AbstractBUnit {

    public EntityBUnitEMDF7(World world) {
        super(world);
    }

    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return 2.2F;
    }
}