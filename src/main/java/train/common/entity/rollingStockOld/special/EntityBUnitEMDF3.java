package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractBUnit;

public class EntityBUnitEMDF3 extends AbstractBUnit {

    public EntityBUnitEMDF3(World world) {
        super(world);
    }

    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return 2.2F;
    }
}