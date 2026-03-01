package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.api.AbstractWorkCart;

public class EntityWorkCart extends AbstractWorkCart {
	public EntityWorkCart(World world) {
		super(world);
	}

	@Override
	public float[][] getRiderOffsets() {

		return new float[][]{{0f,-0.15f,0f}};
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return (1.8F);
	}
}