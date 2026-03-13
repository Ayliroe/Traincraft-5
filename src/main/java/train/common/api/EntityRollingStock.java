package train.common.api;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.entities.EntitySeat;
import ebf.tim.utility.CommonUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import train.client.core.handlers.SoundUpdaterRollingStock;
import train.common.Traincraft;
import train.common.adminbook.ServerLogger;
import train.common.api.components.Bogies;
import train.common.api.components.Hitboxes;
import train.common.api.components.Seats;
import train.common.core.handlers.ConfigHandler;
import train.common.entity.TrustedPlayer;
import train.common.items.ItemRollingStock;
import train.common.library.BlockIDs;
import train.common.library.TraincraftRegistry.TrainRegister;

import java.util.ArrayList;
import java.util.List;

import static train.common.core.util.TraincraftUtil.isRailBlockAt;

public abstract class EntityRollingStock extends AbstractTrains {

    // --- MAIN ---
    public Bogies bogies = new Bogies(this);
    public final Seats seats = new Seats(this);
    public Hitboxes hitbox = new Hitboxes(this);

    // --- PHYSICS ---
    private boolean firstLoad = true;
    private boolean derail = false;

    // --- AUDIO ---
    @SideOnly(Side.CLIENT)
    private SoundHandler theSoundManager;
    @SideOnly(Side.CLIENT)
    private SoundUpdaterRollingStock sndUpdater;

    // --- STATE ---
    public int fuelTrain = 0; // Note: Used by locos, but also b-units which derive from different classes, so this can't be moved higher-up

    /*
     * =========================================== INIT ===========================================
     **/


    public EntityRollingStock(World world) {
        super(world);
        dataWatcher.addObject(14, 0);
        dataWatcher.addObject(21, 0);

        preventEntitySpawning = true;
        isImmuneToFire = true;
        yOffset = 0;

        /* Railcraft's stuff */
        //maxSpeed = defaultMaxSpeedRail;
        //maxSpeedGround = defaultMaxSpeedGround;

        maxSpeedAirLateral = defaultMaxSpeedAirLateral;
        maxSpeedAirVertical = defaultMaxSpeedAirVertical;

        if (ConfigHandler.FLICKERING) {
            ignoreFrustumCheck = true;
        }

        if (Traincraft.proxy.isClient()) {
            sndUpdater = new SoundUpdaterRollingStock();
        }

        // Ignore default minecraft collisions
        setCollisionHandler(null);
    }

    @Override
    public void init(TrainRegister spec) {
        super.init(spec);
        bogies.init();
        seats.init(); // TODO: unduplicate these
        hitbox.init();
    }

    @Override
    protected void entityInit() {
        if(getWorld()!=null) {
            dataWatcher.addObject(16, (byte) 0);
            dataWatcher.addObject(17, 0);
            dataWatcher.addObject(18, 1);
            dataWatcher.addObject(19, 0.0F);
            dataWatcher.addObject(29, 0.0F);
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(getTrustedList().size());
        for (TrustedPlayer player : getTrustedList()) {
            ByteBufUtils.writeUTF8String(buffer, player.getDisplayName());
            buffer.writeBoolean(player.hasBreakAccess());
        }
        buffer.writeBoolean(acceptsOverlayTextures());
        if (acceptsOverlayTextures()) {
            ByteBufUtils.writeTag(buffer, getOverlayTextureContainer().getOverlayConfigTag());
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        super.readSpawnData(additionalData);
        int numOfTrustedPlayers = additionalData.readInt();
        for (int i = 0; i < numOfTrustedPlayers; i++) {
            getTrustedList().add(new TrustedPlayer(ByteBufUtils.readUTF8String(additionalData), additionalData.readBoolean()));
        }
        if (additionalData.readBoolean()) { // If accepts overlay textures...
            getOverlayTextureContainer().importFromConfigTag(ByteBufUtils.readTag(additionalData));
        }
    }

    /*
     * =========================================== NBT ===========================================
     **/

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setBoolean("firstLoad", firstLoad);
        bogies.writeEntityToNBT(nbttagcompound);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        firstLoad = nbttagcompound.getBoolean("firstLoad");
        bogies.init();
        seats.init();
        hitbox.init();
        bogies.readEntityFromNBT(nbttagcompound);
    }

    /*
     * =========================================== UPDATE ===========================================
     **/

    @Override
    public void onUpdate() {

        // --- CHUNKLOADING UUID ---
        if (!getWorld().isRemote && uniqueID == -1) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
                setNewUniqueID(getEntityId());
            }
        }
        shouldChunkLoad = getFlag(7);
        if (shouldChunkLoad) {
            if (chunkTicket == null) {
                requestTicket();
            }
        }

        // --- DAMAGE FALLOFF ---
        if (getRollingAmplitude() > 0) {
            setRollingAmplitude(getRollingAmplitude() - 1);
        }
        if (getDamage() > 0) {
            setDamage(getDamage() - 1);
        }

        updatePortal();

        if (Traincraft.proxy.isClient()) {
            soundUpdater();
        }

        seats.update();
        hitbox.update();
        bogies.update();

        if (getWorld().isRemote) {
            if(render_cache!=null && render_cache.bogies!=null){
                for (train.client.render.Bogie b : render_cache.bogies) {
                    if (b != null) {
                        b.updatePosition(this, null);
                        b.updateRotation(this);
                    }
                }
            }

            return;
        }

        else {
            updatePosition();

            links.restoreLinks();

            int floor_posX = MathHelper.floor_double(posX);
            int floor_posY = MathHelper.floor_double(posY);
            int floor_posZ = MathHelper.floor_double(posZ);

            if (getWorld().isAirBlock(floor_posX, floor_posY, floor_posZ))
                floor_posY--;
            else if (isRailBlockAt(getWorld(), floor_posX, floor_posY + 1, floor_posZ) || getWorld().getBlock(floor_posX, floor_posY + 1, floor_posZ) == BlockIDs.tcRail.block || getWorld().getBlock(floor_posX, floor_posY + 1, floor_posZ) == BlockIDs.tcRailGag.block)
                floor_posY++;

            func_145775_I();
            MinecraftForge.EVENT_BUS.post(new MinecartUpdateEvent(this, floor_posX, floor_posY, floor_posZ));

            dataWatcher.updateObject(14, (int) (motionX * 100));
            dataWatcher.updateObject(21, (int) (motionZ * 100));
            dataWatcher.updateObject(29, getVelocity());

            if (ConfigHandler.ENABLE_LOGGING && !getWorld().isRemote && ticksExisted % 120 == 0)
                ServerLogger.writeWagonToFolder(this);
        }
    }

    public void updatePosition() {
        // --- RAIL CHECKS ---
        // TODO this obviously doesn't work
        Block b = CommonUtil.getBlockAt(getWorld(),posX,posY,posZ);
        if (b instanceof BlockRailBase){
            derail= false;

            //do scaled rail boosting but keep it capped to the max velocity of the rail
            if (b == Blocks.golden_rail) {
                if ((((BlockRailBase) b).isPowered()) && getVelocity() < maxBoost(b)) {
                    float boost = CommonUtil.getMaxRailSpeed(getWorld(), (BlockRailBase) b, this, posX, posY, posZ) * 0.005f;
                    bogies.addVelocity(boost);
                }
            }
        } else {
            //set the derail state based on whether or not there's a valid rail block below.
            //later this will add more inherent support for 3rd party mods like ZnD, right now it's just vanilla/RC/TiM
            //derail= !CommonUtil.isTrack(getWorld(),posX,posY,posZ);
        }

        // --- DERAIL YAW CHANGES ---
        if(derail) {
            if(links.isFrontLinked() && links.isBackLinked()) {
                rotationYaw = CommonUtil.atan2degreesf(links.getBack().posZ - links.getFront().posZ, links.getBack().posX - links.getFront().posX);
            } else if (links.isBackLinked()) {
                rotationYaw = CommonUtil.atan2degreesf(links.getBack().posZ - posZ, links.getBack().posX - posX);
            } else if (links.isFrontLinked()) {
                rotationYaw = CommonUtil.atan2degreesf(posZ - links.getFront().posZ, posX - links.getFront().posX);
            }
        }

        // --- LINKS SPRING ---
        links.updateSpring();

        // --- DRAG ---
        applyDrag();
    }

    @Override
    protected void applyDrag() {
        float drag = 0.95f; float derailDrag = 0.175f; float lateralDrag = 0.15f;
        //If an active loco is linked, don't apply a constant drag
        if (links.isActiveLocoLinked())
            drag = 1f;

        // --- SLOPE ACCELERATION ---
        if(ConfigHandler.ENABLE_SLOPE_ACCELERATION) {
            if (Math.abs(rotationPitch) - 1 > 0) { //cap the pitch that we actually consider to be on a slope
                //vanilla uses 0.0078125 per tick for slope speed.
                //0.00017361 would be that divided by 45 since vanilla slopes are 45 degree angles. Clamp the pitch so vanilla 45s don't cause enormous speedup.
                //scale by entity pitch, it's backwards here for some reason, idk.
                float clampedPitch = Math.max(-7.5f, Math.min(7.5f, rotationPitch));
                bogies.addVelocity((0.00017361) * -clampedPitch);
            }
        }

        // --- DERAIL DRAG ---
        // Sum up off-rail bogies and scale based on slipperiness of block below
        float bogiesOffRail = derail ? 1f : bogies.isDerailed();
        if(bogiesOffRail > 0) {
            drag *= 1 - bogiesOffRail * derailDrag * (1 - CommonUtil.getBlockAt(getWorld(),posX,posY,posZ).slipperiness);
        }

        // --- LATERAL FRICTION DRAG ---
        // If you do both at the same time then it's way too much.
        // TODO fix this
        else if (false) {
            drag -= lateralDrag * 0 * 4.448f; //we don't know what 4.448 does
        }

        //cap the drag to prevent weird behavior.
        // if it goes to 1 or higher then we speed up, which is bad, if it's below 0 we reverse, which is also bad
        drag = Math.max(0, Math.min(0.9999f, drag));

        bogies.multiplyVelocity(drag);
    }

    public void updatePortal() {
        if (!getWorld().isRemote && getWorld() instanceof WorldServer) {
            getWorld().theProfiler.startSection("portal");
            MinecraftServer var1 = MinecraftServer.getServer();
            int var2 = getMaxInPortalTime();

            if (inPortal) {
                if (var1.getAllowNether()) {
                    if (ridingEntity == null && portalCounter++ >= var2) {
                        portalCounter = var2;
                        timeUntilPortal = getPortalCooldown();
                        byte var3;

                        if (getWorld().provider.dimensionId == -1) {
                            var3 = 0;
                        } else {
                            var3 = -1;
                        }

                        travelToDimension(var3);
                    }

                    inPortal = false;
                }
            } else {
                if (portalCounter > 0) {
                    portalCounter -= 4;
                }

                if (portalCounter < 0) {
                    portalCounter = 0;
                }
            }

            if (timeUntilPortal > 0) {
                --timeUntilPortal;
            }

            getWorld().theProfiler.endSection();
        }
    }

    /*
     * =========================================== PHYSICS ===========================================
     **/

    @Override
    public void setVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        bogies.setVelocity(p_70024_1_, p_70024_3_, p_70024_5_);
    }

    @Override
    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        bogies.addVelocity(p_70024_1_, p_70024_3_, p_70024_5_);
    }

    public float getVelocity(){ return getWorld().isRemote ? dataWatcher.getWatchableObjectFloat(29): (float)(Math.abs(motionX)+Math.abs(motionZ)); }
    double maxBoost(Block booster){
        if(this instanceof Locomotive && ((Locomotive)this).getMaxSpeed() > 0){
            return Math.min(((Locomotive)this).getMaxSpeed(),
                    CommonUtil.getMaxRailSpeed(getWorld(), (BlockRailBase) booster,this, posX,posY,posZ));
        }
        return CommonUtil.getMaxRailSpeed(getWorld(), (BlockRailBase) booster,this, posX,posY,posZ);
    }

    // Dragonparts
    @Override
    public Entity[] getParts() { return hitbox.getParts(); }

    /*
     * =========================================== DAMAGE ===========================================
     **/

    @Override
    public boolean attackEntityFromPart(EntityDragonPart part, DamageSource damagesource, float i) {
        return attackEntityFrom(damagesource,i);
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        if (!getWorld().isRemote && !isDead && TrainUtils.canBeAttackedBySource(this, damagesource)) {
            EntityPlayer player = (EntityPlayer)damagesource.getEntity(); // canBeAttackedBySource guarantees this is valid

            setRollingDirection(-getRollingDirection());
            setRollingAmplitude(10);
            setBeenAttacked();
            if (player.capabilities.isCreativeMode) {
                setDamage(1000);
                if (ConfigHandler.ENABLE_WAGON_REMOVAL_NOTICES && player.canCommandSenderUseCommand(2, "")) {
                    player.addChatComponentMessage(new ChatComponentText("Operator removed train owned by " + getTrainOwner()));
                }
            }
            setDamage(getDamage() + i * 10);
            if (getDamage() > 40) {
                //TODO: check the seats instead
                if (riddenByEntity != null) {
                    riddenByEntity.mountEntity(this);
                }
                ServerLogger.deleteWagon(this);
                setDead();
                dropCartAsItem(player.capabilities.isCreativeMode);
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {
        setRollingDirection(-getRollingDirection());
        setRollingAmplitude(10);
        setDamage(getDamage() + getDamage() * 10);
    }

    @Override
    public void setDead() {
        super.setDead();
        links.unlink();
        bogies.setDead();
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            soundUpdater();
        }

        seats.setDead();
        hitbox.setDead();
    }

    /*
     * =========================================== INTERACTION ===========================================
     **/

    // Prevent interactions if locked + not a trusted user + cannot be ridden while locked
    public boolean isLockedAndNotOwner(EntityPlayer player) {
        if (getTrainLockedFromPacket()) {
            if(getOwner() != player.getGameProfile() && !isPlayerTrusted(player.getDisplayName()) && !canBeRiddenWhileLocked() && !(player.capabilities.isCreativeMode && player.canCommandSenderUseCommand(2, ""))) {
                player.addChatMessage(new ChatComponentText("Train is locked by " + getTrainOwner() + "."));
                return true;
            }
        }
        return false;
    }
    /**
     * The actions to perform on key press
     * @return Returns true if the action isn't allowed or has been 'eaten'
     */
    public boolean keyHandlerFromPacket(int i, EntityPlayer player) {
        if (isLockedAndNotOwner(player))
            return true;
        return TrainUtils.onOpeningGUI(this, i, player);
    }

    /**
     * The action to perform when interacted with
     * This can only be called from the server, if on client use Traincraft.keyChannel.sendToServer(new PacketInteract(host.getEntityId()));
     */
    @Override
    public boolean interactFirst(EntityPlayer entityplayer) {
        if (!getWorld().isRemote) {
            // Prevent interactions if we are mounted on a seat
            if (entityplayer.ridingEntity instanceof EntitySeat)                      { return true; }

            // --- LOCKED CARTS ---
            if (isLockedAndNotOwner(entityplayer))                                    { return true; }

            // --- ITEM IN HAND ---
            ItemStack itemstack = entityplayer.inventory.getCurrentItem();
            if(itemstack != null) {
                if (TrainUtils.onClickWithChunkloader(this, itemstack, entityplayer)) { return true; }
                if (TrainUtils.onClickWithWrench(this, itemstack, entityplayer))      { return true; }
                if (TrainUtils.onClickWithCrowbar(this, itemstack, entityplayer))     { return false; }
                if (TrainUtils.onClickWithTicket(this, itemstack, entityplayer))      { return true; }
                if (TrainUtils.onClickWithDye(this, itemstack, entityplayer))         { return true; }
                if (TrainUtils.onClickWithStake(this, itemstack, entityplayer))       { return true; }
                if (TrainUtils.onClickWithPaintbrush(this, itemstack, entityplayer))  { return true; }
                if (TrainUtils.onClickWithPadlock(this, itemstack, entityplayer))     { return true; }
            }

            // --- ENTERING SEAT ---
            if (seats.onEnteringSeat(entityplayer))                                   { return true; }

            // --- INVENTORY GUI ---
            if (TrainUtils.onOpeningInventory(this, entityplayer))                    { return true; }

            if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, entityplayer))) { return true; }
        }
        return false;
    }

    @Override
    public List<ItemStack> getItemsDropped() {
        List<ItemStack> items = new ArrayList<ItemStack>();

        items.add(ItemRollingStock.setPersistentData(new ItemStack(getItem()), this, getUniqueTrainID(), trainCreator, trainOwner, getSkin()));
        return items;
    }

    public ItemStack[] getInventory() { return null; }

    /*
     * =========================================== AUDIO ===========================================
     **/

    @SideOnly(Side.CLIENT)
    private void soundUpdater() {
        if(ticksExisted>0) {
            if (FMLClientHandler.instance().getClient() != null) {
                theSoundManager = FMLClientHandler.instance().getClient().getSoundHandler();
            }
            if (FMLClientHandler.instance().getClient() != null && theSoundManager != null && FMLClientHandler.instance().getClient().thePlayer != null) {
                if (sndUpdater != null) {
                    sndUpdater.update(FMLClientHandler.instance().getClient().getSoundHandler(), this, FMLClientHandler.instance().getClient().thePlayer);
                }
            }
        }
    }

    // Used in SoundUpdaterRollingStock
    public int getMotionXClient() { return dataWatcher.getWatchableObjectInt(14); }
    public int getMotionZClient() { return dataWatcher.getWatchableObjectInt(21); }
}