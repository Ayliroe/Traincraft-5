package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricBR185 extends ElectricTrain {
	public EntityLocoElectricBR185(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "BR 185";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.7F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-2.1f,1.2f, -0.3f},{2.1f,1.2f, 0.3f}};}
    
}