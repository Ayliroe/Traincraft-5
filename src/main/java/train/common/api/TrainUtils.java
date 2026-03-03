package train.common.api;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
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
            if (!SkinRegistry.get(train.getName()).isEmpty()) {
                // If the color is valid for the cart, then change it and reduce itemstack size
                for (TransportSkin s : SkinRegistry.get(train.getName()).values()) {
                    if (itemstack.getItemDamage() == DepreciatedUtil.getColorFromString(s.addr)) {
                        train.setColor(s.addr);
                        itemstack.stackSize--;

                        //if (!getWorld().isRemote)PacketHandler.sendPacketToClients(PacketHandler.sendStatsToServer(10,uniqueID,trainName ,trainType, trainOwner, getColorAsString(itemstack.getItemDamage()), (int)posX, (int)posY, (int)posZ),getWorld(), (int)posX,(int)posY,(int)posZ, 12.0D);

                        return true;
                    }
                }
                if (train.getWorld().isRemote && ConfigHandler.SHOW_POSSIBLE_COLORS) {
                    String concatColors = ": ";
                    for (int t = 0; t < SkinRegistry.get(train.getName()).size(); t++) {
                        concatColors = concatColors.concat(SkinRegistry.get(train.getName()).get(t) + ", ");
                    }
                    playerEntity.addChatMessage(new ChatComponentText("Possible colors" + concatColors));
                    playerEntity.addChatMessage(new ChatComponentText("To paint, click me with the right dye"));
                    return true;
                }
            } else if (SkinRegistry.get(train.getName()) != null || SkinRegistry.get(train.getName()).isEmpty()) {
                playerEntity.addChatMessage(new ChatComponentText("No other colors available"));
            }
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
                (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || !train.isLinked() || train.getTrainOwner().equals(playerEntity.getDisplayName()) || train.getTrainOwner().isEmpty() || train.getTrainOwner() == null)) {
            if (playerEntity.isSneaking() && train instanceof Locomotive) {
                if (!train.canBePushed()) {
                    playerEntity.addChatMessage(new ChatComponentText(train.getTrainName() + " can be pulled, don't forget to fuel it!"));
                    ((Locomotive) train).setCanBePushed(true);
                    ((Locomotive) train).MTC.disconnectFromServer();
                } else {
                    playerEntity.addChatMessage(new ChatComponentText(train.getTrainName() + " can pull"));
                    ((Locomotive) train).setCanBePushed(false);
                }

                if(train.consistLeadID!=train.getEntityId()){
                    train.updateLinks();
                }
                return true;
            }

            if (!train.isAttaching) {
                train.isAttaching = true;
                playerEntity.addChatMessage(new ChatComponentText("Attaching mode on for: " + train.getTrainName()));
                itemstack.damageItem(1, playerEntity);
            } else {
                playerEntity.addChatMessage(new ChatComponentText("Reset, click again to couple new cart to this one"));
                train.Link1 = -1;
                train.Link2 = -1;
                if (train.frontLink != null && train.frontLink.Link1 == train.getUniqueTrainID()) {
					train.frontLink.Link1 = -1;
				}

                if (train.frontLink != null && train.frontLink.Link2 == train.getUniqueTrainID()) {
					train.frontLink.Link2 = -1;
				}

                if (train.backLink != null && train.backLink.Link1 == train.getUniqueTrainID()) {
					train.backLink.Link1 = -1;
				}

                if (train.backLink != null && train.backLink.Link2 == train.getUniqueTrainID()) {
					train.backLink.Link2 = -1;
				}

                if (train.frontLink != null && train.frontLink.frontLink != null && train.frontLink.frontLink.equals(train)) {
					train.frontLink.frontLink = null;
				}

                if (train.frontLink != null && train.frontLink.backLink != null && train.frontLink.backLink.equals(train)) {
					train.frontLink.backLink = null;
				}

                if (train.backLink != null && train.backLink.backLink != null && train.backLink.backLink.equals(train)) {
					train.backLink.backLink = null;
				}

                if (train.backLink != null && train.backLink.frontLink != null && train.backLink.frontLink.equals(train)) {
					train.backLink.frontLink = null;
				}

                train.frontLink = null;
                train.backLink = null;
                train.isAttaching = false;
                train.isAttached = false;
            }
            return true;
        }
        return false;
    }

    public static boolean onClickWithPaintbrush(AbstractTrains train, ItemStack itemstack, EntityPlayer playerEntity) {
        if (itemstack.getItem() instanceof ItemPaintbrushThing && playerEntity.isSneaking()) {
            if (!SkinRegistry.get(train.getName()).isEmpty()) {
                playerEntity.openGui(Traincraft.instance, GuiIDs.PAINTBRUSH, playerEntity.getEntityWorld(), train.getEntityId(), -1, (int) train.posZ);
            }

            if (SkinRegistry.get(train.getName()).isEmpty()) {
                playerEntity.addChatMessage(new ChatComponentText("There are no other colors available."));
            }
            return true;
        } else if (itemstack.getItem() instanceof ItemPaintbrushThing) {
            for (int i = 0; i < SkinRegistry.get(train.getName()).size(); i++) {
                if (train.getColor().equals(SkinRegistry.get(train.getName()).get(i))) {
                    if (SkinRegistry.get(train.getName()).size() > i+1) {
                        train.setColor(SkinRegistry.get(train.getName()).get(i+1).addr);
                    } else {
                        train.setColor(SkinRegistry.get(train.getName()).get(0).addr);
                    }
                    return true;
                }
            }
        } else if (playerEntity.isSneaking() && itemstack.getItem() instanceof ItemPadlock) {
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
}