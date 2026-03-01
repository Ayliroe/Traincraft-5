package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.library.GuiIDs;

public class EntityLocoSteam262T extends SteamTrain {
	public EntityLocoSteam262T(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.6F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{1.7f,1.4f, 0.3f}};}

	@Override
	public boolean shouldRiderSit(){return false;}
}