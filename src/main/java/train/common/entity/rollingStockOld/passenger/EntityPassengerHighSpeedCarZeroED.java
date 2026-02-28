package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.api.EntityRollingStock;
import train.common.api.IPassenger;

public class EntityPassengerHighSpeedCarZeroED extends EntityRollingStock implements IPassenger {

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