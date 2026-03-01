package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricICE1 extends ElectricTrain {
	public EntityLocoElectricICE1(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.56F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-2.4f,1.1f, -0.2f}};}
    
}