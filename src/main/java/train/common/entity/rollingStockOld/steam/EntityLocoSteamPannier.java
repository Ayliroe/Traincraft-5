package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamPannier extends SteamTrain {
	public EntityLocoSteamPannier(World world) {
		super(world, LiquidManager.WATER_FILTER);
	}
	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.3F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{2.1f,1.7f, 0.3f}};}

	@Override
	public boolean shouldRiderSit(){return false;}
}