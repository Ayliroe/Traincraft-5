package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricE10_DB extends ElectricTrain {
	public EntityLocoElectricE10_DB(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "E10 (DB)";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-2.1f,1.4f, -0.2f},{2.2f,1.4f, 0.2f}};}
    
}