package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractTracksBuilder;

public class EntityTracksBuilder extends AbstractTracksBuilder {

	public EntityTracksBuilder(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 2.1F;
	}
}