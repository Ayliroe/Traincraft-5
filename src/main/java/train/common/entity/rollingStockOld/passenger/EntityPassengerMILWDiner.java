package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityPassengerMILWDiner extends AbstractPassengerCar {

	public EntityPassengerMILWDiner(World world) {
		super(world);
	}

	@Override
	public void updateRiderPosition() {
		if(riddenByEntity==null){return;}
		riddenByEntity.setPosition(posX, posY + getMountedYOffset() + riddenByEntity.getYOffset() + 0.2, posZ);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.1F;
	}
}