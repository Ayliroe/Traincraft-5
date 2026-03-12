package train.common.api;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.Vec3f;
import io.netty.buffer.ByteBuf;
import mods.railcraft.api.carts.CartTools;
import mods.railcraft.api.carts.IMinecart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import train.client.render.Bogie;
import train.client.render.TransportRenderCache;
import train.common.Traincraft;
import train.common.adminbook.ItemAdminBook;
import train.common.api.components.Links;
import train.common.core.handlers.ConfigHandler;
import train.common.entity.TrustedPlayer;
import train.common.items.ItemRollingStock;
import train.common.items.ItemWrench;
import train.common.library.Info;
import train.common.library.RenderRecord;
import train.common.library.SoundRecord;
import train.common.library.TraincraftRegistry;
import train.common.library.TraincraftRegistry.TrainRegister;
import train.common.overlaytexture.OverlayTextureManager;

import java.util.*;

public abstract class AbstractTrains extends EntityMinecart implements IMinecart, IEntityAdditionalSpawnData, IEntityMultiPart, IInventory, IFluidHandler {

    // --- MAIN ---
    private TrainRegister register = null;
    public final Links links = new Links(this);

    // --- RENDER ---
    public TransportRenderCache render_cache = new TransportRenderCache();
    public final Map<String, TextureDescription> textureDescriptionMap = new HashMap<>();
    private OverlayTextureManager overlayTextureContainer;
    private boolean acceptsOverlayTextures = false;

    // --- STATE ---
    protected boolean itemdropped = false;
    public String trainName = "";       // Name taken from the item name
    public double mass = 1;             // Mass, multiplied by 10 to get tons
    public double defaultMass = 1;      // Empty mass ignoring items/liquids
    public boolean locked = false;
    private List<TrustedPlayer> trustedList = new ArrayList<>();    // Players trusted to use the train
    public String trainOwner = "";      // User who spawned the train
    public String trainCreator = "";    // User who created the train
    public double trainDistanceTraveled = 0;

    // --- CHUNKLOADING ---
    protected Ticket chunkTicket;
    public List<ChunkCoordIntPair> loadedChunks = new ArrayList<>();
    public boolean shouldChunkLoad = true;

    // --- UUIDs ---
    public int uniqueID = -1;           // Unique ID given when created
    public static int uniqueIDs = 1;    // Stores the last ID given
    public static int numberOfTrains;

    /*
     * =========================================== INIT ===========================================
     **/

    public AbstractTrains(World world) {
        super(world);

        setSize(0.25f, 0.25f);
        renderDistanceWeight = 2.0D;

        // TODO: merge stuff into json
        dataWatcher.addObject(30, "");
        dataWatcher.addObject(7, trainOwner);
        // 8 is unused
        dataWatcher.addObject(9, trainName);
        dataWatcher.addObject(10, numberOfTrains);
        dataWatcher.addObject(11, uniqueID);
        dataWatcher.addObject(13, trainCreator);
        shouldChunkLoad = ConfigHandler.CHUNK_LOADING;
        setShouldChunkLoad(shouldChunkLoad);
    }

    /**
     * IMPORTANT NOTICE
     * Minecraft hates the idea of having custom constructors, and can only handle "Entity(World world)" when spawning the clientside entity in EntitySpawnHandler.java
     * This function allows passing parameters on creation, ex. the stock's spec.
     * It is called once inside TrainRecord.getEntity() for the server, and below in readSpawnData() for the client
     * DO NOT PUT FUNCTIONS IN THE CONSTRUCTOR THAT RELY ON SUCH VALUES; as they won't be available yet. Put such functions inside init() instead.
     */
    public void init(TrainRegister register) {
        this.register = register;
        setDefaultMass(weightKg()*0.1);
        setMinecartName(getName());
        dataWatcher.updateObject(30, getDefaultSkin());
    }

    // Called on the serverside entity before the clientside one is spawned, to pass parameters that must be valid at init.
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, register.type.getName());
        buffer.writeBoolean(getTrainLockedFromPacket());
    }

    // Called on the clientside entity to obtain the parameters above.
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        init(TraincraftRegistry.trains.get(ByteBufUtils.readUTF8String(additionalData)));
        setTrainLockedFromPacket(additionalData.readBoolean());
    }

    /*
     * =========================================== NBT ===========================================
     **/

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("register", register.type.getName());
        nbttagcompound.setString("skin", getSkin());
        nbttagcompound.setBoolean("chunkLoadingState", getFlag(7));
        nbttagcompound.setDouble("trainDistanceTraveled", trainDistanceTraveled);
        nbttagcompound.setString("theOwner", trainOwner);
        nbttagcompound.setBoolean("locked", locked);
        nbttagcompound.setString("theCreator", trainCreator);
        exportTrustedListToNBT(nbttagcompound);
        nbttagcompound.setString("theName", trainName);
        nbttagcompound.setInteger("uniqueID", uniqueID);
        //nbttagcompound.setInteger("uniqueIDs",uniqueIDs);

        links.writeEntityToNBT(nbttagcompound);

        nbttagcompound.setInteger("numberOfTrains", AbstractTrains.numberOfTrains);
        nbttagcompound.setTag("Motion", newDoubleNBTList(motionX, motionY, motionZ));
        nbttagcompound.setInteger("Dim", dimension);

        nbttagcompound.setLong("UUIDM", getUniqueID().getMostSignificantBits());
        nbttagcompound.setLong("UUIDL", getUniqueID().getLeastSignificantBits());
        nbttagcompound.setBoolean("acceptsOverlayTextures", acceptsOverlayTextures);
        if (acceptsOverlayTextures) {
            nbttagcompound.setTag("overlayTextureConfigTag", overlayTextureContainer.getOverlayConfigTag());
        }
        exportTrustedListToNBT(nbttagcompound);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        register = TraincraftRegistry.trains.get(nbttagcompound.getString("register"));
        setSkin(nbttagcompound.getString("skin"));
        setShouldChunkLoad(nbttagcompound.getBoolean("chunkLoadingState"));
        trainDistanceTraveled = nbttagcompound.getDouble("trainDistanceTraveled");
        trainOwner = nbttagcompound.getString("theOwner");
        locked = nbttagcompound.getBoolean("locked");
        setFlag(8, locked);
        trainCreator = nbttagcompound.getString("theCreator");
        importTrustedListFromNBT(nbttagcompound);
        trainName = nbttagcompound.getString("theName");
        uniqueID = nbttagcompound.getInteger("uniqueID");
        //uniqueIDs = nbttagcompound.getInteger("uniqueIDs");
        setInformation(trainOwner, trainCreator, trainName, uniqueID);

        links.readEntityFromNBT(nbttagcompound);

        numberOfTrains = nbttagcompound.getInteger("numberOfTrains");

        NBTTagList nbttaglist1 = nbttagcompound.getTagList("Motion", 6);
        motionX = nbttaglist1.func_150309_d(0);
        motionZ = nbttaglist1.func_150309_d(2);

        if(nbttagcompound.hasKey("Dim")){
            dimension=nbttagcompound.getInteger("Dim");
        }
        if(nbttagcompound.hasKey("UUIDM")){
            entityUniqueID = new UUID(nbttagcompound.getLong("UUIDM"), nbttagcompound.getLong("UUIDL"));
        }
        if (nbttagcompound.getBoolean("acceptsOverlayTextures")) {
            acceptsOverlayTextures = true;
            overlayTextureContainer.importFromConfigTag(nbttagcompound.getCompoundTag("overlayTextureConfigTag"));
        }
    }

    @Override
    public boolean writeMountToNBT(NBTTagCompound tag) {
        return false;
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound p_70039_1_) {
        if (!isDead && getEntityString() != null) {
            p_70039_1_.setString("id", getEntityString());
            writeToNBT(p_70039_1_);
            return true;
        }
        return false;
    }

    /*
     * =========================================== TYPE ===========================================
     **/

    public Item getItem()               { return register.item; }

    public String getName()             { return register.type.getName(); }
    public float getMHP()               { return register.type.getMHP(); }
    public float getMaxSpeed()          { return register.type.getMaxSpeed(); }
    public float weightKg()             { return register.type.getMass()*10f; }
    public int getFuelConsumption()     { return register.type.getFuelConsumption(); }
    public int getWaterConsumption()    { return register.type.getWaterConsumption(); }
    public int getHeatingTime()         { return register.type.getHeatingTime(); }
    public double getAccel()            { return register.type.getAccelerationRate(); }
    public double getBrake()            { return register.type.getBrakeRate(); }
    public int[] getTankCapacity()      { return new int[]{register.type.getTankCapacity()}; }
    @Override
    public int getSizeInventory()       { return register.type.getCargoCapacity(); }
    public List<String> getSkins()      { return register.type.getSkins(); }
    public String getDefaultSkin()      { return !getSkins().isEmpty() ? getSkins().get(0) : ""; }
    public float getOptimalDistance()   { return register.type.getOptimalDistance() != 0 ? register.type.getOptimalDistance() : getHitboxSize()[0]*0.5f; }
    public float[] getHitboxSize(){
        if (register.type.getHitboxSize().length != 0) { return register.type.getHitboxSize(); }
        if (register.type.getBogieLocoPosition() != 0) { return new float[]{(float)Math.abs(register.type.getBogieLocoPosition())+(Math.abs(getOptimalDistance()*2f)),1.65f,1f}; }
                                                         return new float[]{Math.abs((getOptimalDistance()*2)),1.65f,1f};
    }
    @Override
    public boolean shouldRiderSit()     { return register.type.getShouldRiderSit(); }
    public float[][] getRiderOffsets()  { return register.type.getRiderOffsets(); }
    public AbstractTrains makeNewEntity(World world) { return register.getEntity(world); }

    /**defines the points that the entity uses for path-finding and rotation, with 0 being the entity center.
     * Usually the point where the front and back bogies would connect to the transport.
     * Or the center of the frontmost and backmost wheel if there are no bogies.
     * The first value is the back point, the second is the front point
     * example:
     * return new float{2f, -1f};
     * may not return null*/
    public float[] rotationPoints() {
        if(register.type.getBogieLocoPosition() == 0){
            return new float[]{getHitboxSize()[0]*0.5f,-getHitboxSize()[0]*0.5f};
        }
        return new float[]{0,-(float)Math.abs(register.type.getBogieLocoPosition())};
    }

    /*
     * =========================================== RENDER ===========================================
     **/

    public RenderRecord getRender()     { return register.render; }
    @SideOnly(Side.CLIENT)
    public ModelBase[] getModel()       { return new ModelBase[]{ register.render.getModel() }; }
    @SideOnly(Side.CLIENT)
    public float[][] modelOffsets()     { return new float[][]{ register.render.getTrans() }; }
    @SideOnly(Side.CLIENT)
    public float[][] modelRotations()   { return new float[][]{ register.render.getRotate() }; }
    public float[][] getRenderScale()   { return new float[][]{ register.render.getScale() }; }
    public Bogie[] bogies()             { return null; }

    public float getPlayerScale()       { return 1f; }
    public ArrayList<double[]> getSmokePosition() {return null;}
    public int[] getParticleData(int id) {
        switch (id){
            case 1: {return new int[]{1,200,0xFF0000};}
            default: {return new int[]{1,10,0xCCCC11};}
        }
    }

    /**
     * @author 02skaplan
     * <p>Called to setup the overlay texture manager for the given AbstractTrain. It is recommended
     * to call this from the constructor of the AbstractTrain-derived entity class.</p>
     * <p>After calling, it is recommended to use getOverlayTextureContainer to initialze the fixed, dynamic, or both
     * fixed and dynamic overlays with their respective settings.</p>
     * @param acceptedType Whether the overlay manager will allow fixed, dynamic, or both fixed and dynamic overlays.
     */
    public void initOverlayTextures(OverlayTextureManager.Type acceptedType) {
        overlayTextureContainer = new OverlayTextureManager(acceptedType, this);
        acceptsOverlayTextures = true;
    }
    public OverlayTextureManager getOverlayTextureContainer() { return overlayTextureContainer; }
    public boolean acceptsOverlayTextures() { return acceptsOverlayTextures; }

    /*
     * =========================================== SOUND ===========================================
     **/

    public SoundRecord getSounds()      { return register.sounds; }
    public TrainSound getBell()         { return new TrainSound(Info.resourceLocation + ":bell",0.5f,1f, 0); }

    public TrainSound getHorn() {
        if(!register.sounds.getHornString().isEmpty()) {
            return new TrainSound(register.sounds.getHornString(), register.sounds.getHornVolume(),1f, 0);
        }
        return null;
    }

    public TrainSound getRunningSound(){
        if(!register.sounds.getRunString().isEmpty()) {
            TrainSound sound = new TrainSound(register.sounds.getRunString(), register.sounds.getRunVolume(), 0.4f, register.sounds.getRunSoundLength());
            if(register.sounds.getSoundChangeWithSpeed()) {
                sound.enableRunningPitch();
            }
            return sound;
        }
        return null;
    }

    public TrainSound getIdleSound() {
        if(!register.sounds.getIdleString().isEmpty()) {
            return new TrainSound(register.sounds.getIdleString(), register.sounds.getIdleVolume(),0.001F, register.sounds.getIdleSoundLength());
        }
        return null;
    }

    /*
     * =========================================== OTHER PROPERTIES ===========================================
     **/

    public World getWorld() { return worldObj; }

    public Vec3f getPos() { return new Vec3f(posX, posY, posZ); }

    // Only used for ComputerCraft peripherals, TODO refactor
    public String getTrainType() {
        if(this instanceof SteamTrain)       return "steam";
        if(this instanceof DieselTrain)      return "diesel";
        if(this instanceof ElectricTrain)    return "electric";
        if(this instanceof Tender)           return "tender";
        if(this instanceof AbstractWorkCart) return "work";
        if(this instanceof Freight)          return "freight";
        if(this instanceof IPassenger)       return "passenger";
        if(this instanceof LiquidTank)       return "tank";
        return "decorative";
    }

    // --- OWNER ---
    public String getTrainOwner()       { return dataWatcher.getWatchableObjectString(7); }
    public String getTrainName()        { return dataWatcher.getWatchableObjectString(9); }
    public String getTrainCreator()     { return dataWatcher.getWatchableObjectString(13); }
    public GameProfile getOwner()       { return CartTools.getCartOwner(this); }    // ??? Difference w/ train.getTrainOwner().equalsIgnoreCase(playerEntity.getDisplayName())

    // --- UUID ---
    public int getUniqueTrainID()       { return uniqueID; }
    public void setNewUniqueID(int numberOfTrains) {
        if (numberOfTrains <= 0) {
            numberOfTrains = uniqueIDs++;
        } else {
            uniqueIDs = numberOfTrains++;
        }
        uniqueID = numberOfTrains;
        getEntityData().setInteger("uniqueID", numberOfTrains);
    }
    public String getPersistentUUID() {
        if (getEntityData().hasKey("puuid")) {
            return getEntityData().getString("puuid");
        } else {
            getEntityData().setString("puuid", getUniqueID().toString());
            return getUniqueID().toString();
        }
    }

    // --- CHUNK LOADING ---
    public boolean getShouldChunkLoad()                     { return getFlag(7); }
    public void setShouldChunkLoad(boolean chunkLoadState)  { setFlag(7, chunkLoadState); }
    public Ticket getTicket()                               { return chunkTicket; }
    public void setTicket(Ticket ticket)                    { chunkTicket = ticket; }
    public void requestTicket() {
        Ticket chunkTicket = ForgeChunkManager.requestTicket(Traincraft.instance, getWorld(), ForgeChunkManager.Type.ENTITY);
        if (chunkTicket != null) {
            chunkTicket.setChunkListDepth(25);
            chunkTicket.bindEntity(this);
            setTicket(chunkTicket);
        }
    }

    // --- LOCKING ---
    public boolean getTrainLockedFromPacket() { return locked; }
    public void setTrainLockedFromPacket(boolean set) { locked = set; }
    public boolean canBeRiddenWhileLocked() { return this instanceof Locomotive || this instanceof IPassenger || this instanceof AbstractWorkCart; }
    protected boolean lockThisCart(ItemStack itemstack, EntityPlayer entityplayer) {
        if (itemstack != null && (itemstack.getItem() instanceof ItemWrench || itemstack.getItem() instanceof ItemAdminBook)) {
            if (entityplayer.getDisplayName().equals(trainOwner) || entityplayer.getGameProfile().getName().equals(trainOwner)
                    || trainOwner.isEmpty() || entityplayer.canCommandSenderUseCommand(2, "")) {
                if (locked) {
                    locked = false;
                    if (getWorld().isRemote) {
                        entityplayer.addChatMessage(new ChatComponentText("Unlocked."));
                    }
                } else {
                    locked = true;
                    if (getWorld().isRemote) {
                        entityplayer.addChatMessage(new ChatComponentText("Locked."));
                    }
                }
            } else if (getWorld().isRemote) {
                entityplayer.addChatMessage(new ChatComponentText("You are not the owner!"));
            }
            return true;
        }
        return false;
    }

    // --- SKIN ---
    public String getSkin() { return dataWatcher.getWatchableObjectString(30); }
    public boolean setSkin(String skin) {
        if (getSkins().contains(skin) && !getSkin().equals(skin)) {
            dataWatcher.updateObject(30, skin);
            return true;
        }
        return false;
    }

    // --- DEFAULT MASS ---
    protected double getDefaultMass()   { return defaultMass; }
    protected void setDefaultMass(double def) {
        mass = def;
        defaultMass = def;
    }

    // --- INFO ---
    public void setInformation(String trainOwner, String trainCreator, String trainName, int uniqueID) {
        if (!getWorld().isRemote) {
            dataWatcher.updateObject(7, trainOwner);
            dataWatcher.updateObject(9, trainName);
            dataWatcher.updateObject(11, uniqueID);
            if (trainCreator != null && !trainCreator.isEmpty()) {
                dataWatcher.updateObject(13, trainCreator);
            }
        }
    }

    // --- ITEM ---
    /**
     * This function returns an ItemStack that represents this cart. This should
     * be an ItemStack that can be used by the player to place the cart. This is
     * the item that was registered with the cart via the registerMinecart
     * function, but is not necessary the item the cart drops when destroyed.
     *
     * @return An ItemStack that can be used to place the cart.
     */
    @Override
    public ItemStack getCartItem()      { return new ItemStack(getItem()); }
    public abstract List<ItemStack> getItemsDropped();

    public void dropCartAsItem(boolean isCreative) {
        if (!isCreative && !itemdropped) {
            itemdropped = true;
            for (ItemStack item : getItemsDropped()) {
                if (item.getItem() instanceof ItemRollingStock) {
                    ItemStack stack = ItemRollingStock.setPersistentData(item, this, getUniqueTrainID(), trainCreator, trainOwner, getSkin());
                    entityDropItem(stack != null ? stack : item, 0);
                } else {
                    setUniqueIDToItem(item);
                    entityDropItem(item, 0);
                }
            }
        }
    }

    protected void setUniqueIDToItem(ItemStack stack) {
        NBTTagCompound var3 = stack.getTagCompound();
        if (var3 == null) {
            var3 = new NBTTagCompound();
            stack.setTagCompound(var3);
        }
        if (uniqueID != -1) stack.getTagCompound().setInteger("uniqueID", uniqueID);
        if (trainCreator != null && !trainCreator.isEmpty()) stack.getTagCompound().setString("trainCreator", trainCreator);
        exportTrustedListToNBT(stack.getTagCompound());
        stack.getTagCompound().setString("trainColor", getSkin());
        // Only save the overlay configuration to NBT if it exists. No need to store an empty configuration in NBT as it will be initialized as the default when the entity spawns in.
        if (acceptsOverlayTextures && getOverlayTextureContainer().getType() != OverlayTextureManager.Type.NONE) {
            stack.getTagCompound().setTag("overlayTextureConfigTag", getOverlayTextureContainer().getOverlayConfigTag());
        }
    }

    /*
     * =========================================== TRUSTED LIST ===========================================
     **/

    /**
     * @return Returns String ArrayList of trusted players' usernames.
     */
    public List<TrustedPlayer> getTrustedList() {
        return trustedList;
    }
    public void setTrustedList(List<TrustedPlayer> trustedList) { this.trustedList = trustedList; }

    /**
     * <p>Returns whether a player is trusted to a piece of rolling stock.</p>
     * @param displayName Case-insensitive display name of player.
     * @return True if the player is trusted, false if the player is not trusted.
     */
    public boolean isPlayerTrusted(String displayName) {
        for (TrustedPlayer trustedPlayer : getTrustedList()) {
            if (trustedPlayer.getDisplayName().equalsIgnoreCase(displayName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Returns whether a player is trusted to break a piece of rolling stock.</p>
     * @param displayName Case-insensitive display name of player.
     * @return True if player has break access, false if player does not have break access.
     */
    public boolean isPlayerTrustedToBreak(String displayName) {
        for (TrustedPlayer trustedPlayer : getTrustedList()) {
            if (trustedPlayer.getDisplayName().equalsIgnoreCase(displayName)) {
                return trustedPlayer.hasBreakAccess();
            }
        }
        return false;
    }

    /**
     * <p>Export trusted players to NBT tag for data saving.</p>
     * @param nbttagcompound NBT tag into which to write trusted list.
     */
    public void exportTrustedListToNBT(NBTTagCompound nbttagcompound) {
        if (!trustedList.isEmpty()) {
            NBTTagList trustedList = new NBTTagList();
            for (TrustedPlayer trustedPlayer : this.trustedList) {
                NBTTagCompound trustedPlayerTag = new NBTTagCompound();
                trustedPlayerTag.setString("playerName", trustedPlayer.getDisplayName());
                trustedPlayerTag.setBoolean("breakAccess", trustedPlayer.hasBreakAccess());
                trustedList.appendTag(trustedPlayerTag);
            }
            nbttagcompound.setTag("trustedList", trustedList);
            nbttagcompound.setString("trustedListPreviousOwner", getTrainOwner());
        }
    }

    /**
     * <p>Import a trusted player list from a given NBT tag.</p>
     * @param nbttagcompound NBT tag from which to import trusted list.
     */
    public void importTrustedListFromNBT(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasKey("trustedList")) {
            NBTTagList trustedList = nbttagcompound.getTagList("trustedList", Constants.NBT.TAG_COMPOUND);
            this.trustedList.clear();
            for (int i = 0; i < trustedList.tagCount(); i++) {
                if (!trustedList.getCompoundTagAt(i).getString("playerName").equalsIgnoreCase(trainOwner)) // Check to ensure we're not adding the current owner to the trusted list...
                    this.trustedList.add(new TrustedPlayer(trustedList.getCompoundTagAt(i).getString("playerName"), trustedList.getCompoundTagAt(i).getBoolean("breakAccess")));
            }
            if (nbttagcompound.hasKey("trustedListPreviousOwner")) { // If the previous owner is not the one who placed down the piece of rolling stock...
                if (!nbttagcompound.getString("trustedListPreviousOwner").equalsIgnoreCase(trainOwner)) {
                    getTrustedList().add(new TrustedPlayer(nbttagcompound.getString("trustedListPreviousOwner"), true));
                }
            }
        }
    }

    /*
     * =========================================== IENTITYMULTIPART, ENTITYMINECART, ENTITY ===========================================
     **/

    @Override
    public World func_82194_d() { return getWorld(); }

    @Override
    public AxisAlignedBB getCollisionBox(Entity p_70114_1_) { return null; }

    @Override
    public double getMountedYOffset() { return 0; }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize() { return 0.0F; }

    @Override
    public boolean canBePushed() { return true; }

    @Override
    public void setDead() {
        ForgeChunkManager.releaseTicket(chunkTicket);
        super.setDead();
    }

    @Override
    public String getCommandSenderName() {
        return StatCollector.translateToLocal("item.tc:" + register.type.getName() + ".name");
    }

    // Return false if this cart should not call IRail.onMinecartPass() and should ignore Powered Rails.
    @Override
    public boolean shouldDoRailFunctions() { return true; }

    @Override
    public void moveMinecartOnRail(int i, int j, int k, double d) {}

    @Override
    public int getMinecartType() { return 0; }

    // Applies a velocity to each of the entities pushing them away from each other
    @Override
    public void applyEntityCollision(Entity par1Entity) {}

    @Override
    public boolean canBeCollidedWith() { return false; }

    /*
     * =========================================== RAILCRAFT IMINECART ===========================================
     **/

    // Railcart routing stuff
    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart) {
        if (stack == null || cart == null) {
            return false;
        }
        ItemStack cartItem = cart.getCartItem();
        return cartItem.getItem() == stack.getItem();
    }

    /*
     * =========================================== MINECRAFT IINVENTORY ===========================================
     **/

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) { return null; }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) { return null; }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) { return null; }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {}

    // Do not override, use getCommandSenderName() instead
    @Override
    public final String getInventoryName() { return null; }

    @Override
    public int getInventoryStackLimit() { return 0; }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) { return !isDead && entityplayer.getDistanceSqToEntity(this) <= 64D; }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) { return true; }

    /*
     * =========================================== FORGE IFLUIDHANDLER ===========================================
     **/

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) { return 0; }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) { return null; }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) { return null; }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) { return false; }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) { return false; }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) { return new FluidTankInfo[0]; }
}