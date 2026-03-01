package train.common.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import train.common.api.LiquidManager.StandardTank;
import train.common.entity.rollingStockOld.special.EntityBUnitDD35;
import train.common.entity.rollingStockOld.special.EntityBUnitEMDF3;
import train.common.entity.rollingStockOld.special.EntityBUnitEMDF7;

public abstract class Tender extends Freight implements IFluidHandler {

    private int maxTank;
    private int update = 8;
    private StandardTank theTank;
    public TileEntity[] blocksToCheck;

    public Tender(World world) {
        this(new FluidStack(FluidRegistry.WATER, 0), world, LiquidManager.WATER_FILTER);
    }

    private Tender(FluidStack fluid, World world, FluidStack filter) {
        super(world);
        maxTank = getTankCapacity()[0];
        if (filter == null)
            theTank = LiquidManager.getInstance().new StandardTank(maxTank);
        if (filter != null)
            theTank = LiquidManager.getInstance().new FilteredTank(maxTank, filter);
        IFluidTank[] tankArray = new IFluidTank[1];
        tankArray[0] = theTank;
        dataWatcher.addObject(4, 0);
        dataWatcher.addObject(27, 0);
    }

    @Override
    public int getSizeInventory() { return 16; };

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        theTank.writeToNBT(nbttagcompound);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        theTank.readFromNBT(nbttagcompound);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (getWorld().isRemote)
            return;
        if (theTank != null && theTank.getFluid() != null) {
            dataWatcher.updateObject(27, theTank.getFluid().amount);
            dataWatcher.updateObject(4, theTank.getFluid().getFluidID());
        } else if (theTank != null && theTank.getFluid() == null) {
            dataWatcher.updateObject(27, 0);
            dataWatcher.updateObject(4, 0);
        }
        checkInvent(cargoItems[0], this);
    }

    /**
     * handle mass depending on items and liquid
     */
    @Override
    protected void handleMass() {
        if (ticksExisted % 10 != 0) return;
        double preciseAmount = 0;
        mass = getDefaultMass();
        if (theTank != null && theTank.getFluid() != null && theTank.getFluid().amount > 0) {
            preciseAmount = theTank.getFluid().amount;
        }
        itemInsideCount = 0;
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack itemstack = getStackInSlot(i);
            if (itemstack != null && itemstack.stackSize > 0) {
                itemInsideCount += itemstack.stackSize;
            }
        }
        mass += (itemInsideCount * 0.0001);//1 item = 1 kilo
        mass += (preciseAmount / 10000);//1 bucket = 1 kilo
    }

    /**
     * added for SMP, used by the HUD
     *
     * @return
     */
    public int getWater() {
        return (dataWatcher.getWatchableObjectInt(27));
    }

    /**
     * used by the GUI
     *
     * @return int
     */
    public int getLiquidItemID() {
        return (dataWatcher.getWatchableObjectInt(4));
    }

    public int getCartTankCapacity() {
        return maxTank;
    }

    public StandardTank getTank() {
        return theTank;
    }

    private void placeInInvent(ItemStack itemstack1, Tender tender) {
        for (int i = 1; i < tender.cargoItems.length; i++) {
            if (tender.cargoItems[i] == null) {
                tender.cargoItems[i] = itemstack1;
                return;
            } else if (tender.cargoItems[i] != null && tender.cargoItems[i].getItem() == itemstack1.getItem() && itemstack1.isStackable() &&
                    (!itemstack1.getHasSubtypes() || tender.cargoItems[i].getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(tender.cargoItems[i], itemstack1)) {
                int var9 = tender.cargoItems[i].stackSize + itemstack1.stackSize;
                if (var9 <= tender.cargoItems[i].getMaxStackSize()) {
                    tender.cargoItems[i].stackSize = var9;
                    return;
                } else if (tender.cargoItems[i].stackSize < tender.cargoItems[i].getMaxStackSize()) {
                    tender.cargoItems[i].stackSize += 1;
                    return;
                }
            } else if (i == tender.cargoItems.length - 1) {
                entityDropItem(itemstack1, 1);
                return;
            }
        }
    }

    public void liquidInSlot(ItemStack itemstack, Tender tender) {
        if (getWorld().isRemote)
            return;
        update += 1;
        if (update % 8 == 0 && itemstack != null) {
            ItemStack result = LiquidManager.getInstance().processContainer(this, 0, this, itemstack);
            if (result != null) {
                placeInInvent(result, tender);
                decrStackSize(0, 1);
            }
        }
    }

    protected void checkInvent(ItemStack tenderInvent, Tender loco) {
        if (tenderInvent != null) {
            liquidInSlot(tenderInvent, loco);
        }

        if (ticksExisted % 5 == 0 && fill(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 100), false) == 100) {
            FluidStack drain = null;
            blocksToCheck = new TileEntity[]{getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY - 1), MathHelper.floor_double(posZ)),
                    getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 2), MathHelper.floor_double(posZ)),
                    getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 3), MathHelper.floor_double(posZ)),
                    getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 4), MathHelper.floor_double(posZ))
            };

            for (TileEntity block : blocksToCheck) {
                if (drain == null && block instanceof IFluidHandler) {
                    for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                        if (((IFluidHandler) block).drain(direction, 100, false) != null &&
                                ((IFluidHandler) block).drain(direction, 100, false).fluid == FluidRegistry.WATER &&
                                ((IFluidHandler) block).drain(direction, 100, false).amount == 100
                        ) {
                            drain = ((IFluidHandler) block).drain(
                                    direction, 100, true);
                        }
                    }
                }
            }
            if (drain == null && frontLink instanceof LiquidTank
                    && !(frontLink instanceof EntityBUnitEMDF7) && !(frontLink instanceof EntityBUnitEMDF3) && !(frontLink instanceof EntityBUnitDD35)) {
                if (getFluid() == null) {
                    drain = ((LiquidTank) frontLink).drain(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 100), true);
                } else if (getFluid().getFluid() == FluidRegistry.WATER) {
                    drain = ((LiquidTank) frontLink).drain(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 100), true);
                }
            } else if (drain == null && backLink instanceof LiquidTank
                    && !(frontLink instanceof EntityBUnitEMDF7) && !(frontLink instanceof EntityBUnitEMDF3) && !(frontLink instanceof EntityBUnitDD35)) {
                if (getFluid() == null) {
                    drain = ((LiquidTank) backLink).drain(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 100), true);
                } else if (getFluid().getFluid() == FluidRegistry.WATER) {
                    drain = ((LiquidTank) backLink).drain(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 100), true);
                }
            }
            if (drain != null) {
                fill(ForgeDirection.UNKNOWN, drain, true);
            }
        }
    }

    /*IInventory implements*/
    @Override
    public ItemStack getStackInSlot(int i) {
        return cargoItems[i];
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int par1) {
        if (cargoItems[par1] != null) {
            ItemStack var2 = cargoItems[par1];
            cargoItems[par1] = null;
            return var2;
        } else {
            return null;
        }
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (cargoItems[i] != null) {
            if (cargoItems[i].stackSize <= j) {
                ItemStack itemstack = cargoItems[i];
                cargoItems[i] = null;
                return itemstack;
            }
            ItemStack itemstack1 = cargoItems[i].splitStack(j);
            if (cargoItems[i].stackSize == 0) {
                cargoItems[i] = null;
            }
            return itemstack1;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        cargoItems[i] = itemstack;
        if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }

    public void setLiquid(FluidStack liquid) {
    }

    public void setCapacity(int capacity) {
        maxTank = capacity;
    }

    public int getCapacity() {
        return maxTank;
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
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{theTank.getInfo()};
    }

    public FluidStack getFluid() {
        return theTank.getFluid();
    }

    public int getFluidAmount() {
        return theTank.getFluidAmount();
    }

    @Override
    public void dropCartAsItem(boolean isCreative) {
        if (!itemdropped) {
            super.dropCartAsItem(isCreative);
            for (ItemStack stack : cargoItems) {
                if (stack != null) {
                    entityDropItem(stack, 0);
                }
            }
        }
    }
}