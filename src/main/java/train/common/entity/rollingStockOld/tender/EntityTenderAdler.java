/*******************************************************************************
 * Copyright (c) 2014 Mrbrutal. All rights reserved.
 * 
 * @name Traincraft
 * @author Mrbrutal
 ******************************************************************************/

package train.common.entity.rollingStockOld.tender;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import train.common.Traincraft;
import train.common.api.LiquidManager;
import train.common.api.Tender;
import train.common.library.GuiIDs;

public class EntityTenderAdler extends Tender implements IInventory  {
	public int freightInventorySize;
	public int numFreightSlots;

	public EntityTenderAdler(World world) {
		super(world, FluidRegistry.WATER, 0, LiquidManager.WATER_FILTER);
		initFreightTender();
	}

	public void initFreightTender() {
		freightInventorySize = 16;
		cargoItems = new ItemStack[freightInventorySize];
	}

	public EntityTenderAdler(World world, double d, double d1, double d2) {
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
		checkInvent(cargoItems[0], this);
	}

	@Override
	public String getInventoryName() {
		return "Adler's Tender";
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
			entityplayer.openGui(Traincraft.instance, GuiIDs.TENDER, worldObj, this.getEntityId(), -1, (int) this.posZ);
		}
		return true;
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 0.55F;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}
}
