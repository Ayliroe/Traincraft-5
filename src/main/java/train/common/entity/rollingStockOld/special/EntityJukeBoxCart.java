package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractJukeBox;

public class EntityJukeBoxCart extends AbstractJukeBox {

	public EntityJukeBoxCart(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.85F;
	}
}