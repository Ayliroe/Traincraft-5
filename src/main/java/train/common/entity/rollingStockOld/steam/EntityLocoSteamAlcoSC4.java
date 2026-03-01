package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamAlcoSC4 extends SteamTrain {
	public EntityLocoSteamAlcoSC4(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.6F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{2.1f,1.5f, -0.3f},{2.1f,1.5f, 0.3f}};}
}