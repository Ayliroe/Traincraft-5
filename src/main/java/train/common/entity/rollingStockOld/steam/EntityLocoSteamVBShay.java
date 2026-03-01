package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamVBShay extends SteamTrain {
	public EntityLocoSteamVBShay(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.275F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.5f,1.5f, 0f}};}

	@Override
	public boolean shouldRiderSit(){return false;}
}