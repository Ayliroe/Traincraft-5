package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerICE_2class extends AbstractPassengerCar {

	public EntityPassengerICE_2class(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.1F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    
}