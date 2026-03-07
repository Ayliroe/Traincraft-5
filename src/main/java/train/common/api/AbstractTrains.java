package train.common.api;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.entities.EntitySeat;
import fexcraft.tmt.slim.ModelBase;
import io.netty.buffer.ByteBuf;
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

    public boolean isAttached = false;
    public boolean isAttaching = false;
    public static int numberOfTrains;
    public double Link1;
    public double Link2;
    public AbstractTrains frontLink;
    public AbstractTrains backLink;
    public ArrayList<AbstractTrains> consist;
    public Integer consistLeadID=null;

    protected Ticket chunkTicket;
    public List<ChunkCoordIntPair> loadedChunks = new ArrayList<>();
    public boolean shouldChunkLoad = true;
    protected boolean itemdropped = false;

    public TransportRenderCache render_cache = new TransportRenderCache();

    public EntityBogie bogieFront=null;
    public EntityBogie bogieBack=null;

    public List<EntitySeat> seats = new LinkedList<>();

    public String trainName = "";       // Name taken from the item name
    public double mass = 1;             // Mass, multiplied by 10 to get tons
    public double defaultMass = 1;      // Empty mass ignoring items/liquids
    public boolean locked = false;
    private List<TrustedPlayer> trustedList = new ArrayList<>();    // Players trusted to use the train
    public String trainOwner = "";      // User who spawned the train
    public String trainCreator = "";    // User who created the train
    public int uniqueID = -1;           // Unique ID given when created
    public static int uniqueIDs = 1;    // Stores the last ID given

    public double trainDistanceTraveled = 0;

    public final Map<String, TextureDescription> textureDescriptionMap = new HashMap<>();
    private OverlayTextureManager overlayTextureContainer;
    private boolean acceptsOverlayTextures = false;

    private TrainRegister register = null;

    public AbstractTrains(World world) {
        super(world);

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
        setSize(0.98f, 1.98f);
        setMinecartName(getName());
        dataWatcher.updateObject(30, getDefaultSkin());
    }

    /**
     * <p>This method is called on the client side when an entity is being loaded in. The additionalData buffer is sent from the server
     * and is populated by the server using the writeSpawnData method.</p>
     * <br></br><p>"this is basically NBT for entity spawn, to keep data between client and server in sync because some data is not automatically shared."</p>
     * @param additionalData The packet data stream
     */
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        init(TraincraftRegistry.trains.get(ByteBufUtils.readUTF8String(additionalData)));
        setTrainLockedFromPacket(additionalData.readBoolean());
    }

    /**
     * <p>This method is called on the server side when a connected client is loading the entity. Data written
     * to the ByteBuffer will be synced with the client and available to the client through the readSpawnData method.</p>
     * <br></br><p>"this is basically NBT for entity spawn, to keep data between client and server in sync because some data is not automatically shared."</p>
     * @param buffer The packet data stream
     */
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, register.type.getName());
        buffer.writeBoolean(getTrainLockedFromPacket());
    }

    public String getTrainOwner()       { return dataWatcher.getWatchableObjectString(7); }
    public String getTrainName()        { return dataWatcher.getWatchableObjectString(9); }
    public String getTrainCreator()     { return dataWatcher.getWatchableObjectString(13); }

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

    public RenderRecord getRender()     { return register.render; }
    public SoundRecord getSounds()      { return register.sounds; }
    public String getName()             { return register.type.getName(); }
    public Item getItem()               { return register.type.getItem(); }
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
    public String getDefaultSkin()      { return !getSkins().isEmpty() ? getSkins().get(0) : ""; }
    public float getOptimalDistance()   { return register.type.getOptimalDistance() != 0 ? register.type.getOptimalDistance() : getHitboxSize()[0]*0.5f; }
    public float[] getHitboxSize(){
        if(register.type.getHitboxSize().length != 0) {
            return register.type.getHitboxSize();
        }
        if(register.type.getBogieLocoPosition() != 0) {
            return new float[]{(float)Math.abs(register.type.getBogieLocoPosition())+(Math.abs(getOptimalDistance()*2f)),2f,1f};
        }
        return new float[]{Math.abs((getOptimalDistance()*2)),2f,1f};
    }
    @Override
    public boolean shouldRiderSit()     { return register.type.getShouldRiderSit(); }
    public float[][] getRiderOffsets()  { return register.type.getRiderOffsets(); }
    public AbstractTrains getEntity(World world) { return register.getEntity(world); }

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

    @Override
    public AxisAlignedBB getCollisionBox(Entity p_70114_1_) {
        if (riddenByEntity != p_70114_1_) {
            return super.getCollisionBox(p_70114_1_);
        } else {
            return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize() {
        return 0.0F;
    }

    public abstract boolean isLinked();

    public abstract List<ItemStack> getItemsDropped();

    public int getUniqueTrainID() {
        return uniqueID;
    }

    @Override
    public void setDead() {
        ForgeChunkManager.releaseTicket(chunkTicket);
        super.setDead();
    }

    public void setNewUniqueID(int numberOfTrains) {

        if (numberOfTrains <= 0) {
            numberOfTrains = uniqueIDs++;
        } else {
            uniqueIDs = numberOfTrains++;
        }
        uniqueID = numberOfTrains;
        getEntityData().setInteger("uniqueID", numberOfTrains);
    }

    public void setShouldChunkLoad(boolean chunkLoadState) { setFlag(7, chunkLoadState); }
    public boolean getShouldChunkLoad() { return getFlag(7); }

    public boolean setSkin(String skin) {
        if (getSkins().contains(skin) && !getSkin().equals(skin)) {
            dataWatcher.updateObject(30, skin);
            return true;
        }
        return false;
    }

    public String getSkin() { return dataWatcher.getWatchableObjectString(30); }
    public List<String> getSkins() { return register.type.getSkins(); }

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

        nbttagcompound.setInteger("numberOfTrains", AbstractTrains.numberOfTrains);
        nbttagcompound.setBoolean("isAttached", isAttached);
        nbttagcompound.setTag("Motion", newDoubleNBTList(motionX, motionY, motionZ));
        nbttagcompound.setDouble("Link1", Link1);
        nbttagcompound.setDouble("Link2", Link2);

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

        numberOfTrains = nbttagcompound.getInteger("numberOfTrains");
        isAttached = nbttagcompound.getBoolean("isAttached");
        NBTTagList nbttaglist1 = nbttagcompound.getTagList("Motion", 6);            motionX = nbttaglist1.func_150309_d(0);
        motionX = nbttaglist1.func_150309_d(0);
        motionZ = nbttaglist1.func_150309_d(2);
        Link1 = nbttagcompound.getDouble("Link1");
        Link2 = nbttagcompound.getDouble("Link2");
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

    protected void setDefaultMass(double def) {
        mass = def;
        defaultMass = def;
    }

    protected double getDefaultMass() {
        return defaultMass;
    }

    /**
     * Lock packet
     */
    public boolean getTrainLockedFromPacket() {
        return locked;
    }

    /**
     * Lock packet
     */
    public void setTrainLockedFromPacket(boolean set) {
        // System.out.println(getWorld().isRemote + " " + set);
        locked = set;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }


    /**
     * Locking for passengers, flat, caboose, jukebox,workcart
     */
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

    protected boolean canBeRiddenWhileLocked(AbstractTrains train) {
        return (train instanceof Locomotive) || (train instanceof IPassenger) || (train instanceof AbstractWorkCart);
    }

    /**
     * Railcraft routing integration
     */
    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart) {
        if (stack == null || cart == null) {
            return false;
        }
        ItemStack cartItem = cart.getCartItem();
        return cartItem.getItem() == stack.getItem();
    }

    @Override
    public String getCommandSenderName() {
        return StatCollector.translateToLocal("entity.tc." + register.type.getInternalName() + ".name");
    }

    public void setTicket(Ticket ticket) {
        chunkTicket = ticket;
    }

    public Ticket getTicket() {
        return chunkTicket;
    }

    public void requestTicket() {
        Ticket chunkTicket = ForgeChunkManager.requestTicket(Traincraft.instance, getWorld(), ForgeChunkManager.Type.ENTITY);
        if (chunkTicket != null) {
            chunkTicket.setChunkListDepth(25);
            chunkTicket.bindEntity(this);
            setTicket(chunkTicket);
        }
    }

    public String getPersistentUUID() {
        if (getEntityData().hasKey("puuid")) {
            return getEntityData().getString("puuid");
        } else {
            getEntityData().setString("puuid", getUniqueID().toString());
            return getUniqueID().toString();
        }
    }

    /**
     * called on linking changes and when a train changes running states
     * @param consist the list of entities in the consist
     */
    public void setValuesOnLinkUpdate(ArrayList<AbstractTrains> consist){
        this.consist=consist;

        if (this instanceof Locomotive) {
            ((Locomotive)this).currentMassPulled=0;
            for (AbstractTrains t : consist) {
                ((Locomotive)this).currentMassPulled += t.weightKg();
            }
        }
    }

    public void updateLinks(){
        ArrayList<AbstractTrains> transports = new ArrayList<>();

        traverseConsist(this, transports);
        if(transports.size()<2){
            consist = transports;
            consistLeadID = getEntityId();
            return;
        }

        AbstractTrains frontTrain = findFront(transports);
        transports = new ArrayList<>();
        traverseConsist(frontTrain, transports);
        
        for (AbstractTrains t : transports) {
            t.consist = transports;
            t.consistLeadID = frontTrain.getEntityId();
            t.setValuesOnLinkUpdate(transports);
        }
    }
    
    private void traverseConsist(AbstractTrains current, ArrayList<AbstractTrains> visited) {
        if (current == null || visited.contains(current)) {
            return;
        }
        
        visited.add(current);
        if (current.frontLink != null) {
            traverseConsist(current.frontLink, visited);
        }
        if (current.backLink != null) {
            traverseConsist(current.backLink, visited);
        }
    }
    
    private AbstractTrains findFront(ArrayList<AbstractTrains> transports) {
        for (AbstractTrains train : transports) {
            if (train instanceof Locomotive && !train.canBePushed()) {
                return train;
            }
        }

        for (AbstractTrains train : transports) {
            if (train.frontLink == null || train.backLink == null) {
                return train;
            }
        }
        
        return transports.get(0);
    }

    /**
     * Finds the direction from which a locomotive is pulling/pushing from.
     * @return Returns 1 if from front, -1 if from back, or 0 if no pulling locomotive exists or this is the pulling locomotive.
     */
    protected int pullingLocomotiveDirection() {
        if (this instanceof Locomotive && !canBePushed()) {
            return 0;
        }

        ArrayList<AbstractTrains> visited = new ArrayList<>();  // In case somebody makes a circular train
        visited.add(this);

        boolean visitingFront = true;

        AbstractTrains previousTrain = this;
        AbstractTrains train = frontLink;
        while (!visited.contains(train)) {
            if (train == null) {
                // If we have reached the front end, reset and start from the back. If we reached that other end too, break.
                if (visitingFront) {
                    visitingFront = false;
                    train = backLink;
                    previousTrain = this;
                    continue;
                }
                else {
                    break;
                }
            }

            visited.add(train);

            if (train instanceof Locomotive && !train.canBePushed()) {
                return visitingFront ? 1 : -1;
            }

            // Trains can link front to front and back to back, so keep traversing toward whatever side we didn't come from
            if (train.frontLink != previousTrain) {
                previousTrain = train;
                train = train.frontLink;
            }
            else {
                previousTrain = train;
                train = train.backLink;
            }
        }
        // Both front and back didn't find anything, so there's no pulling locomotive.
        return 0;
    }

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

    public OverlayTextureManager getOverlayTextureContainer() {
        return overlayTextureContainer;
    }

    public boolean acceptsOverlayTextures() {
        return acceptsOverlayTextures;
    }





    /**
     * This function returns an ItemStack that represents this cart. This should
     * be an ItemStack that can be used by the player to place the cart. This is
     * the item that was registered with the cart via the registerMinecart
     * function, but is not necessary the item the cart drops when destroyed.
     *
     * @return An ItemStack that can be used to place the cart.
     */
    @Override
    public ItemStack getCartItem() {
        return new ItemStack(getItem());
    }

        /*
    <h1>Bogies and models</h1>
    */

    /**returns a list of models to be used for the bogies
     * example:
     * return new Bogie[]{new Bogie(new MyModel1(), offset), new Bogie(new MyModel2(), offset2), etc...};
     * may return null. */
    public Bogie[] bogies(){
        return null;
    }



    /**defines the scale to render the model at. Default is 0.0625*/
    public float[][] getRenderScale(){return new float[][]{register.render.getScale()};}

    /**defines the scale to render the model at. Default is 1*/
    public float getPlayerScale(){return 1f;}

    /**returns the x/y/z offset each model should render at, with 0 being the entity center, in order with getModels
     * example:
     * return new float[][]{{x1,y1,z1},{x2,y2,z2}, etc...};
     * may return null.*/
    @SideOnly(Side.CLIENT)
    public float[][] modelOffsets(){return new float[][]{register.render.getTrans()};}


    /**returns the x/y/z rotation each model should render at in degrees, in order with getModels
     * example:
     * return new float[][]{{x1,y1,z1},{x2,y2,z2}, etc...};
     * may return null.*/
    @SideOnly(Side.CLIENT)
    public float[][] modelRotations(){return new float[][]{register.render.getRotate()};}




    /**returns a list of models to be used for the transport
     * example:
     * return new MyModel();
     * may return null. */
    @SideOnly(Side.CLIENT)
    public ModelBase[] getModel(){return new ModelBase[]{register.render.getModel()};}

    public ArrayList<double[]> getSmokePosition() {return null;}

    public int[] getParticleData(int id) {
        switch (id){
            case 1: {return new int[]{1,200,0xFF0000};}
            default: {return new int[]{1,10,0xCCCC11};}
        }
    }

    public TrainSound getHorn(){
        if(getSounds() != null && !getSounds().getHornString().isEmpty()){
            return new TrainSound(getSounds().getHornString(),getSounds().getHornVolume(),1f, 0);
        }
        return null;
    }

    public TrainSound getBell(){
        return new TrainSound(Info.resourceLocation + ":bell",0.5f,1f, 0);
    }

    public TrainSound getRunningSound(){
        if(getSounds() != null && !getSounds().getRunString().isEmpty()){
            TrainSound sound = new TrainSound(getSounds().getRunString(), getSounds().getRunVolume(), 0.4f, getSounds().getRunSoundLength());
            if(getSounds().getSoundChangeWithSpeed()){
                sound.enableRunningPitch();
            }
            return sound;
        }
        return null;
    }

    public TrainSound getIdleSound(){
        if(getSounds() != null && !getSounds().getIdleString().isEmpty()){
            return new TrainSound(getSounds().getIdleString(),getSounds().getIdleVolume(),0.001F, getSounds().getIdleSoundLength());
        }
        return null;
    }

    public World getWorld(){ return worldObj;}
    @Override
    public World func_82194_d() {
        return getWorld();
    }

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) {return null;}

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {return null;}

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_) {return null;}

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {}

    // Do not override, use getCommandSenderName() instead
    @Override
    public final String getInventoryName() {return null; }

    @Override
    public int getInventoryStackLimit() {return 0;}

    @Override
    public void markDirty() {}

    public boolean isUseableByPlayer(EntityPlayer entityplayer) {return false; }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {return false;}


    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {return 0;}

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {return null;}

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {return null;}

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {return false;}

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {return false;}

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {return new FluidTankInfo[0];}
}