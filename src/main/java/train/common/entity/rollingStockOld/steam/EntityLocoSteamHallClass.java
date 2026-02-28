package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamHallClass extends SteamTrain {
	public EntityLocoSteamHallClass(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}

	@Override
	public String getInventoryName() {
		return "Hall Class";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.2F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{2.8f,1.5f, 0.3f}};}
}