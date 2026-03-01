package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;

public class EntityLocoSteamC41 extends SteamTrain {
	public EntityLocoSteamC41(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.6F;
	}

	@Override
	public float[][] getRiderOffsets() { return new float[][] {{-0.2f,0.2f,-0.4f},{-0.2f,0.2f,0.4f}}; }

	@Override
	public float getPlayerScale() {
		return 0.65f;
	}
}