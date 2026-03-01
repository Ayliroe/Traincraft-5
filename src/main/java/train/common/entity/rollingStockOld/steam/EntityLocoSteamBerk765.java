package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamBerk765 extends SteamTrain {
	public EntityLocoSteamBerk765(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.375F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{3.4f,1.4f, 0.35f},{3.4f,1.4f, -0.35f}};}
}