package train.common.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import ebf.tim.entities.EntitySeat;
import ebf.tim.utility.CommonUtil;
import io.netty.buffer.ByteBuf;
import mods.railcraft.api.carts.CartTools;
import mods.railcraft.api.tracks.RailTools;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.adminbook.ServerLogger;
import train.common.core.handlers.ConfigHandler;
import train.common.core.network.PacketParkingBrake;
import train.common.core.network.PacketSlotsFilled;
import train.common.enums.DataMemberName;
import train.common.library.GuiIDs;
import train.common.mtc.PDMMessage;
import train.common.mtc.TilePDMInstructionRadio;
import train.common.mtc.packets.*;

import java.util.List;
import java.util.Random;

public abstract class Locomotive extends Freight implements WirelessTransmitter, IRollingStockLightControls  {

    // --- CONTROLS ---
    public boolean forwardPressed = false;
    public boolean backwardPressed = false;
    public boolean brakePressed = false;

    // --- LOCO STATE ---
    public boolean isLocoTurnedOn = false;
    public boolean parkingBrake = false;
    private boolean canBePulled = false;
    private boolean hasDrowned = false;
    protected boolean canCheckInvent = true;
    public int speedLimit = 0;

    private String lastRider = "";
    private Entity lastEntityRider;

    private int whistleDelay = 0;
    private int blowUpDelay = 0;
    private int heatBrakeDelay = 0;

    // --- DEBUFFS ---
    public double currentMassPulled = 0;
    public double currentSpeedSlowDown = 0;
    public double currentAccelSlowDown = 0;
    public double currentBrakeSlowDown = 0;
    public double currentFuelConsumptionChange = 0;

    public double accelRate = 0.7D;
    public double brakingRate = 0.96D;
    public double fuelRate;

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

    // --- SERVER ---
    public String serverUUID = "";
    public String trainID;
    public boolean isConnected = false;
    public String trainLevel = "1";

    // --- LIGHTING ---
    private boolean isLightsEnabled = false;
    private boolean isBeaconEnabled = false;
    private byte ditchLightMode = 0;
    private byte beaconCycleIndex = 0;

    // --- AUDIO ---
    private int soundPosition = 0;
    public TrainSound soundRunning;
    public TrainSound soundIdle;
    public TrainSound soundHorn;
    public TrainSound soundBell;

    /*
     * =========================================== INIT ===========================================
     **/

    public Locomotive(World world) {
        super(world);
        if(world==null){return;}

        // --- DEFAULTS ---
        setDefaultMass(0);
        accelRate = getSpecAccel();
        brakingRate = getSpecBrake();
        fuelRate = getSpecFuelConsumption();
        fuelTrain = 0;
        if (this instanceof SteamTrain) isLocoTurnedOn = true;

        initDataWatcher();

        entityCollisionReduction = 0.99F;

        // --- UPDATE LINKS ---
        for(AbstractTrains t: consist){
            if(t.consistLeadID!=getEntityId()){
                updateLinks();
            }
        }

        // --- TRAIN ID ---
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder sb = new StringBuilder(5);
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        trainID = output;

        // --- SERVER ---
        if (serverUUID != "") {
            attemptConnection(serverUUID);
        }
    }

    public Locomotive(World world, double d, double d1, double d2) {
        super(world, d, d1, d2);
        fuelTrain = 0;
    }

    // Additional spawn data to check for
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        super.readSpawnData(additionalData);
        isLocoTurnedOn = additionalData.readBoolean();
        parkingBrake = additionalData.readBoolean();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(isLocoTurnedOn);
        buffer.writeBoolean(parkingBrake);
    }

    /*
     * =========================================== UPDATE ===========================================
     **/

    @Override
    public void onUpdate() {
        cycleBeaconIndex();
        if (!getWorld().isRemote) {
            if(consistLeadID!=getEntityId()){
                updateLinks();
            }
            if (ticksExisted % 10 == 0) {
                updateDebuffs(); }
            if (ticksExisted % 200 == 0) {
                updateFillStatus();
            }
            if (ticksExisted % 100 == 0) {
                updateFuel();
            }
        }
        if (ticksExisted % 600 == 0 && riddenByEntity instanceof EntityPlayer) {
            lastRider = ((EntityPlayer) riddenByEntity).getDisplayName();
            lastEntityRider = (riddenByEntity);
        }

        updateWhistle();
        updateVelocity();
        updateDestinationAndOwner();
        if (!getWorld().isRemote) {
            updateMTCandATO();
        }

        super.onUpdate();
        updateHeat();
        if (!getWorld().isRemote) {
            updateDataWatcher();
            if (ticksExisted % 4 == 0) {
                updateDrowning();
            }
        }
    }

    private void cycleBeaconIndex()  {
        if (isBeaconEnabled && ticksExisted % 5 == 0)  {
            beaconCycleIndex++;
            if (beaconCycleIndex == 4) {
                beaconCycleIndex = 0;
            }
        }
    }

    public void updateDebuffs() {
        currentMassPulled = pullingWeight * 0.07457;
        double totalMhp = getSpecMHP() > 0 ? getSpecMHP() : 100;    // Guarantee non-zero so we don't divide by zero

        // Append passive locos Mhp
        for (AbstractTrains stock : consist) {
            if (stock instanceof Locomotive && stock.uniqueID != uniqueID) {
                totalMhp += ((Locomotive)stock).getSpecMHP();
            }
        }

        // Mhp debuffs
        currentSpeedSlowDown = currentMassPulled / totalMhp * 74.57;
        currentBrakeSlowDown = Math.pow(currentMassPulled,2) / totalMhp * 0.7457 * 0.8;
        currentAccelSlowDown = currentBrakeSlowDown * 1.13;
        currentFuelConsumptionChange = currentBrakeSlowDown * 100; // *100 because fuel is checked every 100 ticks

        // Heat debuffs
        double speedMult = 1;
        double accelMult = 1;
        if (getState().equals("cold")) {
            speedMult *= 0.6;
        }
        else if (getState().equals("warm")) {
            speedMult *= 0.7;
        }
        else if (getState().equals("too hot")) {
            accelMult *= 0.2;
            getWorld().spawnParticle("largesmoke", posX, posY + 0.3, posZ, 0.0D, 0.0D, 0.0D);
        }

        // Get the defaults, then scale them
        setCurrentMaxSpeed((int)Math.max(       (getSpecMaxSpeed()           - currentSpeedSlowDown) * speedMult,0));        // Avoid Speed < 0
        brakingRate = Math.min(                 getSpecBrake()               + currentBrakeSlowDown,0.998);                 // Avoid Brake > 1 (acceleration)
        accelRate = Math.max(                   (getSpecAccel()              - currentAccelSlowDown) * accelMult,0);        // Avoid Accel < 0 (braking)
        fuelRate =                              getSpecFuelConsumption()     - currentFuelConsumptionChange;
    }

    /**
     * Can't use datawatcher here. Locomotives use them all already
     * Check inventory The packet never arrives if it is sent when the
     * entity reads its NBT (player hasn't been initialised probably)
     */
    public void updateFillStatus() {
        slotsFilled = 0;
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack itemstack = getStackInSlot(i);
            if (itemstack != null) {
                slotsFilled++;
            }
        }

        Traincraft.slotschannel.sendToAllAround(new PacketSlotsFilled(this, slotsFilled), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
    }

    public void recieveSlotsFilled(int amount) {
        slotsFilled = amount;
    }

    protected void updateFuel() {
        if (isLocoTurnedOn) {
            fuelTrain -= (int)fuelRate;

            if (fuelTrain < 0) {
                fuelTrain = 0;
            }
        }
    }

    public void updateWhistle() {
        if (whistleDelay > 0) {
            whistleDelay--;
        }
        if (ConfigHandler.SOUNDS && whistleDelay == 0) {
            if(soundIdle==null){
                soundIdle=getIdleSound();
            }
            if (soundIdle != null && !soundIdle.addr.isEmpty()) {
                if (getFuel() > 0 && isLocoTurnedOn) {
                    double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);
                    if (speed > -0.001D && speed < 0.01D && soundPosition == 0) {
                        getWorld().playSoundAtEntity(this, soundIdle.addr, soundIdle.vol, soundIdle.pit);
                        soundPosition = soundIdle.len;
                    }

                    if(soundRunning==null){
                        soundRunning=getRunningSound();
                    }
                    if (soundRunning!=null && soundRunning.runningPitch && !soundRunning.addr.isEmpty() && whistleDelay == 0) {
                        if (speed > 0.01D && speed < 0.06D && soundPosition == 0) {
                            getWorld().playSoundAtEntity(this, soundRunning.addr, soundRunning.vol, soundRunning.pit-0.3f);
                            soundPosition = soundRunning.len;
                        } else if (speed > 0.06D && speed < 0.2D && soundPosition == 0) {
                            getWorld().playSoundAtEntity(this, soundRunning.addr, soundRunning.vol, soundRunning.pit-0.1f);
                            soundPosition = soundRunning.len / 2;
                        } else if (speed > 0.2D && soundPosition == 0) {
                            getWorld().playSoundAtEntity(this, soundRunning.addr, soundRunning.vol, soundRunning.pit);
                            soundPosition = soundRunning.len / 3;
                        }
                    } else {
                        if (speed > 0.01D && soundPosition == 0) {
                            getWorld().playSoundAtEntity(this, soundRunning.addr, soundRunning.vol, soundRunning.pit);
                            soundPosition = soundRunning.len;
                        }
                    }

                    if (soundPosition > 0) {
                        soundPosition--;
                    }
                }
            }
        }
    }

    public void updateVelocity() {

        double velocityMult = 1;

        // --- WORKING ---
        if (!(getState().equals("broken") || hasDrowned)) {
            // Parking brakingRate
            if (!getWorld().isRemote && getParkingBrakeFromPacket()) {
                velocityMult *= 0;
            }
            // Spacebar brakingRate
            if (brakePressed) {
                velocityMult *= brakingRate;
            }
            // Lockdown track
            for (AbstractTrains train : consist) {
                if (train != null) {
                    if (RailTools.isCartLockedDown(train)) {
                        velocityMult *= 0;
                        break;
                    }
                }
            }
            // Acceleration (only allow if on, fueled and nothing else is slowing down)
            if (isLocoTurnedOn & getFuel() > 0) {
                if (velocityMult == 1 && (forwardPressed || backwardPressed)) {
                    appendMovement(0.0015 * accelRate * ((forwardPressed ? -1 : 1) + (backwardPressed ? 1 : -1)));
                }
            }
            else {
                velocityMult *= 0.97;
            }
        }
        // --- BROKEN ---
        else {
            velocityMult *= 0.97;
            setFire(8);
            getWorld().spawnParticle("largesmoke", posX, posY + 0.3, posZ, 0.0D, 0.0D, 0.0D);
            getWorld().spawnParticle("largesmoke", posX, posY + 0.3, posZ, 0.0D, 0.0D, 0.0D);
            blowUpDelay++;
            if (hasDrowned && blowUpDelay > 20) {
                attackEntityFrom(DamageSource.drown, 100);
            }
            else if (blowUpDelay > 80) {
                if (!getWorld().isRemote) {
                    getWorld().createExplosion(this, posX, posY, posZ, 0.5F, false);
                    setDead();
                    if (FMLCommonHandler.instance().getMinecraftServerInstance() != null && lastEntityRider instanceof EntityPlayer) {
                        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText(((EntityPlayer) lastEntityRider).getDisplayName() + " blew " + getTrainOwner() + "'s locomotive"));
                    }
                }
            }
        }

        multiplyVelocity(velocityMult);
    }

    public void updateDestinationAndOwner() {
        for (AbstractTrains train : consist) {
            if (train != null) {
                // Getting main locomotive of the train and copying its destination to all attached carts
                if (!getDestination().isEmpty()) {
                    if (train != this) { train.destination = getDestination(); }
                    CartTools.setCartOwner(train, CartTools.getCartOwner(this));
                }
            }
        }
    }

    public void updateDrowning() {
        if (getWorld().handleMaterialAcceleration(boundingBox.expand(0.0D, -0.2000000059604645D, 0.0D).contract(0.001D, 0.001D, 0.001D), Material.water, this)) {
            hasDrowned = true;
            canCheckInvent = false;
            fuelTrain = 0;
            if (FMLCommonHandler.instance().getMinecraftServerInstance() != null && lastEntityRider instanceof EntityPlayer) {
                FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText(((EntityPlayer) lastEntityRider).getDisplayName() + " drowned " + getTrainOwner() + "'s locomotive"));
            }
        }
    }

    public void updateHeat() {
        float heat = getHeat();
        int heatChange = 0;

        if (canOverheat()) {
            double speed = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);

            // speed is low, overheat goes down to normal
            if(!getState().equals("broken")) {
                if ((speed <= 0.10) && !isBraking && heat > 0.5 && (getWorld().rand.nextInt(10) == 0)) {
                    heatChange--;
                    if (speed <= 0.05) {
                        heatChange--;
                    }
                }
                // fuel is empty, heat level goes down
                if (fuelTrain < 1 && heat > 0 && (getWorld().rand.nextInt(10) == 0)) {
                    heatChange--;
                }
                // Heat goes down with time
                if (heatChange > 0.5 && (getWorld().rand.nextInt(30) == 0)) {
                    heatChange--;
                }
            }
            // train is fueled => heat level goes up to normal
            if (isLocoTurnedOn && fuelTrain > 1 && (heat < 0.5) && (getWorld().rand.nextInt(7) == 0)) {
                heatChange++;
            }

            // train is braking, increment a delayer break won't overheat too quickly
            if (isBraking) { heatBrakeDelay++; }
            else { heatBrakeDelay=0; }

            // Delayer has reached max and speed is not 0: overheat
            if (isBraking && heatBrakeDelay > 40 && speed > 0.05) {
                if (getWorld().rand.nextInt(10) == 0) {
                    heatChange += 2;
                }
            }

            if (this instanceof SteamTrain) {
                int waterLevel = ((SteamTrain) this).getWater();
                // water is empty => overheats
                if ((waterLevel < 1) && fuelTrain > 10) {
                    if (getWorld().rand.nextInt(10) == 0) {
                        heatChange += 3;
                    }
                }
                int maxWaterLevel = ((SteamTrain) this).getCartTankCapacity();
                if ((waterLevel > maxWaterLevel - (maxWaterLevel / 2)) && heat > 0.5 && !getState().equals("broken")) {
                    heatChange--;
                }
            }
            if (heat >= 1)          { setState("broken"); }
            else if (heat >= 0.875) { setState("too hot"); }
            else if (heat >= 0.75)  { setState("very hot"); }
            else if (heat >= 0.5)   { setState("hot"); }
            else if (heat >= 0.25)  { setState("warm"); }
            else                    { setState("cold"); }

            setHeat(Math.min(heat+((float)heatChange/200),1));
        }
    }

    /*
     * =========================================== UTILS ===========================================
     **/

    public float getSpecMaxSpeed() {        return getSpec() == null ? 50   : getSpec().getMaxSpeed(); }
    public float getSpecMHP() {             return getSpec() == null ? 100  : getSpec().getMHP(); }
    public double getSpecAccel() {          return getSpec() == null ? 0.4  : getSpec().getAccelerationRate(); }
    public double getSpecBrake() {          return getSpec() == null ? 0.97 : getSpec().getBrakeRate(); }
    public int getSpecFuelConsumption() {   return getSpec() == null ? 80   : getSpec().getFuelConsumption(); }

    @Override
    public boolean canBePushed() { return canBePulled; }
    public void setCanBePushed(boolean pushable) { canBePulled = pushable; }


    public boolean canOverheat() { return true; }
    public int getFuelDiv(int i) { return getFuel() * i / 1200; }

    @Override
    public int getMinecartType() { return 2; }

    public boolean isNotOwner() {
        if (riddenByEntity instanceof EntityPlayer && !((EntityPlayer) riddenByEntity).getDisplayName().equalsIgnoreCase(getTrainOwner())) {
            return true;
        }
        if (!seats.isEmpty() && seats.get(0).getPassenger() instanceof EntityPlayer && !((EntityPlayer) seats.get(0).getPassenger()).getDisplayName().equalsIgnoreCase(getTrainOwner())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        if (getWorld().isRemote) { return true; }

        if (canBeDestroyedByPlayer(damagesource)) { return true; }

        super.attackEntityFrom(damagesource, i);
        setRollingDirection(-getRollingDirection());
        setRollingAmplitude(10);
        setBeenAttacked();
        setDamage(getDamage() + i * 10);
        if (getDamage() > 40) {
            if (riddenByEntity != null) {
                riddenByEntity.mountEntity(this);
            }

            setDead();
            disconnectFromServer();
            ServerLogger.deleteWagon(this);

            if (damagesource.getEntity() instanceof EntityPlayer) {
                dropCartAsItem(((EntityPlayer) damagesource.getEntity()).capabilities.isCreativeMode);
            } else {
                dropCartAsItem(false);
            }
        }
        return true;
    }

    /*
     * =========================================== DATA WATCHER & PACKETS ===========================================
     **/

    public void initDataWatcher() {
        dataWatcher.addObject(2, (int) getSpecMaxSpeed());
        dataWatcher.addObject(3, destination);
        dataWatcher.addObject(20, 0f); // Heat
        dataWatcher.addObject(23, ""); // State
        dataWatcher.addObject(24, fuelTrain);
        dataWatcher.addObject(25, 0); // Speed
        dataWatcher.addObject(26, guiDetailsJSON());
        dataWatcher.addObject(28, lightingDetailsJSONString());
    }

    public void updateDataWatcher() {
        dataWatcher.updateObject(3, destination);
        // 15 is unused
        dataWatcher.updateObject(24, fuelTrain);
        dataWatcher.updateObject(25, (int)Math.round(SpeedHandler.convertSpeedInv(Math.sqrt(bogieBack.velocity[0] * bogieBack.velocity[0] + bogieBack.velocity[1] * bogieBack.velocity[1]))));
        dataWatcher.updateObject(26, guiDetailsJSON());
        dataWatcher.updateObject(28, lightingDetailsJSONString());
    }

    // 02 Max speed
    public int getCurrentMaxSpeed() {        return dataWatcher.getWatchableObjectInt(2); }
    public void setCurrentMaxSpeed(int maxSpeed) {
        if (!getWorld().isRemote) {             dataWatcher.updateObject(2, maxSpeed); }
    }

    // 03 Destination
    public String getDestinationGUI() {
        if (getWorld().isRemote) {              return dataWatcher.getWatchableObjectString(3); }
                                                return destination;
    }

    // 20 Heat
    public float getHeat() {                    return dataWatcher.getWatchableObjectFloat(20); }
    public void setHeat(float heat) {           dataWatcher.updateObject(20, heat); }

    // 23 State
    public String getState() {                  return dataWatcher.getWatchableObjectString(23); }
    public void setState(String state) {        dataWatcher.updateObject(23, state); }

    // 24 Fuel
    public int getFuel() {
        if (getWorld().isRemote) {              return dataWatcher.getWatchableObjectInt(24); }
                                                return fuelTrain;
    }

    // 25 Speed
    public double getSpeed() {                  return dataWatcher.getWatchableObjectInt(25); }

    // 26 GUI details
    public String guiDetailsDW() {              return dataWatcher.getWatchableObjectString(26); }
    public String guiDetailsJSON() {
        JsonObject gui = new JsonObject();
        gui.addProperty("cartsPulled", consist.size()-1);
        gui.addProperty("massPulled", currentMassPulled);
        gui.addProperty("slowDown", Math.round(currentSpeedSlowDown));
        gui.addProperty("accelSlowDown", (double)Math.round(currentAccelSlowDown*1000)/1000);
        gui.addProperty("brakeSlowDown", (double)Math.round(currentBrakeSlowDown*1000)/1000);
        gui.addProperty("fuelUseChange", (double)Math.round(currentFuelConsumptionChange*1000)/1000);
        return gui.toString();
    }

    // 28 Lighting details
    public String getLightingDetails() {        return dataWatcher.getWatchableObjectString(28); }
    public boolean isLightsEnabled() {          return AsJsonObject(dataWatcher.getWatchableObjectString(28)).get(DataMemberName.isLightsEnabled.AsString()).getAsBoolean(); }
    public void setPacketLights(boolean isLocoLightsOn) { isLightsEnabled = isLocoLightsOn; }
    public boolean isBeaconEnabled() {          return AsJsonObject(dataWatcher.getWatchableObjectString(28)).get(DataMemberName.isBeaconEnabled.AsString()).getAsBoolean(); }
    public void setPacketBeacon(boolean isLocoBeaconEnabled) { isBeaconEnabled = isLocoBeaconEnabled; }
    public byte getBeaconCycleIndex() {         return AsJsonObject(dataWatcher.getWatchableObjectString(28)).get(DataMemberName.beaconCycleIndex.AsString()).getAsByte(); }
    public boolean isDitchLightsEnabled() {     return AsJsonObject(dataWatcher.getWatchableObjectString(28)).get(DataMemberName.ditchLightMode.AsString()).getAsByte() > 0; }
    public void setPacketDitchLightsMode(byte ditchLightMode) { this.ditchLightMode = ditchLightMode; }

    public String lightingDetailsJSONString()  {
        JsonObject lightingDetailsJSONString = new JsonObject();
        lightingDetailsJSONString.addProperty(DataMemberName.isLightsEnabled.AsString(), isLightsEnabled);
        lightingDetailsJSONString.addProperty(DataMemberName.isBeaconEnabled.AsString(), isBeaconEnabled);
        lightingDetailsJSONString.addProperty(DataMemberName.beaconCycleIndex.AsString(), beaconCycleIndex);
        lightingDetailsJSONString.addProperty(DataMemberName.ditchLightMode.AsString(), ditchLightMode);
        return lightingDetailsJSONString.toString();
    }

    public JsonObject lightingDetailsAsJSON()  {
        JsonObject lightingDetailsJSONString = new JsonObject();
        lightingDetailsJSONString.addProperty(DataMemberName.isLightsEnabled.AsString(), isLightsEnabled);
        lightingDetailsJSONString.addProperty(DataMemberName.isBeaconEnabled.AsString(), isBeaconEnabled);
        lightingDetailsJSONString.addProperty(DataMemberName.beaconCycleIndex.AsString(), beaconCycleIndex);
        lightingDetailsJSONString.addProperty(DataMemberName.ditchLightMode.AsString(), ditchLightMode);
        return lightingDetailsJSONString;
    }

    // Parking brakingRate
    public boolean getParkingBrakeFromPacket() {            return parkingBrake; }
    public void setParkingBrakeFromPacket(boolean set) {    parkingBrake = set; }

    // Loco on
    public void setLocoTurnedOnFromPacket(boolean set) {    isLocoTurnedOn = set; }

    private JsonObject AsJsonObject(String string) {
        return new JsonParser().parse(string).getAsJsonObject();
    }

    /*
     * =========================================== NBT ===========================================
     **/

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);

        // --- LOCO STATE ---
        nbttagcompound.setBoolean("isLocoTurnedOn", isLocoTurnedOn);
        nbttagcompound.setBoolean("parkingBrake", parkingBrake);
        nbttagcompound.setBoolean("canBePulled", canBePulled);
        nbttagcompound.setFloat("heat", getHeat());
        nbttagcompound.setInteger("speedLimit", speedLimit);

        nbttagcompound.setString("lastRider", lastRider);

        nbttagcompound.setInteger("fuelTrain", fuelTrain);

        // --- MTC & ATO ---
        nbttagcompound.setInteger("mtcStatus", mtcStatus);
        nbttagcompound.setInteger("mtcType", mtcType);
        nbttagcompound.setInteger("atoStatus", atoStatus);
        nbttagcompound.setDouble("xFromStop", xFromStopPoint);
        nbttagcompound.setDouble("yFromStop", yFromStopPoint);
        nbttagcompound.setDouble("zFromStop", zFromStopPoint);
        nbttagcompound.setDouble("xFromStationStop", xStationStop);
        nbttagcompound.setDouble("yFromStationStop", yStationStop);
        nbttagcompound.setDouble("zFromStationStop", zStationStop);
        nbttagcompound.setInteger("nextSpeedLimit", nextSpeedLimit);
        nbttagcompound.setDouble("xSpeedChange", xSpeedLimitChange);
        nbttagcompound.setDouble("ySpeedChange", ySpeedLimitChange);
        nbttagcompound.setDouble("zSpeedChange", zSpeedLimitChange);
        nbttagcompound.setBoolean("mtcOverridePressed", mtcOverridePressed);
        nbttagcompound.setBoolean("overspeedOverridePressed", overspeedOveridePressed);
        nbttagcompound.setString("currentSignalBlock", currentSignalBlock);
        nbttagcompound.setBoolean("stationStop", stationStop);

        nbttagcompound.setString("destination", destination);

        // --- SERVER ---
        nbttagcompound.setString("serverUUID", serverUUID);
        nbttagcompound.setString("trainID", trainID);
        nbttagcompound.setBoolean("isConnected", isConnected);
        nbttagcompound.setString("trainLevel", trainLevel);

        // --- LIGHTING ---
        nbttagcompound.setString(DataMemberName.lightingDetailsJSONString.AsString(), lightingDetailsJSONString());

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound ntc) {
        super.readEntityFromNBT(ntc);

        // --- LOCO STATE ---
        isLocoTurnedOn = ntc.getBoolean("isLocoTurnedOn");
        parkingBrake = ntc.getBoolean("parkingBrake");
        canBePulled = ntc.getBoolean("canBePulled");
        setHeat(ntc.getFloat("heat"));
        speedLimit = ntc.getInteger("speedLimit");

        lastRider = ntc.getString("lastRider");

        fuelTrain = ntc.getInteger("fuelTrain");

        // --- MTC & ATO ---
        mtcStatus = ntc.getInteger("mtcStatus");
        mtcType = ntc.getInteger("mtcType");
        atoStatus = ntc.getInteger("atoStatus");
        xFromStopPoint = ntc.getDouble("xFromStop");
        yFromStopPoint = ntc.getDouble("yFromStop");
        zFromStopPoint = ntc.getDouble("zFromStop");
        xStationStop = ntc.getDouble("xFromStationStop");
        yStationStop = ntc.getDouble("yFromStationStop");
        zStationStop = ntc.getDouble("zFromStationStop");
        nextSpeedLimit = ntc.getInteger("nextSpeedLimit");
        xSpeedLimitChange = ntc.getDouble("xSpeedChange");
        ySpeedLimitChange = ntc.getDouble("ySpeedChange");
        zSpeedLimitChange = ntc.getDouble("zSpeedChange");
        mtcOverridePressed = ntc.getBoolean("mtcOverridePressed");
        overspeedOveridePressed = ntc.getBoolean("overspeedOverridePressed");
        currentSignalBlock = ntc.getString("currentSignalBlock");
        stationStop = ntc.getBoolean("stationStop");

        destination = ntc.getString("destination");

        // --- SERVER ---
        serverUUID = ntc.getString("serverUUID");
        trainID = ntc.getString("trainID");
        isConnected = ntc.getBoolean("isConnected");
        trainLevel = ntc.getString("trainLevel");

        // --- LIGHTING ---
        JsonObject lightingDetailsJSONStringObject;
        try { lightingDetailsJSONStringObject = new JsonParser().parse(ntc.getString(DataMemberName.lightingDetailsJSONString.AsString())).getAsJsonObject(); }
        catch (Exception e)  { lightingDetailsJSONStringObject = lightingDetailsAsJSON(); }
        isLightsEnabled = lightingDetailsJSONStringObject.get(DataMemberName.isLightsEnabled.AsString()).getAsBoolean();
        isBeaconEnabled = lightingDetailsJSONStringObject.get(DataMemberName.isBeaconEnabled.AsString()).getAsBoolean();
        ditchLightMode = lightingDetailsJSONStringObject.get(DataMemberName.ditchLightMode.AsString()).getAsByte();
        beaconCycleIndex = lightingDetailsJSONStringObject.get(DataMemberName.beaconCycleIndex.AsString()).getAsByte();
    }

    /*
     * =========================================== GUI PACKETS ===========================================
     **/

    // Gets packet from server and distribute for GUI handles motion
    @Override
    public void keyHandlerFromPacket(int i, int player) {
        if (getTrainLockedFromPacket()) {
            if (isLockedAndNotOwner(player)) {
                return;
            }
        }
        pressKey(i);

        if (i == 8 && ConfigHandler.SOUNDS) {
            soundHorn();
        }

        if (i == 10) {
            soundWhistle();
        }

        if (i == 4) {
            forwardPressed = true;
        }

        if (i == 5) {
            backwardPressed = true;
        }

        if (i == 7) {
            if (seats != null && !seats.isEmpty()) {
                for(EntitySeat seat: seats) {
                    if(seat.isControlSeat() && seat.getPassenger() != null && playerEntity == seat.getPassenger() && playerEntity.ridingEntity == seat) {
                        ((EntityPlayer) seat.getPassenger()).openGui(Traincraft.instance, GuiIDs.LOCO, getWorld(), (int) posX, (int) posY, (int) posZ);
                        break;
                    } else if (seat.getPassenger() != null && seat.getPassenger() instanceof EntityPlayer) {
                        Traincraft.proxy.seatGUI((EntityPlayer) seat.getPassenger(),this);
                        break;
                    }
                }
            }
        }

        if (i == 12) {
            brakePressed = true;
        }

        if (i == 13) {
            forwardPressed = false;
        }

        if (i == 14) {
            backwardPressed = false;
        }

        if (i == 15) {
            brakePressed = false;
        }

        if (i == 16) {
            if (mtcStatus != 0 && mtcType == 2) {
                if (!(this instanceof SteamTrain && !ConfigHandler.ALLOW_ATO_ON_STEAMERS)) {
                    if (atoStatus == 1) {
                        atoStatus = 0;
                    } else {
                        atoStatus = 1;
                    }
                }
            }
        }

        if (i == 17) {
            if (mtcOverridePressed) {
                mtcOverridePressed = false;
            } else {
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

        if (i == 18) {
            if (mtcStatus != 0) {
                overspeedOveridePressed = !overspeedOveridePressed;
            }
        }
    }

    public void soundHorn() {
        if(soundHorn==null){
            soundHorn=getHorn();
        }
        if (soundHorn != null && !soundHorn.addr.isEmpty() && whistleDelay == 0) {
            getWorld().playSoundAtEntity(this, soundHorn.addr, soundHorn.vol, soundHorn.pit);
            whistleDelay = 65;
        }

        List<?> entities = getWorld().getEntitiesWithinAABB(EntityAnimal.class, AxisAlignedBB.getBoundingBox(
                posX - 20, posY - 5, posZ - 20,
                posX + 20, posY + 5, posZ + 20));

        for (Object e : entities) {
            if (e instanceof EntityAnimal) {
                ((EntityAnimal) e).setTarget(this);
                ((EntityAnimal) e).getNavigator().setPath(null, 0);
            }
        }
    }

    public void soundWhistle() {
        if(soundBell==null){
            soundBell=getBell();
        }
        if (soundBell != null && !soundBell.addr.isEmpty() && whistleDelay == 0) {
            getWorld().playSoundAtEntity(this, soundBell.addr, soundBell.vol, soundBell.pit);
            whistleDelay = 65;
        }
    }

    /*
     * =========================================== MESSAGING ===========================================
     **/

    @Override
    public void markDirty() {
        super.markDirty();
        if (!getWorld().isRemote) {
            Traincraft.slotschannel.sendToAllAround(new PacketSlotsFilled(this, slotsFilled), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
        }
    }

    @Override
    public void receiveMessage(PDMMessage message) {
        JsonParser parser = new JsonParser();

        JsonObject thing = parser.parse(PDMMessage.message.toString()).getAsJsonObject();
        //System.out.println("Got one!");

        if (message != null) {
            if (thing.get("funct").getAsString().equals("startlevel2")) {
                //That's actually really great, now let's get where it sent from owo
                //	System.out.println("Connected!");
                serverUUID = PDMMessage.UUIDFrom;
                mtcType = 2;
                mtcStatus = thing.get("mtcStatus").getAsInt();
                isConnected = true;
                Traincraft.mscChannel.sendToAllAround(new PacketMTC(getEntityId(), mtcStatus, 2), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                speedLimit = thing.get("speedLimit").getAsInt();
                nextSpeedLimit = thing.get("nextSpeedLimit").getAsInt();
                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, 0, 0, 0, getEntityId()), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                if (nextSpeedLimit != 0) {
                    xSpeedLimitChange = thing.get("nextSpeedLimitChangeX").getAsDouble();
                    ySpeedLimitChange = thing.get("nextSpeedLimitChangeY").getAsDouble();
                    zSpeedLimitChange = thing.get("nextSpeedLimitChangeZ").getAsDouble();
                }

            } else if (thing.get("funct").getAsString().equals("response")) {
                mtcType = 2;
                mtcStatus = thing.get("mtcStatus").getAsInt();
                Traincraft.mscChannel.sendToAllAround(new PacketMTC(getEntityId(), mtcStatus, 2), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                nextSpeedLimit = thing.get("nextSpeedLimit").getAsInt();
                if (!speedGoingDown && xFromStopPoint == 0.0) {
                    speedLimit = thing.get("speedLimit").getAsInt();
                    Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, 0, 0, 0, getEntityId()), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                }

                if (thing.get("speedChange").getAsBoolean()) {
                    xSpeedLimitChange = thing.get("nextSpeedLimitChangeX").getAsDouble();
                    ySpeedLimitChange = thing.get("nextSpeedLimitChangeY").getAsDouble();
                    zSpeedLimitChange = thing.get("nextSpeedLimitChangeZ").getAsDouble();
                    Traincraft.itnsChannel.sendToAllAround(new PacketNextSpeed(nextSpeedLimit, 0, 0, 0, xSpeedLimitChange, ySpeedLimitChange, zSpeedLimitChange, getEntityId()), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                }

                if (thing.get("endSoon").getAsBoolean()) {
                    if (!(stationStop)) {
                        xFromStopPoint = thing.get("xStopPoint").getAsDouble();
                        yFromStopPoint = thing.get("yStopPoint").getAsDouble();
                        zFromStopPoint = thing.get("zStopPoint").getAsDouble();
                        Traincraft.atoSetStopPoint.sendToAllAround(new PacketATOSetStopPoint(getEntityId(), xFromStopPoint, yFromStopPoint, zFromStopPoint, xStationStop, yStationStop, zStationStop), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                    }
                }

                if (thing.get("stationStopSoon").getAsBoolean() && !stationStop) {
                    xStationStop = thing.get("xStationStop").getAsDouble();
                    yStationStop = thing.get("yStationStop").getAsDouble();
                    zStationStop = thing.get("zStationStop").getAsDouble();

                    Traincraft.atoSetStopPoint.sendToAllAround(new PacketATOSetStopPoint(getEntityId(), xFromStopPoint, yFromStopPoint, zFromStopPoint, xStationStop, yStationStop, zStationStop), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                }

                if (thing.get("atoStatus") != null) {
                    atoStatus = thing.get("atoStatus").getAsInt();
                    Traincraft.atoChannel.sendToAllAround(new PacketATO(getEntityId(), thing.get("atoStatus").getAsInt()), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                }
            }
        }
    }

    @Override
    public void sendMessage(PDMMessage message) {
        //	System.out.println("Sendmessage..");
        AxisAlignedBB targetBox = AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 2000, posY + 2000, posZ + 2000);
        List<TileEntity> allTEs = getWorld().loadedTileEntityList;
        for (TileEntity te : allTEs) {

            if (te instanceof TilePDMInstructionRadio) {

                TilePDMInstructionRadio teP = (TilePDMInstructionRadio) te;

                if (teP.uniqueID.equals(PDMMessage.UUIDTo)) {

                    //System.out.println(message.message);
                    teP.receiveMessage(message);
                }
            }
        }
    }

    public void attemptConnection(String theServerUUID) {
        //Oh, that's great! We just got the servers UUID. Now let's try connecting to it.
        if (theServerUUID != null && !serverUUID.equals(theServerUUID) && !canBePushed()) {
            //	System.out.println("Oh, that's great! We just got the servers UUID. Now let's try connecting to it.");
            JsonObject sendTo = new JsonObject();
            sendTo.addProperty("funct", "attemptconnection");
            sendTo.addProperty("trainType", trainLevel);
            //	System.out.println(sendTo.toString());
            sendMessage(new PDMMessage(trainID, theServerUUID, sendTo.toString(), 0));
        }
    }

    public void disconnectFromServer() {
        if (Loader.isModLoaded("ComputerCraft") || Loader.isModLoaded("OpenComputers")) {
            JsonObject sendTo = new JsonObject();
            sendTo.addProperty("funct", "disconnect");
            sendMessage(new PDMMessage(trainID, serverUUID, sendTo.toString(), 0));
            mtcType = 1;
            serverUUID = "";
            isConnected = false;
        }
    }

    /*
     * =========================================== MTC & ATO ===========================================
     **/

    public void updateMTCandATO() {
        // --- MTC (Minecraft Train Control) ---
        if (mtcStatus == 1 | mtcStatus == 2) {
            if (mtcType == 2) {
                //Send updates every few seconds
                if (ticksExisted % 20 == 0 && !canBePushed()) {
                    JsonObject sendingObj = new JsonObject();
                    sendingObj.addProperty("funct", "update");
                    sendingObj.addProperty("signalBlock", currentSignalBlock);
                    sendingObj.addProperty("destination", getDestinationGUI());
                    sendingObj.addProperty("trainLevel", trainLevel);
                    sendMessage(new PDMMessage(trainID, serverUUID, sendingObj.toString(), 1));
                }
            }
            isDriverOverspeed = getSpeed() > speedLimit && speedLimit != 0;

            if (isDriverOverspeed && ticksExisted % 120 == 0 && !overspeedBrakingInProgress && !overspeedOveridePressed && atoStatus != 1) {
                //Start braking because the driver is an idiot.
                overspeedBrakingInProgress = true;
            }
            if (overspeedBrakingInProgress && atoStatus != 1) {
                if (getSpeed() < speedLimit) {
                    //Stop overspeed braking.
                    overspeedBrakingInProgress = false;
                    isDriverOverspeed = false;
                } else {
                    slow(speedLimit);
                }
            }
            distanceFromStopPoint = getDistance(xFromStopPoint, yFromStopPoint, zFromStopPoint);
            distanceFromSpeedChange = getDistance(xSpeedLimitChange, ySpeedLimitChange, zSpeedLimitChange);

            if (distanceFromSpeedChange <= speedLimit && distanceFromSpeedChange <= getSpeed() && !(distanceFromSpeedChange <= nextSpeedLimit)) {
                speedLimit = (int) Math.round(distanceFromSpeedChange);
                speedGoingDown = true;

                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) posX, (int) posY, (int) posZ, getEntityId()), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                if (distanceFromSpeedChange <= 6) {
                    xSpeedLimitChange = 0.0;
                    ySpeedLimitChange = 0.0;
                    zSpeedLimitChange = 0.0;
                    speedLimit = nextSpeedLimit;
                    nextSpeedLimit = 0;
                    Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) posX, (int) posY, (int) posZ, getEntityId()), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                    Traincraft.itnsChannel.sendToAllAround(new PacketNextSpeed( nextSpeedLimit, 0,0,0, xSpeedLimitChange, ySpeedLimitChange, zSpeedLimitChange, getEntityId()), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                    speedGoingDown = false;
                }

            }

            if (distanceFromStopPoint >= 40 && distanceFromStopPoint < speedLimit && !(xFromStopPoint == 0.0) && mtcType == 1){
                speedLimit = (int)Math.round(distanceFromStopPoint);
                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) posX, (int) posY, (int) posZ, getEntityId()), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                speedGoingDown = true;
            }
            if (distanceFromStopPoint >= 10 && distanceFromStopPoint < speedLimit && !(xFromStopPoint == 0.0) && mtcType == 2){
                speedLimit = (int)Math.round(distanceFromStopPoint);
                Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) posX, (int) posY, (int) posZ, getEntityId()), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                speedGoingDown = true;
            }

				/*if (distanceFromStopPoint < getSpeed() && !(distanceFromStopPoint < nextSpeedLimit)  && !(this instanceof EntityLocoElectricPeachDriverlessMetro)) {
					speedLimit = (int) Math.round(distanceFromStopPoint);
					Traincraft.itsChannel.sendToAllAround(new PacketSetSpeed(speedLimit, (int) posX, (int) posY, (int) posZ, getEntityId()), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D) );
				}*/

            // --- ATO (Automatic Train Operation) ---
            if (atoStatus == 1) {
                distanceFromStationStop = getDistance(xStationStop, yStationStop, zStationStop);
                if (parkingBrake) {
                    parkingBrake = false;
                    //Accelerate to the speed limit
                }
                if (!(distanceFromStopPoint < getSpeed()) && (!(distanceFromSpeedChange < getSpeed()))) {
                    accel(speedLimit);
                }


                if (distanceFromStopPoint < getSpeed()) {
                    //Stop it at a certain point
                    stop(Vec3.createVectorHelper(xFromStopPoint, yFromStopPoint, zFromStopPoint));
                }
                if (distanceFromStationStop < getSpeed()) {
                    stop(Vec3.createVectorHelper(xStationStop, yStationStop, zStationStop));
                    stationStopping = true;

                } else {
                    stationStopping = false;
                }

                if (distanceFromSpeedChange < getSpeed() && !(getSpeed() == nextSpeedLimit)) {
                    //Slow it down to the next speed limit
                    slow(nextSpeedLimit);
                }

                if (isDriverOverspeed) {
                    //The ATO system is speeding somehow, slow it down
                    slow(speedLimit);
                }
                if (distanceFromStopPoint < 2 || distanceFromStationStop < 2) {
                    parkingBrake = true;
                    isBraking = true;
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

                    Traincraft.atoChannel.sendToAllAround(new PacketATO(getEntityId(), 0),new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));

                    Traincraft.atoSetStopPoint.sendToAllAround(new PacketATOSetStopPoint(getEntityId(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                    Traincraft.brakeChannel.sendToAllAround(new PacketParkingBrake(true, getEntityId()), new NetworkRegistry.TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
                    JsonObject sendingObj = new JsonObject();
                    sendingObj.addProperty("funct", "stationstopcomplete");
                    sendMessage(new PDMMessage(trainID, serverUUID, sendingObj.toString(), 0));
                }
            }
        }
    }

    public void accel(Integer desiredSpeed) {
        if (getWorld() != null) {
            if (getSpeed() != desiredSpeed) {
                if ((int) getSpeed() <= speedLimit) {
                    double rotation = seats.get(0).getPassenger() == null?rotationYaw:seats.get(0).getPassenger().rotationYaw;
                    double[] motion = CommonUtil.rotatePoint(0.002,0,rotation==0?0:CommonUtil.floorDouble(rotation/90d)*90);
                    motion[1]= MathHelper.sqrt_double(motion[0]*motion[0]+motion[2]*motion[2]);
                    appendMovement(motion[1]);
                }
            }
        }
    }

    public void slow(Integer desiredSpeed) {
        if (getSpeed() >= desiredSpeed) {
            multiplyVelocity(brakingRate);
        }
    }

    public void stop(Vec3 signalPosition) {
        double currentDistance = Math.copySign(Vec3.createVectorHelper(posX, posY, posZ).distanceTo(signalPosition), 1.0D);
        if (1.0D - currentDistance != 0.0D && currentDistance != 0.0D) {
            multiplyVelocity(currentDistance / getSpeed());
        } else {
            multiplyVelocity(0.5);
        }
    }

    /** RC routing integration */
    @Override
    public boolean setDestination(ItemStack ticket) {
        if (ticket != null) {
            destination = getTicketDestination(ticket);
            return true;
        }
        return false;
    }
}
