package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDieselWLs40 extends DieselTrain {
	public EntityLocoDieselWLs40(World world) {
		super(world);

	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{1.4f,1.4f, 0.3f}};}
    
}