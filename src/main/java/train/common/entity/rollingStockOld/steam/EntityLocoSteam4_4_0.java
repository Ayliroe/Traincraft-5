package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteam4_4_0 extends SteamTrain {
	public EntityLocoSteam4_4_0(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.6F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{1.0f,1.55f, 0.2f}};}
}