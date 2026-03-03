package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.Tender;
import train.common.library.TrainRecord;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityTender extends Tender {
    public EntityTender(World world) {
        super(world);
    }
}
