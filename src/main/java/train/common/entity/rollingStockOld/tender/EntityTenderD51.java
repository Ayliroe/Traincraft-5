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

public class EntityTenderD51 extends Tender {

	public EntityTenderD51(World world) {
		super(world, FluidRegistry.WATER, 0, LiquidManager.WATER_FILTER);
	}

	@Override
	public String getInventoryName() {
		return "D51 Tender [JNR]";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.9F;
	}
}