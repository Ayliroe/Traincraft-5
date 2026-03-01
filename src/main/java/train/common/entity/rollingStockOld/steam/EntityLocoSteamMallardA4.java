package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamMallardA4 extends SteamTrain {
	public EntityLocoSteamMallardA4(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{2.65f,1.6f, 0.425f}};}
}