package train.common.api;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public abstract class AbstractLavaTank extends LiquidTank {

    public AbstractLavaTank(World world) {
        super(world);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!getWorld().isRemote) {
            setColor(getAmount() > 0 ? "Full" : "Empty");
        }
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluid==null || fluid == FluidRegistry.LAVA;
    }
}
