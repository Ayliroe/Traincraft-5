package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricMinetrain extends ElectricTrain {
	public EntityLocoElectricMinetrain(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "Cart hauler";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.53F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.4f,1.225f, 0f}};}
    
}