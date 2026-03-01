package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamC62Class extends SteamTrain {
	public EntityLocoSteamC62Class(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (0.5F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{3,1.5f, 0.3f}};}
}