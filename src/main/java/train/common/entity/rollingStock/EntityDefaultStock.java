package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.EntityRollingStock;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityDefaultStock extends EntityRollingStock {
    public EntityDefaultStock(World world) {
        super(world);
    }
}
