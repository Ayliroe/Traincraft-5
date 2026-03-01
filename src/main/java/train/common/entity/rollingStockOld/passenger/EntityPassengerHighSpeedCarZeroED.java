package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerHighSpeedCarZeroED extends AbstractPassengerCar {

	public EntityPassengerHighSpeedCarZeroED(World world) {
		super(world);
	}

	@Override
	public float[][] getRiderOffsets() {
		return new float[][]{{0,(float)getMountedYOffset(),0}};
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.17F;
	}
}