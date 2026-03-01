package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricCD151 extends ElectricTrain {
	public EntityLocoElectricCD151(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.475F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-2.15f,1.4f, 0.3f},{2.15f,1.4f, 0.3f}};}
    
}