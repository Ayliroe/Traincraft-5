package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.Freight;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityFreight extends Freight {
    public EntityFreight(World world) {
        super(world);
    }
}
