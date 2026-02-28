package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteam040VB extends SteamTrain {
	public EntityLocoSteam040VB(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}

	@Override
	public String getInventoryName() {
		return "0-4-0 Vertical Boiler";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.75F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.8f,1.4f, 0.3f}};}

	@Override
	public boolean shouldRiderSit(){return false;}
}