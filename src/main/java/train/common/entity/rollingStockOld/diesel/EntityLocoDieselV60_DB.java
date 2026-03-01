package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselV60_DB extends DieselTrain {
	public EntityLocoDieselV60_DB(World world) {
		super(world);

	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1.3F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.3f,1.6f, 0.3f}};}
    
}