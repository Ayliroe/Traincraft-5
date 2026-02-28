package train.common.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.NetworkRegistry;
import ebf.tim.utility.CommonUtil;
import mods.railcraft.api.carts.CartTools;
import mods.railcraft.api.carts.IRoutableCart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import train.common.Traincraft;
import train.common.core.handlers.ConfigHandler;
import train.common.core.network.PacketParkingBrake;
import train.common.mtc.PDMMessage;
import train.common.mtc.TilePDMInstructionRadio;
import train.common.mtc.packets.*;

import java.util.List;
import java.util.Random;

/*
 * -------------------------------------------------------------------------------------------------
 * =========================================== MTC & ATO ===========================================
 * -------------------------------------------------------------------------------------------------
 **/

public class MTC implements WirelessTransmitter, IRoutableCart {

    // --- SERVER ---
    public String serverUUID = "";
    public String trainID;
    public String trainLevel = "1";

    // --- MTC & ATO ---
    public int mtcStatus = 0;
    public int mtcType = 1;
    public int atoStatus = 0;
    public Double xFromStopPoint = 0.0;
    public Double yFromStopPoint = 0.0;
    public Double zFromStopPoint = 0.0;
    public Double distanceFromStopPoint = 0.0;
    public Double xStationStop = 0.0;
    public Double yStationStop = 0.0;
    public Double zStationStop = 0.0;
    public Double distanceFromStationStop = 0.0;
    public boolean stationStopping = false;
    public int speedLimit = 0;
    public int nextSpeedLimit = 0;
    public Double distanceFromSpeedChange = 0.0;
    public Double xSpeedLimitChange = 0.0;
    public Double ySpeedLimitChange = 0.0;
    public Double zSpeedLimitChange = 0.0;
    public boolean isDriverOverspeed = false;
    public boolean overspeedBrakingInProgress = false;
    public Boolean mtcOverridePressed = false;
    public Boolean overspeedOveridePressed = false;
    public String currentSignalBlock = "";
    public boolean speedGoingDown = false;
    public boolean stationStop = false;
    public String destination = "";

    private final Locomotive loco;

    MTC(Locomotive loco) {
        this.loco = loco;
    }

    /*
     * =========================================== INIT ===========================================
     **/

    public void generateTrainID() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder sb = new StringBuilder(5);
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        trainID = sb.toString();
    }

    /*
     * =========================================== UPDATE ===========================================
     **/

    public void updateMTCandATO() {
        if (loco == null) return;

        // --- MTC (Minecraft Train Control) ---
        if (mtcStatus == 1 | mtcStatus == 2) {
            if (mtcType == 2) {
                //Send updates every few seconds
                if (loco.ticksExisted % 20 == 0 && !loco.canBePushed()) {
                    JsonObject sendingObj = new JsonObject();
                    sendingObj.addProperty("funct", "update");
                    sendingObj.addProperty("signalBlock", currentSignalBlock);
                    sendingObj.addProperty("destination", loco.getDestinationGUI());
                    sendingObj.addProperty("trainLevel", trainLevel);
                    sendMessage(new PDMMessage(trainID, serverUUID, sendingObj.toString(), 1));
                }
            }
            isDriverOverspeed = loco.getSpeed() > speedLimit && speedLimit != 0;

            if (isDriverOverspeed && loco.ticksExisted % 120 == 0 && !overspeedBrakingInProgress && !overspeedOveridePressed && atoStatus != 1) {
                //Start braking because the driver is an idiot.
                overspeedBrakingInProgress = true;
            }
            if (overspeedBrakingInProgress && atoStatus != 1) {
                if (loco.getSpeed() < speedLimit) {
                    //Stop overspeed braking.
                    overspeedBrakingInProgress = false;
                    isDriverOverspeed = false;
                } else {
                    slow(speedLimit);
                }
            }
            distanceFromStopPoint = loco.getDistance(xFromStopPoint, yFromStopPoint, zFromStopPoint);
            distanceFromSpeedChange = loco.getDistance(xSpeedLimitChange, ySpeedLimitChange, zSpeedLimitChange);

            if (distanceFromSpeedChange <= speedLimit && distanceFromSpeedChange <= loco.getSpeed() && !(distanceFromSpeedChange <= nextSpeedLimit)) {
                speedLimit = (int) Math.round(distanceFromSpeedChange);
                speedGoingDown = true;

                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) loco.posX, (int) loco.posY, (int) loco.posZ, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                if (distanceFromSpeedChange <= 6) {
                    xSpeedLimitChange = 0.0;
                    ySpeedLimitChange = 0.0;
                    zSpeedLimitChange = 0.0;
                    speedLimit = nextSpeedLimit;
                    nextSpeedLimit = 0;
                    Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) loco.posX, (int) loco.posY, (int) loco.posZ, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                    Traincraft.itnsChannel.sendToAllAround(new PacketNextSpeed( nextSpeedLimit, 0,0,0, xSpeedLimitChange, ySpeedLimitChange, zSpeedLimitChange, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                    speedGoingDown = false;
                }

            }

            if (distanceFromStopPoint >= 40 && distanceFromStopPoint < speedLimit && !(xFromStopPoint == 0.0) && mtcType == 1){
                speedLimit = (int)Math.round(distanceFromStopPoint);
                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) loco.posX, (int) loco.posY, (int) loco.posZ, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                speedGoingDown = true;
            }
            if (distanceFromStopPoint >= 10 && distanceFromStopPoint < speedLimit && !(xFromStopPoint == 0.0) && mtcType == 2){
                speedLimit = (int)Math.round(distanceFromStopPoint);
                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) loco.posX, (int) loco.posY, (int) loco.posZ, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                speedGoingDown = true;
            }

				/*if (distanceFromStopPoint < getSpeed() && !(distanceFromStopPoint < nextSpeedLimit)  && !(this instanceof EntityLocoElectricPeachDriverlessMetro)) {
					speedLimit = (int) Math.round(distanceFromStopPoint);
					Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) posX, (int) posY, (int) posZ, getEntityId()), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D) );
				}*/

            // --- ATO (Automatic Train Operation) ---
            if (atoStatus == 1) {
                distanceFromStationStop = loco.getDistance(xStationStop, yStationStop, zStationStop);
                if (loco.parkingBrake) {
                    loco.parkingBrake = false;
                    //Accelerate to the speed limit
                }
                if (!(distanceFromStopPoint < loco.getSpeed()) && (!(distanceFromSpeedChange < loco.getSpeed()))) {
                    accel(speedLimit);
                }


                if (distanceFromStopPoint < loco.getSpeed()) {
                    //Stop it at a certain point
                    stop(Vec3.createVectorHelper(xFromStopPoint, yFromStopPoint, zFromStopPoint));
                }
                if (distanceFromStationStop < loco.getSpeed()) {
                    stop(Vec3.createVectorHelper(xStationStop, yStationStop, zStationStop));
                    stationStopping = true;

                } else {
                    stationStopping = false;
                }

                if (distanceFromSpeedChange < loco.getSpeed() && !(loco.getSpeed() == nextSpeedLimit)) {
                    //Slow it down to the next speed limit
                    slow(nextSpeedLimit);
                }

                if (isDriverOverspeed) {
                    //The ATO system is speeding somehow, slow it down
                    slow(speedLimit);
                }
                if (distanceFromStopPoint < 2 || distanceFromStationStop < 2) {
                    loco.parkingBrake = true;
                    if (distanceFromStopPoint < 2) {
                        xFromStopPoint = 0.0;
                        yFromStopPoint = 0.0;
                        zFromStopPoint = 0.0;
                    } else {
                        xStationStop = 0.0;
                        yStationStop = 0.0;
                        zStationStop = 0.0;
                    }
                    atoStatus = 0;
                    stationStop = true;

                    Traincraft.atoChannel.sendToAllAround(new PacketATO(loco.getEntityId(), 0),new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));

                    Traincraft.atoSetStopPoint.sendToAllAround(new PacketATOSetStopPoint(loco.getEntityId(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                    Traincraft.brakeChannel.sendToAllAround(new PacketParkingBrake(true, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                    JsonObject sendingObj = new JsonObject();
                    sendingObj.addProperty("funct", "stationstopcomplete");
                    sendMessage(new PDMMessage(trainID, serverUUID, sendingObj.toString(), 0));
                }
            }
        }
    }

    public void accel(Integer desiredSpeed) {
        if (loco.getWorld() != null) {
            if (loco.getSpeed() != desiredSpeed) {
                if ((int) loco.getSpeed() <= speedLimit) {
                    double rotation = loco.seats.get(0).getPassenger() == null?loco.rotationYaw:loco.seats.get(0).getPassenger().rotationYaw;
                    double[] motion = CommonUtil.rotatePoint(0.002,0,rotation==0?0:CommonUtil.floorDouble(rotation/90d)*90);
                    motion[1]= MathHelper.sqrt_double(motion[0]*motion[0]+motion[2]*motion[2]);
                    loco.appendMovement(motion[1]);
                }
            }
        }
    }

    public void slow(Integer desiredSpeed) {
        if (loco.getSpeed() >= desiredSpeed) {
            loco.multiplyVelocity(loco.brakingRate);
        }
    }

    public void stop(Vec3 signalPosition) {
        double currentDistance = Math.copySign(Vec3.createVectorHelper(loco.posX, loco.posY, loco.posZ).distanceTo(signalPosition), 1.0D);
        if (1.0D - currentDistance != 0.0D && currentDistance != 0.0D) {
            loco.multiplyVelocity(currentDistance / loco.getSpeed());
        } else {
            loco.multiplyVelocity(0.5);
        }
    }

    /*
     * =========================================== CONTROLS ===========================================
     **/

    public void toggleATO() {
        if (mtcStatus != 0 && mtcType == 2) {
            if (loco instanceof SteamTrain && !ConfigHandler.ALLOW_ATO_ON_STEAMERS) {
                ((EntityPlayer) loco.riddenByEntity).addChatMessage(new ChatComponentText("Automatic Train Operation cannot be used with steam trains"));
            } else {
                atoStatus = atoStatus == 1 ? 0 : 1;
            }
        } else {
            ((EntityPlayer) loco.riddenByEntity).addChatMessage(new ChatComponentText("Automatic Train Operation can only be activated when you are using W-MTC"));
        }
    }

    public void toggleMTCOverride() {
        if (mtcOverridePressed) {
            ((EntityPlayer) loco.riddenByEntity).addChatMessage(new ChatComponentText("MTC has been enabled and will re-activate when the system receives new data"));
            mtcOverridePressed = false;
        } else {
            ((EntityPlayer) loco.riddenByEntity).addChatMessage(new ChatComponentText("MTC has been disabled and will not receive speed changes or transmit MTC data"));
            mtcOverridePressed = true;
            mtcStatus = 0;
            speedLimit = 0;
            nextSpeedLimit = 0;
            xSpeedLimitChange = 0.0;
            ySpeedLimitChange = 0.0;
            zSpeedLimitChange = 0.0;
            xFromStopPoint = 0.0;
            yFromStopPoint = 0.0;
            zFromStopPoint = 0.0;
            trainLevel = "0";
            disconnectFromServer();
        }
    }

    public void toggleOverspeedOverride() {
        if (mtcStatus == 1 || mtcStatus == 2) {
            overspeedOveridePressed = !overspeedOveridePressed;
        }
    }

    /*
     * =========================================== NBT ===========================================
     **/

    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("serverUUID", serverUUID);
        nbttagcompound.setString("trainID", trainID);
        nbttagcompound.setString("trainLevel", trainLevel);

        nbttagcompound.setInteger("mtcStatus", mtcStatus);
        nbttagcompound.setInteger("mtcType", mtcType);
        nbttagcompound.setInteger("atoStatus", atoStatus);
        nbttagcompound.setDouble("xFromStop", xFromStopPoint);
        nbttagcompound.setDouble("yFromStop", yFromStopPoint);
        nbttagcompound.setDouble("zFromStop", zFromStopPoint);
        nbttagcompound.setDouble("xFromStationStop", xStationStop);
        nbttagcompound.setDouble("yFromStationStop", yStationStop);
        nbttagcompound.setDouble("zFromStationStop", zStationStop);
        nbttagcompound.setInteger("speedLimit", speedLimit);
        nbttagcompound.setInteger("nextSpeedLimit", nextSpeedLimit);
        nbttagcompound.setDouble("xSpeedChange", xSpeedLimitChange);
        nbttagcompound.setDouble("ySpeedChange", ySpeedLimitChange);
        nbttagcompound.setDouble("zSpeedChange", zSpeedLimitChange);
        nbttagcompound.setBoolean("mtcOverridePressed", mtcOverridePressed);
        nbttagcompound.setBoolean("overspeedOverridePressed", overspeedOveridePressed);
        nbttagcompound.setString("currentSignalBlock", currentSignalBlock);
        nbttagcompound.setBoolean("stationStop", stationStop);
        nbttagcompound.setString("destination", destination);
    }

    public void readEntityFromNBT(NBTTagCompound ntc) {
        serverUUID = ntc.getString("serverUUID");
        trainID = ntc.getString("trainID");
        trainLevel = ntc.getString("trainLevel");

        mtcStatus = ntc.getInteger("mtcStatus");
        mtcType = ntc.getInteger("mtcType");
        atoStatus = ntc.getInteger("atoStatus");
        xFromStopPoint = ntc.getDouble("xFromStop");
        yFromStopPoint = ntc.getDouble("yFromStop");
        zFromStopPoint = ntc.getDouble("zFromStop");
        xStationStop = ntc.getDouble("xFromStationStop");
        yStationStop = ntc.getDouble("yFromStationStop");
        zStationStop = ntc.getDouble("zFromStationStop");
        speedLimit = ntc.getInteger("speedLimit");
        nextSpeedLimit = ntc.getInteger("nextSpeedLimit");
        xSpeedLimitChange = ntc.getDouble("xSpeedChange");
        ySpeedLimitChange = ntc.getDouble("ySpeedChange");
        zSpeedLimitChange = ntc.getDouble("zSpeedChange");
        mtcOverridePressed = ntc.getBoolean("mtcOverridePressed");
        overspeedOveridePressed = ntc.getBoolean("overspeedOverridePressed");
        currentSignalBlock = ntc.getString("currentSignalBlock");
        stationStop = ntc.getBoolean("stationStop");
        destination = ntc.getString("destination");
    }

    /*
     * =========================================== MESSAGING ===========================================
     **/

    @Override
    public void receiveMessage(PDMMessage message) {
        JsonParser parser = new JsonParser();

        JsonObject thing = parser.parse(PDMMessage.message.toString()).getAsJsonObject();

        if (message != null) {
            if (thing.get("funct").getAsString().equals("startlevel2")) {
                //That's actually really great, now let's get where it sent from owo
                //	System.out.println("Connected!");
                serverUUID = PDMMessage.UUIDFrom;
                mtcType = 2;
                mtcStatus = thing.get("mtcStatus").getAsInt();
                Traincraft.mscChannel.sendToAllAround(new PacketMTC(loco.getEntityId(), mtcStatus, 2), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                speedLimit = thing.get("speedLimit").getAsInt();
                nextSpeedLimit = thing.get("nextSpeedLimit").getAsInt();
                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, 0, 0, 0, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                if (nextSpeedLimit != 0) {
                    xSpeedLimitChange = thing.get("nextSpeedLimitChangeX").getAsDouble();
                    ySpeedLimitChange = thing.get("nextSpeedLimitChangeY").getAsDouble();
                    zSpeedLimitChange = thing.get("nextSpeedLimitChangeZ").getAsDouble();
                }

            } else if (thing.get("funct").getAsString().equals("response")) {
                mtcType = 2;
                mtcStatus = thing.get("mtcStatus").getAsInt();
                Traincraft.mscChannel.sendToAllAround(new PacketMTC(loco.getEntityId(), mtcStatus, 2), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                nextSpeedLimit = thing.get("nextSpeedLimit").getAsInt();
                if (!speedGoingDown && xFromStopPoint == 0.0) {
                    speedLimit = thing.get("speedLimit").getAsInt();
                    Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, 0, 0, 0, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                }

                if (thing.get("speedChange").getAsBoolean()) {
                    xSpeedLimitChange = thing.get("nextSpeedLimitChangeX").getAsDouble();
                    ySpeedLimitChange = thing.get("nextSpeedLimitChangeY").getAsDouble();
                    zSpeedLimitChange = thing.get("nextSpeedLimitChangeZ").getAsDouble();
                    Traincraft.itnsChannel.sendToAllAround(new PacketNextSpeed(nextSpeedLimit, 0, 0, 0, xSpeedLimitChange, ySpeedLimitChange, zSpeedLimitChange, loco.getEntityId()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                }

                if (thing.get("endSoon").getAsBoolean()) {
                    if (!(stationStop)) {
                        xFromStopPoint = thing.get("xStopPoint").getAsDouble();
                        yFromStopPoint = thing.get("yStopPoint").getAsDouble();
                        zFromStopPoint = thing.get("zStopPoint").getAsDouble();
                        Traincraft.atoSetStopPoint.sendToAllAround(new PacketATOSetStopPoint(loco.getEntityId(), xFromStopPoint, yFromStopPoint, zFromStopPoint, xStationStop, yStationStop, zStationStop), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                    }
                }

                if (thing.get("stationStopSoon").getAsBoolean() && !stationStop) {
                    xStationStop = thing.get("xStationStop").getAsDouble();
                    yStationStop = thing.get("yStationStop").getAsDouble();
                    zStationStop = thing.get("zStationStop").getAsDouble();

                    Traincraft.atoSetStopPoint.sendToAllAround(new PacketATOSetStopPoint(loco.getEntityId(), xFromStopPoint, yFromStopPoint, zFromStopPoint, xStationStop, yStationStop, zStationStop), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                }

                if (thing.get("atoStatus") != null) {
                    atoStatus = thing.get("atoStatus").getAsInt();
                    Traincraft.atoChannel.sendToAllAround(new PacketATO(loco.getEntityId(), thing.get("atoStatus").getAsInt()), new NetworkRegistry.TargetPoint(loco.getWorld().provider.dimensionId, loco.posX, loco.posY, loco.posZ, 150.0D));
                }
            }
        }
    }

    @Override
    public void sendMessage(PDMMessage message) {
        //AxisAlignedBB targetBox = AxisAlignedBB.getBoundingBox(loco.posX, loco.posY, loco.posZ, loco.posX + 2000, loco.posY + 2000, loco.posZ + 2000);
        List<TileEntity> allTEs = loco.getWorld().loadedTileEntityList;
        for (TileEntity te : allTEs) {

            if (te instanceof TilePDMInstructionRadio) {
                TilePDMInstructionRadio teP = (TilePDMInstructionRadio) te;

                if (teP.uniqueID.equals(PDMMessage.UUIDTo)) {
                    teP.receiveMessage(message);
                }
            }
        }
    }

    public void attemptConnection(String theServerUUID) {
        if (!serverUUID.isEmpty()) {
            //Oh, that's great! We just got the servers UUID. Now let's try connecting to it.
            if (theServerUUID != null && !serverUUID.equals(theServerUUID) && !loco.canBePushed()) {
                JsonObject sendTo = new JsonObject();
                sendTo.addProperty("funct", "attemptconnection");
                sendTo.addProperty("trainType", trainLevel);
                sendMessage(new PDMMessage(trainID, theServerUUID, sendTo.toString(), 0));
            }
        }
    }

    public void disconnectFromServer() {
        if (Loader.isModLoaded("ComputerCraft") || Loader.isModLoaded("OpenComputers")) {
            JsonObject sendTo = new JsonObject();
            sendTo.addProperty("funct", "disconnect");
            sendMessage(new PDMMessage(trainID, serverUUID, sendTo.toString(), 0));
            mtcType = 1;
            serverUUID = "";
        }
    }

    /*
     * =========================================== RC IROUTABLECART ===========================================
     **/

    @Override
    public String getDestination() {
        if (destination == null) return "";
        return destination;
    }

    @Override
    public boolean setDestination(ItemStack ticket) {
        if (ticket != null) {
            destination = MTC.getTicketDestination(ticket);
            return true;
        }
        return false;
    }

    @Override
    public GameProfile getOwner() {
        return CartTools.getCartOwner(loco);
    }

    public static String getTicketDestination(ItemStack ticket) {
        if (ticket == null) {
            return "";
        }
        NBTTagCompound nbt = ticket.getTagCompound();
        if (nbt == null) {
            return "";
        }
        return nbt.getString("dest");
    }
}