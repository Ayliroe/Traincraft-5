package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerMILW extends AbstractPassengerCar {

	public EntityPassengerMILW(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.7F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    
}