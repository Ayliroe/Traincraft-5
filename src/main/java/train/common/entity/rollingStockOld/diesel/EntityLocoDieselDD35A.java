package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselDD35A extends DieselTrain {
	public EntityLocoDieselDD35A(World world) {
		super(world, LiquidManager.dieselFilter());

	}

	@Override
	public String getInventoryName() {
		return "DD35A";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.3F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-2.8f,1.5f, -0.2f}};}
    
}