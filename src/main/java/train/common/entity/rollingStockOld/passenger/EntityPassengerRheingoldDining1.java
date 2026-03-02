package train.common.entity.rollingStockOld.passenger;

import net.minecraft.world.World;
import train.common.api.AbstractWorkCart;

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
}