package train.common.api;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import ebf.tim.entities.EntitySeat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeChunkManager;
import train.common.Traincraft;
import train.common.api.components.MTC;
import train.common.core.handlers.ConfigHandler;
import train.common.core.util.DepreciatedUtil;
import train.common.items.ItemChunkLoaderActivator;
import train.common.items.ItemPadlock;
import train.common.items.ItemPaintbrushThing;
import train.common.items.ItemWrench;
import train.common.library.GuiIDs;
import train.common.library.ItemIDs;
import train.common.library.RenderRecord;

import java.util.List;

public final class TrainUtils {

    /*
     * =========================================== CLICK ACTIONS ===========================================
     **/

    public static boolean onClickWithChunkloader(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (!train.getWorld().isRemote && ConfigHandler.CHUNK_LOADING && train instanceof Locomotive) {
            if (itemstack.getItem() instanceof ItemChunkLoaderActivator) {
                if (train.getShouldChunkLoad()) {
                    train.setShouldChunkLoad(false);
                    playerEntity.addChatMessage(new ChatComponentText("Stop loading chunks"));
                    ForgeChunkManager.releaseTicket(train.chunkTicket);
                    train.chunkTicket = null;
                } else {
                    train.setShouldChunkLoad(true);
                    playerEntity.addChatMessage(new ChatComponentText("Start loading chunks"));
                }
                itemstack.damageItem(1, playerEntity);
                return true;
            } else if (train.lockThisCart(itemstack, playerEntity)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean onClickWithDye(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (itemstack.getItem() instanceof ItemDye) {

            // If the dye matches an existing skin, then change it and reduce itemstack size
            for (String skin : train.getSkins()) {
                if (itemstack.getItemDamage() == DepreciatedUtil.getColorFromString(skin)) {
                    if (train.setSkin(skin)) {
                        itemstack.stackSize--;
                        return true;
                    }
                }
            }
            // Else print the known skins
            printPossibleSkins(train, playerEntity);
            return true;
        }
        return false;
    }

    public static boolean onClickWithWrench(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (itemstack.getItem() instanceof ItemWrench && train instanceof Locomotive && playerEntity.isSneaking() && !train.getWorld().isRemote) {
            ((Locomotive)train).MTC.destination = "";
            playerEntity.addChatMessage(new ChatComponentText("Destination reset"));
            return true;
        }
        return false;
    }

    public static boolean onClickWithCrowbar(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        ItemStack crowbar = GameRegistry.findItemStack("railcraft", "tool.crowbar", 1);
        ItemStack crowbar1 = GameRegistry.findItemStack("railcraft", "tool.crowbar.reinforced", 1);

        return itemstack == crowbar || itemstack == crowbar1;
    }

    public static boolean onClickWithStake(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (itemstack != null && itemstack.getItem() == ItemIDs.stake.item && !train.getWorld().isRemote &&
                (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || !train.links.isLinked() || train.getTrainOwner().equals(playerEntity.getDisplayName()) || train.getTrainOwner().isEmpty() || train.getTrainOwner() == null)) {
            if (playerEntity.isSneaking() && train instanceof Locomotive) {
                if (!train.canBePushed()) {
                    playerEntity.addChatMessage(new ChatComponentText(train.getTrainName() + " can be pulled, don't forget to fuel it!"));
                    ((Locomotive) train).setCanBePushed(true);
                    ((Locomotive) train).MTC.disconnectFromServer();
                } else {
                    playerEntity.addChatMessage(new ChatComponentText(train.getTrainName() + " can pull"));
                    ((Locomotive) train).setCanBePushed(false);
                }

                train.links.refreshLeadID();
                return true;
            }

            if (!train.links.getIsAttaching()) {
                train.links.setIsAttaching(true);
                playerEntity.addChatMessage(new ChatComponentText("Attaching mode on for: " + train.getTrainName()));
                itemstack.damageItem(1, playerEntity);
            } else {
                train.links.setIsAttaching(false);
                playerEntity.addChatMessage(new ChatComponentText("Reset, click again to couple new cart to this one"));
                train.links.unlink();
            }
            return true;
        }
        return false;
    }

    public static boolean onClickWithPaintbrush(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (itemstack.getItem() instanceof ItemPaintbrushThing) {
            List<String> skins = train.getSkins();
            if (!skins.isEmpty()) {
                if (playerEntity.isSneaking()) {
                    playerEntity.openGui(Traincraft.instance, GuiIDs.PAINTBRUSH, playerEntity.getEntityWorld(), train.getEntityId(), -1, (int) train.posZ);
                    return true;
                }
                else {
                    int currentSkinIndex = skins.indexOf(train.getSkin());
                    return train.setSkin(currentSkinIndex < skins.size() - 1 ? skins.get(currentSkinIndex + 1) : skins.get(0));
                }
            }
            else printPossibleSkins(train, playerEntity);
        }
        return false;
    }

    public static boolean onClickWithPadlock(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (itemstack.getItem() instanceof ItemPadlock && playerEntity.isSneaking()) {
            if (train.getTrainOwner().equalsIgnoreCase(playerEntity.getDisplayName())) {
                playerEntity.openGui(Traincraft.instance, GuiIDs.LOCK_MENU, playerEntity.getEntityWorld(), train.getEntityId(), -1, (int) train.posZ);
                return true;
            } else {
                if (!train.getWorld().isRemote) playerEntity.addChatMessage(new ChatComponentText("Train is locked by " + train.getTrainOwner() + "."));
                return false;
            }
        }
        return false;
    }

    public static boolean onClickWithTicket(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (itemstack.hasTagCompound() && MTC.getTicketDestination(itemstack) != null && !MTC.getTicketDestination(itemstack).isEmpty() && train instanceof Locomotive) {
            ((Locomotive)train).MTC.setDestination(itemstack);
            if (!train.getWorld().isRemote) playerEntity.addChatMessage(new ChatComponentText("Setting destination to " + MTC.getTicketDestination(itemstack) + "."));

            // Ticket are single use but golden ones are multiple uses
            ItemStack ticket = GameRegistry.findItemStack("Railcraft", "railcraft.routing.ticket", 1);
            if (ticket != null && ticket.getItem() != null && itemstack.getItem() == ticket.getItem()) {
                if (--itemstack.stackSize == 0) {
                    playerEntity.inventory.setInventorySlotContents(playerEntity.inventory.currentItem, null);
                }
            }
            return true;
        }
        return false;
    }

    /*
     * =========================================== GUI ===========================================
     **/

    public static boolean onOpeningGUI(EntityRollingStock train, int i, EntityPlayer playerEntity) {
        //EntityPlayer targetPlayer = (train.seats != null && !train.seats.isEmpty() && train.seats.get(0).getPassenger() != null) ? (EntityPlayer)train.seats.get(0).getPassenger() : playerEntity;
        EntityPlayer player = playerEntity;

        int targetGUI = -1;
        if (i == 7) {
            // Loco
            if (train instanceof Locomotive & train.seats != null) {
                for (EntitySeat seat : train.seats) {
                    if(seat.isControlSeat() && seat.getPassenger() != null && player == seat.getPassenger() && player.ridingEntity == seat) {
                        player = (EntityPlayer)seat.getPassenger();
                        targetGUI = GuiIDs.LOCO;
                        break;
                    } else if (seat.getPassenger() != null && seat.getPassenger() instanceof EntityPlayer) {
                        Traincraft.proxy.seatGUI((EntityPlayer) seat.getPassenger(),train);
                        break;
                    }
                }
            }
            else if (train instanceof AbstractWorkCart)     { targetGUI = GuiIDs.CRAFTING_CART; }
            else if (train instanceof AbstractControlCar)   { targetGUI = GuiIDs.CONTROL_CAR; }
            // Generic - Seat
            else if (train.seats != null && train.seats.size() > 1 && train.getSizeInventory() == 0 && train.riddenByEntity instanceof EntityPlayer) {
                player = (EntityPlayer)train.riddenByEntity;
                targetGUI = GuiIDs.SEAT_GUI;
            }
        }
        if (i == 9) {
            if (train instanceof AbstractWorkCart)          { targetGUI = GuiIDs.FURNACE_CART; }
        }

        if (targetGUI > 0) {
            player.openGui(Traincraft.instance, targetGUI, train.getWorld(), (int) train.posX, (int) train.posY, (int) train.posZ);
            return true;
        }
        return false;
    }

    public static boolean onOpeningInventory(EntityRollingStock train, EntityPlayer playerEntity) {
        EntityPlayer player = playerEntity;

        int targetGUI = -1;

        if (train instanceof Tender) {
            targetGUI = GuiIDs.TENDER;
        }
        else if (train instanceof Freight && !(train instanceof Locomotive)) {
            targetGUI = GuiIDs.FREIGHT;
        }
        else if (train instanceof LiquidTank) {
            targetGUI = GuiIDs.LIQUID;
        }
        else if (train instanceof AbstractTracksBuilder) {
            targetGUI = GuiIDs.BUILDER;
            ((AbstractTracksBuilder)train).pushZ = (train.posZ - player.posZ);
            ((AbstractTracksBuilder)train).pushX = (train.posX - player.posX);
            ((AbstractTracksBuilder)train).applyDragAndPushForces();
        }
        else if (train instanceof AbstractJukeBox) {
            targetGUI = GuiIDs.JUKEBOX;
        }

        if (targetGUI > 0) {
            player.openGui(Traincraft.instance, targetGUI, train.getWorld(), train.getEntityId(), -1, (int) train.posZ);
            return true;
        }
        return false;
    }

    /*
     * =========================================== OTHER UTILS ===========================================
     **/

    public static double convertSpeed(double speed) {
        speed /= ConfigHandler.REAL_TRAIN_SPEED?2f:6f;
        speed /= 36f; // /10 for minecraft speed
        return speed;
    }

    public static double convertSpeedInv(double speed) {
        speed *= ConfigHandler.REAL_TRAIN_SPEED ? 2 : 6;
        speed *= 36; // *10 for minecraft speed
        return speed;
    }

    public static boolean canBeAttackedBySource(AbstractTrains train, DamageSource damagesource) {
        if (damagesource.getEntity() instanceof EntityPlayer && !damagesource.isProjectile()) {
            EntityPlayer player = (EntityPlayer)damagesource.getEntity();

            // MP and locked
            if (train.getTrainLockedFromPacket() && player instanceof EntityPlayerMP) {
                // OP + Wrench
                if (player.canCommandSenderUseCommand(2, "") &&
                        player.inventory.getCurrentItem() != null &&
                        player.inventory.getCurrentItem().getItem() instanceof ItemWrench) {

                    player.addChatMessage(new ChatComponentText("Removing the train using OP permission."));
                    return true;
                }
                // Owner
                if (player.getDisplayName().equalsIgnoreCase(train.trainOwner) && train.isPlayerTrustedToBreak(player.getDisplayName())) {
                    return true;
                } else {
                    player.addChatMessage(new ChatComponentText("You are not the owner!"));
                    return false;
                }
            }
            // SP or not locked
            else return true;
        }
        return false;
    }

    public static void printPossibleSkins(AbstractTrains train, EntityPlayer player) {
        if (ConfigHandler.SHOW_POSSIBLE_COLORS) {
            if (!train.getSkins().isEmpty()) {
                String concatColors = ": ";
                for (String skin : train.getSkins()) {
                    if (!skin.equals("Empty") && !skin.equals("Full"))
                        concatColors += skin + ", ";
                }

                player.addChatMessage(new ChatComponentText("Possible skins" + concatColors));
                player.addChatMessage(new ChatComponentText("To paint, click me with the right dye"));
            }
            else player.addChatMessage(new ChatComponentText("No other skins available."));
        }
    }
}