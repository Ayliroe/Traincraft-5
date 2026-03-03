package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.DieselTrain;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityDieselTrain extends DieselTrain {
    public EntityDieselTrain(World world) {
        super(world);
    }
}
