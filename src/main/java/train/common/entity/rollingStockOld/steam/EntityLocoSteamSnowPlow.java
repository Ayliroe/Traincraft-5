package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractSteamSnowPlow;

public class EntityLocoSteamSnowPlow extends AbstractSteamSnowPlow {

	public EntityLocoSteamSnowPlow(World world) {
		super(world);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (0.7F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
}