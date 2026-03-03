package train.common.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import train.common.api.LiquidManager.StandardTank;
import train.common.library.TrainRecord;

public abstract class DieselTrain extends Locomotive implements IFluidHandler {

	private int maxTank = 7 * 1000;
	private int update = 8;
	private StandardTank theTank;

	public DieselTrain(World world) {
		super(world);
		dataWatcher.addObject(4, 0);
		dataWatcher.addObject(27, 0);
		dataWatcher.addObject(5, "");
	}

	@Override
	public void init(TrainRecord spec) {
		super.init(spec);

		maxTank = getTankCapacity()[0];

		FluidStack filter = null;
		String[] multiFilter = LiquidManager.dieselFilter();

		if (filter == null && multiFilter == null) {
			theTank = LiquidManager.getInstance().new StandardTank(maxTank);
		}if (filter != null) {
			theTank = LiquidManager.getInstance().new FilteredTank(maxTank, filter);
		}if (multiFilter != null) {
			theTank = LiquidManager.getInstance().new FilteredTank(maxTank, multiFilter);
		}
	}

	@Override
	public int getSizeInventory() { return 10; }

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!getWorld().isRemote) {
			if (theTank.getFluidAmount() != dataWatcher.getWatchableObjectInt(27)){
				dataWatcher.updateObject(27, theTank.getFluidAmount());
				fuelTrain = theTank.getFluidAmount();
				dataWatcher.updateObject(4, theTank.getFluid()!=null?theTank.getFluid().getFluidID():0);
				dataWatcher.updateObject(5, theTank.getFluid()!=null?theTank.getFluid().getUnlocalizedName():"");
			}
			if (isLocoTurnedOn && theTank.getFluidAmount() >0) {
				if (theTank.getFluid().amount <= 1) {
					motionX *= 0.94;
					motionZ *= 0.94;
				}
			}
			checkInvent(cargoItems[0]);
		}
	}

	public int getDiesel() {
		return getFuel()==0?(dataWatcher.getWatchableObjectInt(27)):getFuel();
	}
	public String getLiquidName(){ return  dataWatcher.getWatchableObjectString(5);}

	public int getLiquidItemID() {
		return (dataWatcher.getWatchableObjectInt(4));
	}

	public StandardTank getTank() {
		return theTank;
	}

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

	public int getCartTankCapacity() {
		return maxTank;
	}

	private void placeInInvent(ItemStack itemstack1) {
		for (int i = 1; i < cargoItems.length; i++) {
			if (cargoItems[i] == null) {
				cargoItems[i] = itemstack1;
				return;
			}
			else if (cargoItems[i] != null && cargoItems[i].getItem() == itemstack1.getItem() && itemstack1.isStackable() && (!itemstack1.getHasSubtypes() || cargoItems[i].getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(cargoItems[i], itemstack1)) {
				int var9 = cargoItems[i].stackSize + itemstack1.stackSize;
				if (var9 <= itemstack1.getMaxStackSize()) {
					cargoItems[i].stackSize = var9;

				}
				else if (cargoItems[i].stackSize < itemstack1.getMaxStackSize()) {
					cargoItems[i].stackSize += 1;
				}
				return;
			}
			else if (i == cargoItems.length - 1) {
				entityDropItem(itemstack1,1);
				return;
			}
		}
	}

	public void liquidInSlot(ItemStack itemstack) {
		if (getWorld().isRemote)
			return;
		update += 1;
		if (update % 8 == 0 && itemstack != null) {
			ItemStack result = LiquidManager.getInstance().processContainer(this, 0, this, itemstack);
			if (result != null) {
				placeInInvent(result);
			}
		}
	}

	protected ItemStack checkInvent(ItemStack locoInvent0) {
		if (!canCheckInvent)
			return locoInvent0;

		if (getDiesel() > 0) {
			fuelTrain = (getDiesel());
		}
		if (fuelTrain <= 0) {
			motionX *= 0.88;
			motionZ *= 0.88;
		}
		if (locoInvent0 != null) {
			liquidInSlot(locoInvent0);
		}
		return locoInvent0;
	}

	@Override
	protected void updateFuel() {
		if (ticksExisted%5==0 &&getTank().getFluidAmount()+100 < maxTank) {
			FluidStack drain = null;
			TileEntity[] blocksToCheck = new TileEntity[]{getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY - 1), MathHelper.floor_double(posZ)),
					getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 2), MathHelper.floor_double(posZ)),
					getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 3), MathHelper.floor_double(posZ)),
					getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 4), MathHelper.floor_double(posZ))
			};

			for (TileEntity block : blocksToCheck) {
				if (drain == null && block instanceof IFluidHandler) {
					for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
						if (((IFluidHandler) block).drain(direction, 100, false) != null &&
								(getFluid()==null || ((IFluidHandler) block).drain(direction, 100, false).fluid==getTank().getFluid().fluid) &&
								((IFluidHandler) block).drain(direction, 100, false).amount == 100
						) {
							drain = ((IFluidHandler) block).drain(
									direction, 100, true);
						}
					}
				}
			}
			if(drain==null && frontLink instanceof LiquidTank && !(frontLink instanceof AbstractBUnit)){
				if (getFluid() == null) {
					drain = ((LiquidTank) frontLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.DIESEL, 100), true);
					if (drain == null){
						drain = ((LiquidTank) frontLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.REFINED_FUEL, 50), true);
					}
				} else if (getFluid().getFluid() == LiquidManager.DIESEL) {
					drain = ((LiquidTank) frontLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.DIESEL, 100), true);
				} else {
					drain = ((LiquidTank) frontLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.REFINED_FUEL, 50), true);
				}
			} else if (drain==null && backLink instanceof LiquidTank && !(backLink instanceof AbstractBUnit)){
				if (getFluid() == null) {
					drain = ((LiquidTank) backLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.DIESEL, 100), true);
					if (drain == null){
						drain = ((LiquidTank) backLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.REFINED_FUEL, 50), true);
					}
				} else if (getFluid().getFluid() == LiquidManager.DIESEL) {
					drain = ((LiquidTank) backLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.DIESEL, 100), true);
				} else {
					drain = ((LiquidTank) backLink).drain(ForgeDirection.UNKNOWN, new FluidStack(LiquidManager.REFINED_FUEL, 100), true);
				}
			}
			if (drain != null){
				fill(ForgeDirection.UNKNOWN, drain, true);
			}
		}

		if (fuelTrain > 1 && isLocoTurnedOn) {
			fuelTrain -= (int)fuelRate;
			if (fuelTrain < 0) {
				fuelTrain = 0;
				drain(ForgeDirection.UNKNOWN, (int)fuelRate, true);
				setLocoTurnedOnFromPacket(false);
			} else {
				drain(ForgeDirection.UNKNOWN, (int)fuelRate, true);
			}
		}
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
		return theTank ==null? null:theTank.drain(maxDrain, doDrain);
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
		return new FluidTankInfo[] { theTank.getInfo() };
	}

	public FluidStack getFluid() {
		return theTank.getFluid();
	}

	public int getFluidAmount() {
		return theTank.getFluidAmount();
	}
}