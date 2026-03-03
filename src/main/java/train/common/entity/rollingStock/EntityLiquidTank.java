package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.LiquidTank;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityLiquidTank extends LiquidTank {
    public EntityLiquidTank(World world) {
        super(world);
    }
}
