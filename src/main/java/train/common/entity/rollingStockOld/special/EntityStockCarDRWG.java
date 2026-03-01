package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractStockCar;

public class EntityStockCarDRWG extends AbstractStockCar {
	public EntityStockCarDRWG(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.65F;
	}
}