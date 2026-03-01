package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.LiquidManager;
import train.common.api.components.SnowPlow;
import train.common.api.SteamTrain;

public class EntityLocoSteamSnowPlow extends SteamTrain {

	SnowPlow snowPlow = new SnowPlow(this);

	public EntityLocoSteamSnowPlow(World world) {
		super(world);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		checkInvent(cargoItems[0], cargoItems[1], this);

		snowPlow.updatePlow();
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (0.7F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
}