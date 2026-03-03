package train.common.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import train.common.library.TrainRecord;
import train.common.api.LiquidManager.StandardTank;

public abstract class AbstractBUnit extends LiquidTank implements IFluidHandler {

    private int update = 8;
    private StandardTank theTank;

    public AbstractBUnit(World world) {
        super(world);
    }

    @Override
    public void init(TrainRecord spec) {
        super.init(spec);
        theTank = createTank();
    }

    private StandardTank createTank() { return LiquidManager.getInstance().new FilteredTank(getTankCapacity()[0], LiquidManager.dieselFilter()); }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (worldObj.isRemote) {
            return;
        }

        if (theTank != null && theTank.getFluid() != null) {
            dataWatcher.updateObject(18, theTank.getFluidAmount());
            dataWatcher.updateObject(4, theTank.getFluid().getFluidID());
            if (theTank.getFluid().amount <= 1) {
                motionX *= 0.94;
                motionZ *= 0.94;
            }
        } else if (theTank != null && theTank.getFluid() == null) {
            dataWatcher.updateObject(18, 0);
            dataWatcher.updateObject(4, 0);
        }

        if (getAmount() > 0) {
            // setColor(getColorFromString("Full"));
            setDefaultMass(-weightKg());
            if ((motionX > 0.01 || motionZ > 0.01) && ticksExisted % 40 == 0) {
                drain(ForgeDirection.UNKNOWN, 8, true);
            }

        } else if (getAmount() <= 0) {
            // setColor(getColorFromString("Empty"));
            setDefaultMass(weightKg());
        }
    }

    public int getDiesel() {
        return (dataWatcher.getWatchableObjectInt(18));
    }

    public LiquidManager.StandardTank getTank() {
        return theTank;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        if (theTank != null && theTank.getFluid() != null) {
            new FluidStack(theTank.getFluid(), dataWatcher.getWatchableObjectInt(18)).writeToNBT(nbttagcompound);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        if (theTank == null) { theTank = createTank(); }
        if (nbttagcompound.hasKey("FluidName")) {
            fill(ForgeDirection.UNKNOWN, FluidStack.loadFluidStackFromNBT(nbttagcompound), true);
        }
    }

    private void placeInInvent(ItemStack itemstack1) {
        for (int i = 1; i < cargoItems.length; i++) {
            if (cargoItems[i] == null) {
                cargoItems[i] = itemstack1;
                return;
            } else if (cargoItems[i] != null && cargoItems[i].getItem() == itemstack1.getItem() && itemstack1.isStackable() && (!itemstack1.getHasSubtypes() || cargoItems[i].getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(cargoItems[i], itemstack1)) {
                int var9 = cargoItems[i].stackSize + itemstack1.stackSize;
                if (var9 <= itemstack1.getMaxStackSize()) {
                    cargoItems[i].stackSize = var9;

                } else if (cargoItems[i].stackSize < itemstack1.getMaxStackSize()) {
                    cargoItems[i].stackSize += 1;
                }
                return;
            } else if (i == cargoItems.length - 1) {
                entityDropItem(itemstack1, 1);
                return;
            }
        }
    }

    public void liquidInSlot(ItemStack itemstack) {
        if (worldObj.isRemote)
            return;
        update += 1;
        if (update % 8 == 0 && itemstack != null) {
            ItemStack result = LiquidManager.getInstance().processContainer(this, 0, this, itemstack);
            if (result != null) {
                placeInInvent(result);
            }
        }
    }

    @Override
    public ItemStack checkInvent(ItemStack cargoItems0) {
        if (getDiesel() > 0) {
            fuelTrain = (getDiesel());
        }

        if (fuelTrain <= 0) {
            motionX *= 0.88;
            motionZ *= 0.88;
        }

        if (cargoItems0 != null) {
            liquidInSlot(cargoItems0);
        }
        return cargoItems0;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return theTank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || !resource.isFluidEqual(theTank.getFluid())) {
            return null;
        }
        return theTank.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return theTank.drain(maxDrain, doDrain);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{theTank.getInfo()};
    }

    public FluidStack getFluid() {
        return theTank.getFluid();
    }

    public int getFluidAmount() {
        return dataWatcher.getWatchableObjectInt(18);
    }

    @Override
    public String getLiquidName() {
        return FluidRegistry.getFluid(dataWatcher.getWatchableObjectInt(4)) != null ? FluidRegistry.getFluid(dataWatcher.getWatchableObjectInt(4)).getName() : null;
    }
}
