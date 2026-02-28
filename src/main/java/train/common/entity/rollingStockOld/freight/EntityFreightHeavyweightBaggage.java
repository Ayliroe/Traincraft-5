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
	public int freightInventorySize;
	public int numFreightSlots;

	public EntityFreightHeavyweightBaggage(World world) {
		super(world);
		initFreightGrain();
	}

	public void initFreightGrain() {
		freightInventorySize = getSizeInventory();
		cargoItems = new ItemStack[freightInventorySize];
	}

	public EntityFreightHeavyweightBaggage(World world, double d, double d1, double d2) {
		this(world);
		setPosition(d, d1 + (double) yOffset, d2);
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = d;
		prevPosY = d1;
		prevPosZ = d2;
	}

	@Override
	public String getInventoryName() {
		return "Heavyweight Baggage";
	}

	@Override
	public boolean interactFirst(EntityPlayer entityplayer) {
		if ((super.interactFirst(entityplayer))) {
			return false;
		}
		if (!this.worldObj.isRemote) {
			entityplayer.openGui(Traincraft.instance, GuiIDs.FREIGHT, worldObj, this.getEntityId(), -1, (int) this.posZ);
		}
		return true;
	}
	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 5F;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}
}