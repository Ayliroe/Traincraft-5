package train.common.entity.rollingStockOld.freight;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import train.common.Traincraft;
import train.common.api.Freight;
import train.common.library.GuiIDs;

public class EntityFreight100TonHopper extends Freight implements IInventory {

	public EntityFreight100TonHopper(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "Freight Hopper";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.9F;
	}
}