package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselDeltic extends DieselTrain {
	public EntityLocoDieselDeltic(World world) {
		super(world, LiquidManager.dieselFilter());

	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.5F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-2.4f,1.7f, 0f},{2.4f,1.7f, 0.2f}};}
    
}