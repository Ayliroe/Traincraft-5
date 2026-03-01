package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerRheingoldPanorama extends AbstractPassengerCar {

	public EntityPassengerRheingoldPanorama(World world) {
		super(world);
	}

	@Override
	public float getPlayerScale() {
		return 0.65f;
	}

	@Override
	public float[][] getRiderOffsets() {
		return new float[][]{
				{1.0f,0.4f,0f},{-1.0f,0.4f,0f},{-3.05f,-0.25f,0f},{3.05f,-0.25f,0f}};
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.95F;
	}
}