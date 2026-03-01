package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselGP7Red extends DieselTrain {
	public EntityLocoDieselGP7Red(World world) {
		super(world, LiquidManager.dieselFilter());

	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1.14F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-0.5f,1.2f, 0.25f}};}
    
}