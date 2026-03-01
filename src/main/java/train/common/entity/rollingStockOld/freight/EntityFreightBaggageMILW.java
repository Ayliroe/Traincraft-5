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

public class EntityFreightBaggageMILW extends Freight {

	public EntityFreightBaggageMILW(World world) {
		super(world);
	}

	@Override
	public double getMountedYOffset() {
		return (double) height * 0.0D - 0.30000001192092896D;
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.675F;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
}