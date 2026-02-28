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

public class EntityTenderA4 extends Tender {

	public EntityTenderA4(World world) {
		super(world, FluidRegistry.WATER, 0, LiquidManager.WATER_FILTER);
	}

	@Override
	public String getInventoryName() {
		return "A4 Tender";
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.1F;
	}
}