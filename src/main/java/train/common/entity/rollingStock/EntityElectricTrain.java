package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.ElectricTrain;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityElectricTrain extends ElectricTrain {
    public EntityElectricTrain(World world) {
        super(world);
    }
}
