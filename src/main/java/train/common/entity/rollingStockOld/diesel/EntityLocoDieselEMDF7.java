package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselEMDF7 extends DieselTrain {
	public EntityLocoDieselEMDF7(World world) {
		super(world);

	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-0.8f,1.5f, 0.25f}};}
    
}