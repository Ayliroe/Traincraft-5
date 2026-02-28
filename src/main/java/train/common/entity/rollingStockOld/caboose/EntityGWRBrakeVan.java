package train.common.entity.rollingStockOld.caboose;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.AbstractWorkCart;
import train.common.library.GuiIDs;

public class EntityGWRBrakeVan extends AbstractWorkCart implements IInventory {

	public EntityGWRBrakeVan(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "GWR Brake Van";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.4F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.6f, -0.2f}};}
    
}