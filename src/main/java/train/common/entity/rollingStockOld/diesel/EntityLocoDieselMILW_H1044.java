package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselMILW_H1044 extends DieselTrain {
	public EntityLocoDieselMILW_H1044(World world) {
		super(world, LiquidManager.dieselFilter());

	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (0.8F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{2.2f,1.25f, 0.35f}};}
    
}