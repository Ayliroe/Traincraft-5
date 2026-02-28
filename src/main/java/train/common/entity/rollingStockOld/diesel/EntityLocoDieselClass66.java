package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselClass66 extends DieselTrain {
	public EntityLocoDieselClass66(World world) {
		super(world, LiquidManager.dieselFilter());

	}

	@Override
	public String getInventoryName() {
		return "Class 66";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-3,1.25f, 0.2f},{2.9f,1.25f, -0.15f}};}
    
}