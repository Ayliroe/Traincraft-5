package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamLSSP7 extends SteamTrain {
	public EntityLocoSteamLSSP7(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}

	@Override
	public String getInventoryName() {
		return "LSSP 7";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
			return 0.5f;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.65f,1.25f, 0.3f}};}
}