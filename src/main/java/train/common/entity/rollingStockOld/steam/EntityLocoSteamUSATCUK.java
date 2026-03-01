package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamUSATCUK extends SteamTrain {
	public EntityLocoSteamUSATCUK(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.65F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{1.1f,1.2f, -0.25f},{1.1f,1.2f, 0.25f}};}
}