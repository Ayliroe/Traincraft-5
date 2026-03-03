package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.AbstractStockCar;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityStockCar extends AbstractStockCar {
    public EntityStockCar(World world) {
        super(world);
    }
}
