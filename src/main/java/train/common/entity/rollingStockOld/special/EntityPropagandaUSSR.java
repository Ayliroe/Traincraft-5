package train.common.entity.rollingStockOld.special;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import train.common.adminbook.ServerLogger;
import train.common.api.EntityRollingStock;

public class EntityPropagandaUSSR extends EntityRollingStock {


	public EntityPropagandaUSSR(World world) {
		super(world);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 3.7F;
	}
}