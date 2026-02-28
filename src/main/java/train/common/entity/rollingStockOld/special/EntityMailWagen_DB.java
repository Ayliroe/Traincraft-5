package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.AbstractWorkCart;
import train.common.library.GuiIDs;

public class EntityMailWagen_DB extends AbstractWorkCart implements IInventory {

	public EntityMailWagen_DB(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "Mail Wagen";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.84F;
	}

	@Override
 	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
}