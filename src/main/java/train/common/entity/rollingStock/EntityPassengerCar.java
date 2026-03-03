package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityPassengerCar extends AbstractPassengerCar {
    public EntityPassengerCar(World world) {
        super(world);
    }
}
