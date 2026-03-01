package train.common.entity.rollingStockOld.caboose;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityCaboose3 extends AbstractPassengerCar {

    public EntityCaboose3(World world) {
        super(world);
    }

    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return 1.3F;
    }
	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    
}