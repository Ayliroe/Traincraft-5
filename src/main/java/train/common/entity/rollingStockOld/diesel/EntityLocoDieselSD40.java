package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselSD40 extends DieselTrain {
	public EntityLocoDieselSD40(World world) {
		super(world, LiquidManager.dieselFilter());

	}


	@Override
	public String getInventoryName() {
		return "SD40-2";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1.2F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-1,1.4f, 0.2f}};}
    
}