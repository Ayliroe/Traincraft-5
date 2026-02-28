package train.common.entity.rollingStockOld.caboose;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.AbstractWorkCart;
import train.common.library.GuiIDs;

public class EntityCabooseLoggingPRR extends AbstractWorkCart implements IInventory {

	public EntityCabooseLoggingPRR(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "Logging Caboose";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.2F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    
}