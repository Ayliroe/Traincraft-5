package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractBUnit;

public class EntityBUnitDD35 extends AbstractBUnit {

    public EntityBUnitDD35(World world) {
        super(world);
    }

    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return 3.1F;
    }
}