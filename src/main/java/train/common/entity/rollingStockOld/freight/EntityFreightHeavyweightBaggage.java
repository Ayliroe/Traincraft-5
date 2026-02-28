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

public class EntityFreightHeavyweightBaggage extends Freight implements IInventory {

	public EntityFreightHeavyweightBaggage(World world) {
		super(world);
	}

	@Override
	public String getInventoryName() {
		return "Heavyweight Baggage";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 5F;
	}
}