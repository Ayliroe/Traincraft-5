package train.common.entity;

import com.mojang.authlib.GameProfile;
import mods.railcraft.api.carts.IFluidCart;
import mods.railcraft.api.carts.IMinecart;
import mods.railcraft.api.carts.IRoutableCart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import train.common.Traincraft;
import train.common.api.EntityRollingStock;
import train.common.api.Locomotive;
import train.common.core.network.PacketInteract;
import train.common.core.network.PacketRemove;


public class CollisionBox extends EntityDragonPart implements IInventory, IFluidHandler, IMinecart, IFluidCart, IRoutableCart {

    public EntityRollingStock host;

    // Constructor called by both server & client hosts (note we don't need a clientside (World world) constructor, since this isn't registered as a FML entity using registerModEntity)
    public CollisionBox(EntityRollingStock host) {
        super(host, "trainbox", host.getHitboxSize()[2], host.getHitboxSize()[1]);
        this.host = host;
    }

    @Override
    public boolean interactFirst(EntityPlayer p_130002_1_) {
        if (host.getWorld().isRemote)
            Traincraft.keyChannel.sendToServer(new PacketInteract(host.getEntityId()));
        return false;
    }

    /*
     * =========================================== MINECRAFT ENTITY, ENTITYDRAGONPART ===========================================
     **/

    @Override
    public String getCommandSenderName()                                        { return host.getCommandSenderName(); }
    @Override
    public ItemStack getPickedResult(MovingObjectPosition target)               { return host.getCartItem(); }
    @Override
    public boolean isEntityEqual(Entity p_70028_1_)                             { return this == p_70028_1_ || host == p_70028_1_; }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float p_70097_2_) {
        if(worldObj.isRemote){
            Traincraft.keyChannel.sendToServer(new PacketRemove(host.getEntityId(), damageSource==null?-1:damageSource.getEntity().getEntityId()));
            return true;
        }
        return host.attackEntityFromPart(this, damageSource, p_70097_2_);
    }

    // Returning false in both of these disables writing the entity to disk
    @Override
    public boolean writeToNBTOptional(NBTTagCompound tagCompound) { return false; }
    @Override
    public boolean writeMountToNBT(NBTTagCompound tagCompound) { return false; }

    /*
     * =========================================== MINECRAFT IINVENTORY ===========================================
     **/

    @Override
    public int getSizeInventory()                                               { return host.getSizeInventory(); }
    @Override
    public ItemStack getStackInSlot(int p_70301_1_)                             { return host.getStackInSlot(p_70301_1_); }
    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)              { return host.decrStackSize(p_70298_1_, p_70298_2_); }
    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_)                    { return host.getStackInSlotOnClosing(p_70304_1_); }
    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)  { host.setInventorySlotContents(p_70299_1_, p_70299_2_); }
    @Override
    public String getInventoryName()                                            { return host.getCommandSenderName(); }
    @Override
    public boolean hasCustomInventoryName()                                     { return host.hasCustomInventoryName(); }
    @Override
    public int getInventoryStackLimit()                                         { return host.getInventoryStackLimit(); }
    @Override
    public void markDirty()                                                     { host.markDirty(); }
    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_)                   { return host.isUseableByPlayer(p_70300_1_); }
    @Override
    public void openInventory()                                                 { host.openInventory(); }
    @Override
    public void closeInventory()                                                { host.closeInventory(); }
    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)     { return host.isItemValidForSlot(p_94041_1_, p_94041_2_); }

    /*
     * =========================================== FORGE IFLUIDHANDLER ===========================================
     **/

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)   { return host.fill(from, resource, doFill); }
    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) { return host.drain(from, resource, doDrain); }
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) { return host.drain(from, maxDrain, doDrain); }
    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)                    { return host.canFill(from, fluid); }
    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)                   { return host.canDrain(from, fluid); }
    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)                     { return host.getTankInfo(from); }

    /*
     * =========================================== RAILCRAFT IMINECART, IFLUIDCART, IROUTABLECART ===========================================
     **/

    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart)    { return host.doesCartMatchFilter(stack, cart); }

    @Override
    public boolean canPassFluidRequests(Fluid fluid)                            { return true; }
    @Override
    public boolean canAcceptPushedFluid(EntityMinecart requester, Fluid fluid)  { return canFill(ForgeDirection.UNKNOWN,fluid); }
    @Override
    public boolean canProvidePulledFluid(EntityMinecart requester, Fluid fluid) { return canDrain(ForgeDirection.UNKNOWN,fluid); }
    @Override
    public void setFilling(boolean filling) {}

    @Override
    public String getDestination()                                              { return host instanceof Locomotive ? ((Locomotive)host).MTC.getDestination() : null; }
    @Override
    public boolean setDestination(ItemStack ticket)                             { return host instanceof Locomotive ? ((Locomotive)host).MTC.setDestination(ticket) : false; }
    @Override
    public GameProfile getOwner()                                               { return host.getOwner(); }
}