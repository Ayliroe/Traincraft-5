package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.AbstractSteamSnowPlow;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntitySteamSnowPlow extends AbstractSteamSnowPlow {
    public EntitySteamSnowPlow(World world) {
        super(world);
    }
}
