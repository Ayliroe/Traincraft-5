package train.common.entity.rollingStockOld.passenger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.AbstractWorkCart;
import train.common.library.GuiIDs;

public class EntityPassengerRheingoldDining1 extends AbstractWorkCart {
	public EntityPassengerRheingoldDining1(World world) {
		super(world);
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0f,-0.5f, 0f}, {3f,-0.5f, 0f}};}
	@Override
	public float[] getHitboxSize() {
		return new float[]{0.55f,2.1f,1f};
	}

	@Override
	public float[] rotationPoints() {
		return new float[]{3.125f, -3.125f};
	}
}