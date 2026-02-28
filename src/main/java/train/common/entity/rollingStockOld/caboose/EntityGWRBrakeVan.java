package train.common.entity.rollingStockOld.caboose;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.AbstractWorkCart;
import train.common.library.GuiIDs;

public class EntityGWRBrakeVan extends AbstractWorkCart implements IInventory {

	public EntityGWRBrakeVan(World world) {
		super(world);
		initWorkCart();
	}

	public EntityGWRBrakeVan(World world, double d, double d1, double d2) {
		this(world);
		setPosition(d, d1 + (double) yOffset, d2);
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = d;
		prevPosY = d1;
		prevPosZ = d2;
	}

	public void initWorkCart() {
		furnaceItemStacks = new ItemStack[3];
		furnaceBurnTime = 0;
		currentItemBurnTime = 0;
		furnaceCookTime = 0;
	}

	@Override
	public String getInventoryName() {
		return "GWR Brake Van";
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		updateBurning();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return !isDead && entityplayer.getDistanceSqToEntity(this) <= 64D;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	public void markDirty(){}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.4F;
	}
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.6f, -0.2f}};}
    
}