package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.AbstractTracksBuilder;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityTracksBuilder extends AbstractTracksBuilder {
    public EntityTracksBuilder(World world) {
        super(world);
    }
}
