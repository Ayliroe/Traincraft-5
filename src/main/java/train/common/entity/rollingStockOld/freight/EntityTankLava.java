package train.common.entity.rollingStockOld.freight;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractLavaTank;

public class EntityTankLava extends AbstractLavaTank {

	public EntityTankLava(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.85F;
	}
}