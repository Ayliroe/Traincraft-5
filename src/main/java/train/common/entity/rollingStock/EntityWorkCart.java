package train.common.entity.rollingStock;

import net.minecraft.world.World;
import train.common.api.AbstractWorkCart;

// Only use this class for rendering logic, use the abstract one for gameplay
public class EntityWorkCart extends AbstractWorkCart {
    public EntityWorkCart(World world) {
        super(world);
    }
}
