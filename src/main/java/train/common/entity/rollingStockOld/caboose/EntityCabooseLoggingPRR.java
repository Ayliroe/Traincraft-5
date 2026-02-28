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
		initCabooseWorkCart();
	}

	public void initCabooseWorkCart() {
		furnaceItemStacks = new ItemStack[3];
		furnaceBurnTime = 0;
		currentItemBurnTime = 0;
		furnaceCookTime = 0;
	}

	public EntityCabooseLoggingPRR(World world, double d, double d1, double d2) {
		this(world);
		setPosition(d, d1 + yOffset, d2);
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
		updateBurning();
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return !isDead && entityplayer.getDistanceSqToEntity(this) <= 124D;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	public void markDirty(){}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    
}