package train.common.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import train.common.api.LiquidManager.StandardTank;
import train.common.core.handlers.FuelHandler;

public abstract class SteamTrain extends Locomotive implements IFluidHandler {

	protected int maxTank;
	private int maxFuel = 20000;
	private int update = 8;
	private StandardTank theTank;
	private IFluidTank[] tankArray = new IFluidTank[1];

	public SteamTrain(World world) {
		super(world);
		maxTank = getTankCapacity()[0];
		theTank = LiquidManager.getInstance().new FilteredTank(maxTank, LiquidManager.WATER_FILTER);
		tankArray[0] = theTank;
		dataWatcher.addObject(4, 0);
		dataWatcher.addObject(27, 0);
	}

	@Override
	public int getSizeInventory() {
		return 11+(getInventoryRows()*9);
	}

	/**
	 * returns the waterConsumption for each steam loco default is 200: rand.nextInt(200)==0
	 * 
	 * @return
	 */
	public int getWaterConsumption() {
		return getSpec().getWaterConsumption();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		/**
		 * so the client side knows the water amount
		 */
		if (getWorld().isRemote) {
			return;
		}
		if (theTank != null && theTank.getFluid() != null) {
			dataWatcher.updateObject(27, theTank.getFluid().amount);
			dataWatcher.updateObject(4, theTank.getFluid().getFluidID());
		}

		if (theTank != null && theTank.getFluid() != null && getFuel() > 0) {
			if (theTank.getFluid().amount <= 1) {
				motionX *= 0.94;
				motionZ *= 0.94;
			}
		}
		else if (theTank != null && theTank.getFluid() == null) {
			dataWatcher.updateObject(27, 0);
			dataWatcher.updateObject(4, 0);
		}
		if (rand.nextInt(100) == 0 && getWater() > 0 && getFuel() > 0) {
			drain(ForgeDirection.UNKNOWN, getWaterConsumption() / 5, true);
		}

		checkInvent(cargoItems[0], cargoItems[1], this);
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

	private void placeInInvent(ItemStack itemstack1, SteamTrain loco) {
		for (int i = 2; i < loco.cargoItems.length; i++) {
			if (loco.cargoItems[i] == null) {
				loco.cargoItems[i] = itemstack1;
				return;
			}
			else if (loco.cargoItems[i] != null && loco.cargoItems[i].getItem() == itemstack1.getItem() && itemstack1.isStackable() &&
					(!itemstack1.getHasSubtypes() || cargoItems[i].getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(cargoItems[i], itemstack1)) {
				int var9 = cargoItems[i].stackSize + itemstack1.stackSize;
				if (var9 <= itemstack1.getMaxStackSize()) {
					loco.cargoItems[i].stackSize = var9;
					return;
				}
				else if (cargoItems[i].stackSize < cargoItems[i].getMaxStackSize()) {
					loco.cargoItems[i].stackSize += 1;
					return;
				}
			}
			else if (i == loco.cargoItems.length - 1) {
				entityDropItem(itemstack1,1);
				return;
			}
		}
	}

	public void liquidInSlot(ItemStack itemstack, SteamTrain loco) {

		if (getWorld().isRemote)
			return;
		update += 1;
		if (update % 8 == 0 && itemstack != null) {
			ItemStack result = LiquidManager.getInstance().processContainer(this, 1, this, itemstack); //'this' needs to be the loco inventory, but that's not an inventory it's a Itemstack[]
			if (result != null) {
				placeInInvent(result, loco);
				decrStackSize(1, 1);
			}
		}
	}

	protected void checkInvent(ItemStack locoInvent0, ItemStack locoInvent1, SteamTrain loco) {
		if (!canCheckInvent)
			return;

		boolean hasCoalInTender = false;
		if (isLocoTurnedOn && ticksExisted%10==0) {
			FluidStack drain = null;

			if(fill(ForgeDirection.UNKNOWN,new FluidStack(FluidRegistry.WATER, 100), false)==100) {
				TileEntity[] blocksToCheck = new TileEntity[]{getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY - 1), MathHelper.floor_double(posZ)),
						getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 2), MathHelper.floor_double(posZ)),
						getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 3), MathHelper.floor_double(posZ)),
						getWorld().getTileEntity(MathHelper.floor_double(posX), MathHelper.floor_double(posY + 4), MathHelper.floor_double(posZ))
				};

				for (TileEntity block : blocksToCheck) {
					if (drain == null && block instanceof IFluidHandler) {
						for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
							if(((IFluidHandler) block).drain(direction,100,false)!=null &&
									((IFluidHandler) block).drain(direction, 100, false).fluid==FluidRegistry.WATER &&
									((IFluidHandler) block).drain(direction, 100, false).amount ==100
							) {
								drain = ((IFluidHandler) block).drain(
										direction, 100, true);
							}
						}
					}
				}
			}

			AbstractTrains[] links = {backLink,frontLink};
			for (AbstractTrains link : links) {
				if (link instanceof Tender){
					if(drain==null && fill(ForgeDirection.UNKNOWN,new FluidStack(FluidRegistry.WATER, 100), false)==100) {
						if (getFluid() == null || getFluid().getFluid() == FluidRegistry.WATER) {
							drain = link.drain(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 100), true);
						}
					}

					for (int h = 0; h < ((Tender) link).cargoItems.length; h++) {
						if (((Tender) link).cargoItems[h] != null && FuelHandler.steamFuelLast(((Tender) link).cargoItems[h]) != 0) {
							if (getFuel() < maxFuel && ((getFuel() + FuelHandler.steamFuelLast(((Tender) link).cargoItems[h])) <= maxFuel)) {
								fuelTrain += FuelHandler.steamFuelLast(((Tender) link).cargoItems[h]);
								hasCoalInTender = true;
								link.decrStackSize(h, 1);
								break;
							}
						}
					}
				}
			}

			if (drain != null){
				fill(ForgeDirection.UNKNOWN, drain, true);
			}
		}
		if (!hasCoalInTender && locoInvent0 != null && FuelHandler.steamFuelLast(locoInvent0) != 0) {
			if (getFuel() < maxFuel && ((getFuel() + FuelHandler.steamFuelLast(locoInvent0) <= maxFuel))) {
				fuelTrain += FuelHandler.steamFuelLast(locoInvent0);
				decrStackSize(0, 1);
			}
		}


		if (locoInvent1 != null) {
			liquidInSlot(locoInvent1, loco);
			return;
		}
		if (getFuel() <= 0) {
			motionX *= 0.88;
			motionZ *= 0.88;
		}
	}

	/** Used for the gui */
	@Override
	public int getFuelDiv(int i) {
		if (getWorld().isRemote) {
			return ((dataWatcher.getWatchableObjectInt(24) * i) / maxFuel);
		}
		return (fuelTrain * i) / maxFuel;
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
		return new FluidTankInfo[] { theTank.getInfo() };
	}

	public FluidStack getFluid() {
		return theTank.getFluid();
	}

	public int getFluidAmount() {
		return theTank.getFluidAmount();
	}
}