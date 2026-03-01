package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.library.GuiIDs;

public class EntityLocoDiesel44TonSwitcher extends DieselTrain {
	public EntityLocoDiesel44TonSwitcher(World world) {
		super(world);

	}

	@Override
	public String transportcountry(){
		return "us";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1.0F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.1f,1.6f, 0.35f},{0.1f,1.6f, -0.35f}};}
    
}