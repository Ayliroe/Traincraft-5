package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.AbstractLavaTank;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityLavaTank extends AbstractLavaTank {
    public EntityLavaTank(World world) {
        super(world);
    }
}
