package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerBamboo extends AbstractPassengerCar {

	public EntityPassengerBamboo(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.55F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-0.5f,0.9f, 0f},{0.5f,0.9f, 0f}};}
    
}