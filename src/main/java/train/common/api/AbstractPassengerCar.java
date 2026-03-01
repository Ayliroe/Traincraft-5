package train.common.api;

import net.minecraft.world.World;

public abstract class AbstractPassengerCar extends EntityRollingStock implements IPassenger {

    public AbstractPassengerCar(World world) {
        super(world);
    }
}
