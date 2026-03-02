package train.common.entity.rollingStockOld.freight;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.Freight;

public class EntityBoxCartPRR extends Freight {

	public EntityBoxCartPRR(World world) {
		super(world);
	}

    @Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.05F;
	}
}