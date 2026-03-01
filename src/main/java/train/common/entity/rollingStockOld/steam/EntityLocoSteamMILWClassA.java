package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteamMILWClassA extends SteamTrain {
	public EntityLocoSteamMILWClassA(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (0.8F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{2.8f,1.35f, 0.3f}};}
}