package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamFowler extends SteamTrain {
	public EntityLocoSteamFowler(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}

	@Override
	public String getInventoryName() {
		return "Fowler 4F";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.5F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{2,1.4f, 0.3f}};}
}