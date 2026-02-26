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
import train.common.library.ItemIDs;

import java.util.ArrayList;
import java.util.List;

public class EntityFlatCarLogs_DB extends Freight implements IInventory {
	public int freightInventorySize;
	public int numFreightSlots;

	public EntityFlatCarLogs_DB(World world) {
		super(world);
		initFreightCart();
	}

	public void initFreightCart() {
		numFreightSlots = 9;
		freightInventorySize = 45;
		cargoItems = new ItemStack[freightInventorySize];
	}

	public EntityFlatCarLogs_DB(World world, double d, double d1, double d2) {
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
		return "Wood transport";
	}

	@Override
	public int getSizeInventory() {
		return freightInventorySize;
	}

	@Override
	public boolean interactFirst(EntityPlayer entityplayer) {
		playerEntity = entityplayer;
		if ((super.interactFirst(entityplayer))) {
			return false;
		}
		entityplayer.openGui(Traincraft.instance, GuiIDs.FREIGHT, worldObj, this.getEntityId(), -1, (int) this.posZ);
		return true;
	}

	@Override
	public List<ItemStack> getItemsDropped() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		items.add(new ItemStack(ItemIDs.minecartFlatCartLogs_DB.item));
		return items;
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.84F;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}
}