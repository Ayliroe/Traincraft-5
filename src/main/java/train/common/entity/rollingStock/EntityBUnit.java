package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.AbstractBUnit;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityBUnit extends AbstractBUnit {
    public EntityBUnit(World world) {
        super(world);
    }
}
