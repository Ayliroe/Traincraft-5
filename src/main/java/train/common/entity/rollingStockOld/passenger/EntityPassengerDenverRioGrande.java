package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerDenverRioGrande extends AbstractPassengerCar {

	public EntityPassengerDenverRioGrande(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.15F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.2f,1.5f, 0f},{-2.2f,1.5f, 0f},{-1f,1.5f, 0f},{1.2f,1.5f, 0f}};}
    
}