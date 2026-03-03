package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.SteamTrain;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntitySteamTrain extends SteamTrain {
    public EntitySteamTrain(World world) {
        super(world);
    }
}
