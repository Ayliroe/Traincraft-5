package train.common.entity.rollingStockOld.freight;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import train.common.Traincraft;
import train.common.api.LiquidTank;
import train.common.library.GuiIDs;

public class EntityTankLava extends LiquidTank {

	public EntityTankLava(World world) {
		super(world);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!getWorld().isRemote) {
			setColor(getAmount() > 0 ? "Full" : "Empty");
		}
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.85F;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return fluid==null || fluid == FluidRegistry.LAVA;
	}
}