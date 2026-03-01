package train.common.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class AbstractStockCar extends EntityRollingStock implements IPassenger {

    public AbstractStockCar(World world) {
        super(world);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        if (riddenByEntity != null) {

            NBTTagCompound c = new NBTTagCompound();
            if(riddenByEntity.writeMountToNBT(c)) {
                nbttagcompound.setTag("mob", c);
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        if(nbttagcompound.hasKey("mob")){
            readEntityFromNBT(nbttagcompound.getCompoundTag("mob"));
        }
    }
}
