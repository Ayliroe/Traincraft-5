package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselFOLM1 extends DieselTrain {
	public EntityLocoDieselFOLM1(World world) {
		super(world);

	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-2.2f,1.4f, 0f}};}
    
}