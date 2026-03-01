package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerDBOriental extends AbstractPassengerCar {

	public EntityPassengerDBOriental(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.25F;
	}

	public float[] rotationPoints() {
		return new float[]{1f, 0f};
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f},{-1,1.2f, 0f},{1,1.2f, 0f}};}
    
}