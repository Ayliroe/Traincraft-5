package train.common.entity.rollingStockOld.freight;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import train.common.Traincraft;
import train.common.api.LiquidTank;
import train.common.library.GuiIDs;

public class EntityTankWagon2 extends LiquidTank {
	public int freightInventorySize;
	public EntityTankWagon2(World world) {
		super(world);
		initFreightWater();
	}

	public void initFreightWater() {
		freightInventorySize = 2;
		cargoItems = new ItemStack[freightInventorySize];
	}
	public EntityTankWagon2(World world, double d, double d1, double d2) {
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
	public void onUpdate() {
		super.onUpdate();
		checkInvent(cargoItems[0]);
	}

	@Override
	public String getInventoryName() {
		return "Tank cart";
	}

	@Override
	public int getSizeInventory() {
		return freightInventorySize;
	}

	@Override
	public boolean interactFirst(EntityPlayer entityplayer) {
		if ((super.interactFirst(entityplayer))) {
			return false;
		}
		if (!this.worldObj.isRemote) {
			entityplayer.openGui(Traincraft.instance, GuiIDs.LIQUID, worldObj, this.getEntityId(), -1, (int) this.posZ);
		}
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return!isDead && entityplayer.getDistanceSqToEntity(this) <= 64D;
	}
	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.54F;
	}
}