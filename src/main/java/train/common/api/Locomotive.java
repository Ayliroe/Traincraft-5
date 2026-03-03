package train.common.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import io.netty.buffer.ByteBuf;
import mods.railcraft.api.tracks.RailTools;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.components.MTC;
import train.common.core.handlers.ConfigHandler;
import train.common.core.network.PacketSlotsFilled;
import train.common.enums.DataMemberName;
import train.common.library.TrainRecord;

import java.util.List;

public abstract class Locomotive extends Freight implements IRollingStockLightControls  {

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
    public double speedLimiter = 1;

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
    public MTC MTC = new MTC(this);

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
        if (this instanceof SteamTrain) isLocoTurnedOn = true;
        entityCollisionReduction = 0.99F;

        // --- DATA WATCHER ---
        dataWatcher.addObject(2, 0);
        dataWatcher.addObject(3, MTC.destination);
        dataWatcher.addObject(20, 0f); // Heat
        dataWatcher.addObject(23, ""); // State
        dataWatcher.addObject(24, fuelTrain);
        dataWatcher.addObject(25, 0); // Speed
        dataWatcher.addObject(26, guiDetailsJSON());
        dataWatcher.addObject(28, lightingDetailsJSONString());

        // --- UPDATE LINKS ---
        for(AbstractTrains t: consist){
            if(t.consistLeadID!=getEntityId()){
                updateLinks();
            }
        }

        // --- MTC ---
        MTC.generateTrainID();
        MTC.attemptConnection(MTC.serverUUID);
    }

    @Override
    public void init(TrainRecord spec) {
        super.init(spec);

        dataWatcher.updateObject(2, (int) getMaxSpeed());
        accelRate = getAccel();
        brakingRate = getBrake();
        fuelRate = getFuelConsumption();
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
        if (!getWorld().isRemote) {
            MTC.updateMTCandATO();
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
        double massPulledFactor = currentMassPulled * 0.07457;
        double totalMhp = getMHP() > 0 ? getMHP() : 100;    // Guarantee non-zero so we don't divide by zero

        // Append passive locos Mhp
        for (AbstractTrains stock : consist) {
            if (stock instanceof Locomotive && stock.uniqueID != uniqueID) {
                totalMhp += ((Locomotive)stock).getMHP();
            }
        }

        // Mhp debuffs
        currentSpeedSlowDown = massPulledFactor / totalMhp * 74.57;
        currentBrakeSlowDown = Math.pow(massPulledFactor,2) / totalMhp * 0.7457 * 0.8;
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
        setCurrentMaxSpeed((int)Math.max(       (getMaxSpeed()           - currentSpeedSlowDown) * speedMult,0));        // Avoid Speed < 0
        brakingRate = Math.min(                 getBrake()               + currentBrakeSlowDown,0.998);                 // Avoid Brake > 1 (acceleration)
        accelRate = Math.max(                   (getAccel()              - currentAccelSlowDown) * accelMult,0);        // Avoid Accel < 0 (braking)
        fuelRate =                              getFuelConsumption()     - currentFuelConsumptionChange;
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

    @Override
    public void markDirty() {
        super.markDirty();
        if (!getWorld().isRemote) {
            Traincraft.slotschannel.sendToAllAround(new PacketSlotsFilled(this, slotsFilled), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 150.0D));
        }
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
                if ((speed <= 0.10) && !brakePressed && heat > 0.5 && (getWorld().rand.nextInt(10) == 0)) {
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
            if (brakePressed) { heatBrakeDelay++; }
            else { heatBrakeDelay=0; }

            // Delayer has reached max and speed is not 0: overheat
            if (brakePressed && heatBrakeDelay > 40 && speed > 0.05) {
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
        if (super.attackEntityFrom(damagesource, i)) {
            MTC.disconnectFromServer();
            return true;
        }
        return false;
    }

    /*
     * =========================================== DATA WATCHER & PACKETS ===========================================
     **/

    public void updateDataWatcher() {
        dataWatcher.updateObject(3, MTC.destination);
        // 15 is unused
        dataWatcher.updateObject(24, fuelTrain);
        dataWatcher.updateObject(25, (int)Math.round(TrainUtils.convertSpeedInv(Math.sqrt(bogieBack.velocity[0] * bogieBack.velocity[0] + bogieBack.velocity[1] * bogieBack.velocity[1]))));
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
                                                return MTC.destination;
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
    public boolean isBeaconEnabled() {          return AsJsonObject(dataWatcher.getWatchableObjectString(28)).get(DataMemberName.isBeaconEnabled.AsString()).getAsBoolean(); }
    public byte getBeaconCycleIndex() {         return AsJsonObject(dataWatcher.getWatchableObjectString(28)).get(DataMemberName.beaconCycleIndex.AsString()).getAsByte(); }
    public boolean isDitchLightsEnabled() {     return AsJsonObject(dataWatcher.getWatchableObjectString(28)).get(DataMemberName.ditchLightMode.AsString()).getAsByte() > 0; }
    public void setPacketLights(boolean isLocoLightsOn) { isLightsEnabled = isLocoLightsOn; }
    public void setPacketBeacon(boolean isLocoBeaconEnabled) { isBeaconEnabled = isLocoBeaconEnabled; }
    public void setPacketDitchLightsMode(byte ditchLightMode) { this.ditchLightMode = ditchLightMode; }

    private JsonObject AsJsonObject(String string) { return new JsonParser().parse(string).getAsJsonObject(); }

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
        nbttagcompound.setDouble("speedLimiter", speedLimiter);

        nbttagcompound.setString("lastRider", lastRider);

        nbttagcompound.setInteger("fuelTrain", fuelTrain);

        // --- MTC & ATO ---
        MTC.writeEntityToNBT(nbttagcompound);

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
        speedLimiter = ntc.getDouble("speedLimiter");

        lastRider = ntc.getString("lastRider");

        fuelTrain = ntc.getInteger("fuelTrain");

        // --- MTC & ATO ---
        MTC.readEntityFromNBT(ntc);

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
    public boolean keyHandlerFromPacket(int i, EntityPlayer player) {
        if (super.keyHandlerFromPacket(i, player)) { return true; }

        if (i == 4)  {  forwardPressed = true; }
        if (i == 5)  {  backwardPressed = true; }
        if (i == 8 && ConfigHandler.SOUNDS) {   soundHorn(); }
        if (i == 10 && ConfigHandler.SOUNDS) {  soundWhistle(); }
        if (i == 12) {  brakePressed = true; }
        if (i == 13) {  forwardPressed = false; }
        if (i == 14) {  backwardPressed = false; }
        if (i == 15) {  brakePressed = false; }
        if (i == 16) {  MTC.toggleATO(); }
        if (i == 17) {  MTC.toggleMTCOverride(); }
        if (i == 18) {  MTC.toggleOverspeedOverride(); }

        return false;
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
}