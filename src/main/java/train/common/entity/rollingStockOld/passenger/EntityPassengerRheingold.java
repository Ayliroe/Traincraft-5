package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerRheingold extends AbstractPassengerCar {

	public EntityPassengerRheingold(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.94F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{
            {-2.2f,1.2f, -0.1f},{-1.0f,1.2f, -0.1f},{-0.3f,1.2f, -0.1f},{0.9f,1.2f, -0.1f},{2.1f,1.2f, -0.1f}
    };}
    
}