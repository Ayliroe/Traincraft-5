package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassenger2 extends AbstractPassengerCar {

	public EntityPassenger2(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.2F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    
}