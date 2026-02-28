package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamGLYN042T extends SteamTrain {
	public EntityLocoSteamGLYN042T(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}

	@Override
	public String getInventoryName() {
		return "0-4-2 GLYN";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.9F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{1.2f,1.3f, 0.4f}};}

	@Override
	public boolean shouldRiderSit(){return false;}
}