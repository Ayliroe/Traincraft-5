package train.common.entity.rollingStockOld.electric;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.ElectricTrain;
import train.common.library.GuiIDs;

public class EntityLocoElectricTramWood extends ElectricTrain {
	public EntityLocoElectricTramWood(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "Tram";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		//float dist = 0.1F;
		return (0.7F);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{-1.25f,1.1f, 0f},{0.6f,1.1f, -0.3f},{-0.6f,1.1f, 0.3f}};}
    
}