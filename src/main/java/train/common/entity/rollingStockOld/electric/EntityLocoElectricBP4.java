package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricBP4 extends ElectricTrain {
	public EntityLocoElectricBP4(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "BP4";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (0.7F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-1.7f,1.35f, 0.25f}};}
    
}