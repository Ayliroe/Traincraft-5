package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricTramNY extends ElectricTrain {
	public EntityLocoElectricTramNY(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.7F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-1.9f,1.2f, 0.45f},{-0.7f,1.2f, -0.3f},{0.9f,1.2f, 0.3f}};}
    
}