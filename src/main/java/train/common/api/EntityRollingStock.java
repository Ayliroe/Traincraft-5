package train.common.api;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.entities.EntitySeat;
import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.Vec3f;
import io.netty.buffer.ByteBuf;
import mods.railcraft.api.carts.CartTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
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
import train.common.core.handlers.ConfigHandler;
import train.common.core.handlers.FuelHandler;
import train.common.core.network.PacketRollingStockRotation;
import train.common.entity.CollisionBox;
import train.common.entity.EntityHitbox;
import train.common.entity.TrustedPlayer;
import train.common.items.ItemRollingStock;
import train.common.library.BlockIDs;

import java.util.ArrayList;
import java.util.List;

import static train.common.core.util.TraincraftUtil.isRailBlockAt;

public abstract class EntityRollingStock extends AbstractTrains {

    public int fuelTrain = 0; // Note: Used by locos, but also b-units which derive from different classes, so this can't be moved higher-up

    protected static final int[][][] matrix = {
            {{0, 0, -1}, {0, 0, 1}},
            {{-1, 0, 0}, {1, 0, 0}},
            {{-1, -1, 0}, {1, 0, 0}},
            {{-1, 0, 0}, {1, -1, 0}},
            {{0, 0, -1}, {0, -1, 1}},
            {{0, -1, -1}, {0, 0, 1}},
            {{0, 0, 1}, {1, 0, 0}},
            {{0, 0, 1}, {-1, 0, 0}},
            {{0, 0, -1}, {-1, 0, 0}},
            {{0, 0, -1}, {1, 0, 0}}};


    /**
     * appears to be the progress of the turn
     */
    private int rollingturnProgress;

    public EntityHitbox collisionHandler=null;

    public int linkageNumber;

    @SideOnly(Side.CLIENT)
    private SoundHandler theSoundManager;
    @SideOnly(Side.CLIENT)
    private SoundUpdaterRollingStock sndUpdater;

    /**
     * New physics integration
     */
    private boolean firstLoad = true;
    private boolean hasSpawnedBogie = false;
    private boolean derail = false;

    public Vec3f[] cachedVectors = new Vec3f[]{ new Vec3f(0,0,0),new Vec3f(0,0,0),new Vec3f(0,0,0),new Vec3f(0,0,0) };

    public EntityRollingStock(World world) {
        super(world);
        dataWatcher.addObject(14, 0);
        dataWatcher.addObject(21, 0);

        preventEntitySpawning = true;
        isImmuneToFire = true;
        setSize(0.25f,0.25f);
        yOffset = 0;
        linkageNumber = 0;
        entityCollisionReduction = 0.8F;

        consist = new ArrayList<AbstractTrains>();
        consist.add(this);
        updateLinks();

        collisionHandler=new EntityHitbox(this);

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

        setCollisionHandler(null);
    }

    public GameProfile getOwner() {
        return CartTools.getCartOwner(this);
    }


    public Entity[] getParts(){
        return collisionHandler==null || collisionHandler.interactionBoxes==null?null:
                collisionHandler.interactionBoxes.toArray(new Entity[]{});
    }

    /**
     * <p>This method is called on the client side when an entity is being loaded in. The additionalData buffer is sent from the server
     * and is populated by the server using the writeSpawnData method.</p>
     * <br></br><p>"this is basically NBT for entity spawn, to keep data between client and server in sync because some data is not automatically shared."</p>
     * @param additionalData The packet data stream
     */
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

    /**
     * <p>This method is called on the server side when a connected client is loading the entity. Data written
     * to the ByteBuffer will be synced with the client and available to the client through the readSpawnData method.</p>
     * <br></br><p>"this is basically NBT for entity spawn, to keep data between client and server in sync because some data is not automatically shared."</p>
     * @param buffer The packet data stream
     */
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
    public double getMountedYOffset() {
        return 0;
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
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return null;
    }

    protected int steamFuelLast(ItemStack it) {
        return FuelHandler.steamFuelLast(it);
    }

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

    public void unLink() {
        if (isAttached) {
            if (frontLink != null) {
                if (frontLink.Link1 == uniqueID) {
                    frontLink.Link1 = 0;
                    frontLink.frontLink = null;
                    if (frontLink.consist != null){
                        frontLink.consist.clear();
                        frontLink.consist.add(frontLink);
                        frontLink.updateLinks();
                    }

                } else if (frontLink.Link2 == uniqueID) {
                    frontLink.Link2 = 0;
                    frontLink.backLink = null;
                    if (frontLink.consist != null){
                        frontLink.consist.clear();
                        frontLink.consist.add(frontLink);
                        frontLink.updateLinks();
                    }

                }
            }
            if (backLink != null) {
                if (backLink.Link1 == uniqueID) {
                    backLink.Link1 = 0;
                    backLink.frontLink = null;
                    if (backLink.consist != null){
                        backLink.consist.clear();
                        backLink.consist.add(backLink);
                        frontLink.updateLinks();
                    }

                } else if (backLink.Link2 == uniqueID) {
                    backLink.Link2 = 0;
                    backLink.backLink = null;
                    if (backLink.consist != null){
                        backLink.consist.clear();
                        backLink.consist.add(backLink);
                        frontLink.updateLinks();
                    }

                }
            }
            frontLink = null;
            backLink = null;
            isAttached = false;
            updateLinks();
        }
    }

    @Override
    public void setDead() {
        super.setDead();
        unLink();
        if (bogieFront != null) {
            bogieFront.setDead();
        }
        if (bogieBack != null) {
            bogieBack.setDead();
        }
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            soundUpdater();
        }
        //remove seats
        for (EntitySeat seat : seats) {
            seat.setDead();
            seat.getWorld().removeEntity(seat);
        }

        for(CollisionBox box : collisionHandler.interactionBoxes){
            if(box !=null){
                box.setDead();
                getWorld().removeEntity(box);
            }
        }
    }

    /**
     * gets packet from server and distribute for GUI handles motion
     *
     * @param
     */
    public boolean isLockedAndNotOwner(EntityPlayer player) {
        if (getTrainLockedFromPacket()) {
            return !player.getDisplayName().equalsIgnoreCase(getTrainOwner()) && !isPlayerTrusted(player.getDisplayName());
        }
        return false;
    }
    /**
     * The actions to perform on key press
     * @return Returns true if the action isn't allowed or has been 'eaten'
     */
    public boolean keyHandlerFromPacket(int i, EntityPlayer player) {
        if (getTrainLockedFromPacket() && isLockedAndNotOwner(player))
            return true;
        return TrainUtils.onOpeningGUI(this, i, player);
    }   

    private double rollingX=0,rollingY=0,rollingZ=0;
    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9) {
        rollingX = par1;
        rollingY = par3;
        rollingZ = par5;
        rollingturnProgress = par9 + 2;
    }

    @Override
    public void onUpdate() {

        // --- SPAWN BOGIES ---
        if (addedToChunk && !hasSpawnedBogie) {
            if (bogieFront == null) {
                double[] offset=CommonUtil.rotatePoint(rotationPoints()[0], 0,180+rotationYaw);
                bogieFront = new EntityBogie(getWorld(),offset[0]+posX,posY,offset[2]+posZ, this);

                offset=CommonUtil.rotatePoint(rotationPoints()[1], 0,180+rotationYaw);
                bogieBack = new EntityBogie(getWorld(),offset[0]+posX,posY,offset[2]+posZ, this);

                getWorld().spawnEntityInWorld(bogieBack);
                getWorld().spawnEntityInWorld(bogieFront);
            }
            hasSpawnedBogie = true;
        }

        // --- CHUNKLOADING UNIQUEID ---
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

        // --- PLAYER INVINCIBILITY ---
        //just so we aren't doing it *every* tick, but still frequent enough to not let the player actually take damage
        if(ticksExisted % 18 == 0) {
            if (!seats.isEmpty()) {
                for (EntitySeat seat : seats) {
                    if (seat.getPassenger() != null) {
                        seat.getPassenger().addPotionEffect(new PotionEffect(Potion.resistance.id, 20, 5, true));
                    }
                }
            }
        }

        // --- IGNORE MINECART DAMAGE ---
        if (getRollingAmplitude() > 0) {
            setRollingAmplitude(getRollingAmplitude() - 1);
        }
        if (getDamage() > 0) {
            setDamage(getDamage() - 1);
        }

        if (getRiderOffsets() != null && getRiderOffsets().length > 0 && seats.size() < getRiderOffsets().length) {
            for (int i = 0; i < getRiderOffsets().length; i++) {
                EntitySeat seat = new EntitySeat(getWorld(), posX, posY, posZ, getRiderOffsets()[i][0], getRiderOffsets()[i][1] + 2, getRiderOffsets()[i][2], this, i);
                seats.add(seat);
                if (i == 0) {
                    seats.get(i).setControlSeat();
                }
                getWorld().spawnEntityInWorld(seats.get(i));
            }
        }

        // --- PORTAL ---
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

        if (Traincraft.proxy.isClient()) {
            soundUpdater();
        }

        if (getWorld().isRemote) {
            if (rollingturnProgress > 0) {
                setPosition(posX + (rollingX - posX) / (double)rollingturnProgress,
                        posY + (rollingY - posY) / (double)rollingturnProgress,
                        posZ + (rollingZ - posZ) / (double)rollingturnProgress);
                --rollingturnProgress;

                if(bogieFront!=null && bogieBack !=null){
                    posY=(bogieFront.posY+bogieBack.posY)*0.5;
                    double d6 = bogieBack.posX - bogieFront.posX;
                    double d7 = bogieBack.posZ - bogieFront.posZ;
                    rotationPitch = CommonUtil.atan2degreesf(bogieFront.posY - bogieBack.posY, Math.sqrt(d6 * d6 + d7 * d7));
                }
            } else {
                setPosition(posX, posY, posZ);

            }

            if(render_cache!=null && render_cache.bogies!=null){
                for (train.client.render.Bogie b : render_cache.bogies) {
                    if (b != null) {
                        b.updatePosition(this, null);
                        b.updateRotation(this);
                    }
                }
            }

            collisionHandler.position(posX, posY, posZ, rotationPitch, rotationYaw);
            collisionHandler.updateCollidingEntities(this);
            collisionHandler.manageCollision();
            positionSeats();
            return;
        }

        restoreLinks();

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        int floor_posX = MathHelper.floor_double(posX);
        int floor_posY = MathHelper.floor_double(posY);
        int floor_posZ = MathHelper.floor_double(posZ);

        if (getWorld().isAirBlock(floor_posX, floor_posY, floor_posZ)) {
            floor_posY--;
        } else if (isRailBlockAt(getWorld(), floor_posX, floor_posY + 1, floor_posZ) || getWorld().getBlock(floor_posX, floor_posY + 1, floor_posZ) == BlockIDs.tcRail.block || getWorld().getBlock(floor_posX, floor_posY + 1, floor_posZ) == BlockIDs.tcRailGag.block) {
            floor_posY++;
        }

        updatePosition();

        if (bogieFront != null && bogieBack!=null) {

            double d6 = bogieBack.posX - bogieFront.posX;
            double d7 = bogieBack.posZ - bogieFront.posZ;
            prevRotationYaw = rotationYaw;

            rotationYaw = CommonUtil.atan2degreesf(d7, d6);
            rotationPitch = CommonUtil.atan2degreesf(bogieFront.posY - bogieBack.posY, Math.sqrt(d6 * d6 + d7 * d7));
        }


        if (!getWorld().isRemote && ticksExisted % 2 == 0) {
            Traincraft.rotationChannel.sendToAllAround(new PacketRollingStockRotation(this), new TargetPoint(getWorld().provider.dimensionId, posX, posY, posZ, 300.0D));
        }


        func_145775_I();
        MinecraftForge.EVENT_BUS.post(new MinecartUpdateEvent(this, floor_posX, floor_posY, floor_posZ));

        //update the collision handler's positions
        collisionHandler.position(posX, posY, posZ, rotationPitch, rotationYaw);
        collisionHandler.updateCollidingEntities(this);
        collisionHandler.manageCollision();
        for (EntitySeat seat: seats) { //handle died in train
            if (seat.getPassenger() != null && (seat.getPassenger().isDead || seat != seat.getPassenger().ridingEntity)) {
                seat.getPassenger().ridingEntity = null;
                seat.removePassenger(seat.getPassenger());
            }
        }
        dataWatcher.updateObject(14, (int) (motionX * 100));
        dataWatcher.updateObject(21, (int) (motionZ * 100));
        positionSeats();
        if (ConfigHandler.ENABLE_LOGGING && !getWorld().isRemote && ticksExisted % 120 == 0) {
            ServerLogger.writeWagonToFolder(this);
        }
        if(!getWorld().isRemote) {
            dataWatcher.updateObject(29, getVelocity());
        }
    }

    /**
     * As entities can't be registered in nbttagcompound I had to setup this
     * system... When world loads, only the (double) Link1 and Link2 are
     * known. This method search for the entity with the ID corresponding to
     * Link1 or Link2 When it finds it, (EntityRollingStock)frontLink and
     * backLink will be updated accordingly
     */
    private void restoreLinks(){

        if (addedToChunk && ((frontLink == null && Link1 != 0) || (backLink == null && Link2 != 0))) {
            List<?> list = getWorld().getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(15, 15, 15));

            if (list != null && !list.isEmpty()) {
                for (Object entity : list) {
                    if (entity instanceof EntityRollingStock) {
                        if (((EntityRollingStock) entity).uniqueID == Link1) {
                            frontLink = (EntityRollingStock) entity;
                        } else if (((EntityRollingStock) entity).uniqueID == Link2) {
                            backLink = (EntityRollingStock) entity;
                        }
                    }
                }
            }
        }
    }

    private void positionSeats(){
        //rider updating isn't called if there's no driver/conductor, so just in case of that, we reposition the seats here too.
        if (getRiderOffsets() != null) {
            for (int i1 = 0; i1 < seats.size(); i1++) {
                //sometimes seats die when players log out. make new ones.
                if(seats.get(i1) == null){
                    seats.set(i1, new EntitySeat(getWorld(), posX, posY,posZ,0,0,0, this,i1));
                    if(i1==0){
                        seats.get(i1).setControlSeat();
                    }
                    getWorld().spawnEntityInWorld(seats.get(i1));
                }
                cachedVectors[0] = new Vec3f(getRiderOffsets()[i1][0], getRiderOffsets()[i1][1], getRiderOffsets()[i1][2])
                        .rotatePoint(rotationPitch, 180+rotationYaw, 0f);
                cachedVectors[0].addVector(posX,posY,posZ);
                seats.get(i1).setPosition(cachedVectors[0].xCoord, cachedVectors[0].yCoord, cachedVectors[0].zCoord);
            }
        }
    }

    public void updatePosition(){
        if(!getWorld().isRemote) {

            // --- RAIL CHECKS ---
            Block b = CommonUtil.getBlockAt(getWorld(),posX,posY,posZ);
            if (b instanceof BlockRailBase){
                derail= false;

                //do scaled rail boosting but keep it capped to the max velocity of the rail
                if (b == Blocks.golden_rail) {
                    if ((((BlockRailBase) b).isPowered()) &&
                            //this part keeps it capped
                            getVelocity() < maxBoost(b)) {
                        float boost = CommonUtil.getMaxRailSpeed(getWorld(), (BlockRailBase) b, this, posX, posY, posZ) * 0.005f;
                        appendMovement(Math.copySign(cachedVectors[2].yCoord,boost));
                    }
                }
            } else {
                //set the derail state based on whether or not there's a valid rail block below.
                //later this will add more inherent support for 3rd party mods like ZnD, right now it's just vanilla/RC/TiM
                //derail= !CommonUtil.isTrack(getWorld(),posX,posY,posZ);
            }

            // --- DERAIL YAW CHANGES ---
            if(derail) {
                if(frontLink instanceof EntityRollingStock &&
                        backLink instanceof EntityRollingStock){
                    rotationYaw=CommonUtil.atan2degreesf(
                            backLink.posZ - frontLink.posZ,
                            backLink.posX - frontLink.posX);
                } else if (backLink instanceof EntityRollingStock){
                    rotationYaw=CommonUtil.atan2degreesf(
                            backLink.posZ - posZ,
                            backLink.posX - posX);
                } else if (frontLink instanceof EntityRollingStock){
                    rotationYaw=CommonUtil.atan2degreesf(
                            posZ - frontLink.posZ,
                            posX - frontLink.posX);
                }
            }

            // --- LINKS SPRING ---
            double activeSpring = 0.5d; double passiveSpring = 0.25d;
            double springDist = 0d;
            int pullingDir = pullingLocomotiveDirection();
            if (frontLink instanceof EntityRollingStock) {
                springDist += manageLink((EntityRollingStock) frontLink) * (pullingDir == 1 ? activeSpring : (pullingDir == -1 ? 0 : passiveSpring));
            }
            if (backLink instanceof EntityRollingStock) {
                springDist -= manageLink((EntityRollingStock) backLink) * (pullingDir == -1 ? activeSpring : (pullingDir == 1 ? 0 : passiveSpring));
            }
            // Non-null springDist means this stock is allowed to be pulled and an active link is pulling or pushing it
            if (springDist != 0d) {
                setVelocity(springDist);
            }

            // --- DRAG ---
            applyDrag();

            // --- POSITION ---
            cachedVectors[1] = new Vec3f(rotationPoints()[1], 0, 0).rotatePoint(0, rotationYaw, 0)
                    .addVector(bogieBack.posX,0,bogieBack.posZ);
            setPosition(cachedVectors[1].xCoord, (bogieBack.posY+bogieFront.posY)*0.5,cachedVectors[1].zCoord);

            // --- BOGIES ---
            bogieFront.minecartMove(this);
            bogieBack.minecartMove(this);

            // --- ROTATION ---
            setRotation((CommonUtil.atan2degreesf(
                            bogieBack.posZ - bogieFront.posZ,
                            bogieBack.posX - bogieFront.posX)),
                    CommonUtil.calculatePitch(bogieFront.posY, bogieBack.posY , Math.abs(rotationPoints()[0]) + Math.abs(rotationPoints()[1])));

            //reset the vector when we're done so it wont break trains.
            cachedVectors[1]= new Vec3f(0,0,0);

            // --- COLLISION ---
            if(collisionHandler==null) {
                collisionHandler = new EntityHitbox(this);
                collisionHandler.position(posX, posY, posZ, rotationPitch, rotationYaw);
            } else {
                collisionHandler.position(posX, posY, posZ, rotationPitch, rotationYaw);
            }
        }
    }

    public void setVelocity(double velocity) {
        if (bogieBack == null || bogieFront == null) { return; }
        bogieBack.setVelocity(this, velocity);
        bogieFront.setVelocity(this, velocity);
    }

    public void appendMovement(double velocity) {
        if (bogieBack == null || bogieFront == null) { return; }
        bogieBack.addVelocity(this, velocity);
        bogieFront.addVelocity(this, velocity);
    }

    @Override
    public void setVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        if (bogieBack == null || bogieFront == null) { return; }
        double velocity = Math.sqrt(Math.pow(p_70024_1_,2)+Math.pow(p_70024_5_,2));
        bogieBack.setVelocity(this, velocity);
        bogieFront.setVelocity(this, velocity);
    }

    @Override
    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        if (bogieBack == null || bogieFront == null) { return; }
        double velocity = Math.sqrt(Math.pow(p_70024_1_,2)+Math.pow(p_70024_5_,2));
        bogieBack.addVelocity(this, velocity);
        bogieFront.addVelocity(this, velocity);
    }

    public double manageLink(EntityRollingStock other) {
        // Don't apply spring movement if uninitialized or a non-passive loco
        if (other.bogieBack == null || other.bogieFront == null || bogieBack == null || bogieFront == null || (this instanceof Locomotive && !canBePushed())) {
            return 0d;
        }

        double vecX = other.posX - posX;
        double vecZ = other.posZ - posZ;

        return MathHelper.sqrt_double(vecX * vecX + vecZ * vecZ) - (getOptimalDistance()+other.getOptimalDistance());
    }

    @Override
    protected void applyDrag() {
        float drag = 0.95f; float derailDrag = 0.175f; float lateralDrag = 0.15f;
        //If an active loco is linked, don't apply a constant drag
        for(AbstractTrains stock : consist) {
            if(stock instanceof Locomotive && ((Locomotive)stock).isLocoTurnedOn && !stock.canBePushed()){
                drag = 1f;
                break;
            }
        }

        // --- SLOPE ACCELERATION ---
        if(ConfigHandler.ENABLE_SLOPE_ACCELERATION) {
            if (Math.abs(rotationPitch) - 1 > 0) { //cap the pitch that we actually consider to be on a slope
                //vanilla uses 0.0078125 per tick for slope speed.
                //0.00017361 would be that divided by 45 since vanilla slopes are 45 degree angles. Clamp the pitch so vanilla 45s don't cause enormous speedup.
                //scale by entity pitch, it's backwards here for some reason, idk.
                float clampedPitch = Math.max(-7.5f, Math.min(7.5f, rotationPitch));
                appendMovement((0.00017361) * -clampedPitch);
            }
        }

        // --- DERAIL DRAG ---
        // Sum up off-rail bogies and scale based on slipperiness of block below
        float bogiesOffRail = derail ? 1f : (bogieBack.isOnRail?0f:0.5f) + (bogieFront.isOnRail?0f:0.5f);
        if(bogiesOffRail > 0) {
            drag *= 1 - bogiesOffRail * derailDrag * (1 - CommonUtil.getBlockAt(getWorld(),posX,posY,posZ).slipperiness);
        }

        // --- LATERAL FRICTION DRAG ---
        // If you do both at the same time then it's way too much.
        else if (cachedVectors[2].yCoord > 0) {
            drag -= lateralDrag * cachedVectors[2].yCoord * 4.448f; //we don't know what 4.448 does
        }

        //cap the drag to prevent weird behavior.
        // if it goes to 1 or higher then we speed up, which is bad, if it's below 0 we reverse, which is also bad
        drag = Math.max(0, Math.min(0.9999f, drag));

        bogieFront.multiplyVelocity(drag);
        bogieBack.multiplyVelocity(drag);
    }

    public float getVelocity(){
        return getWorld().isRemote?dataWatcher.getWatchableObjectFloat(29):
                (float)(Math.abs(motionX)+Math.abs(motionZ));
    }
    double maxBoost(Block booster){
        if(this instanceof Locomotive && ((Locomotive)this).getMaxSpeed() > 0){
            return Math.min(((Locomotive)this).getMaxSpeed(),
                    CommonUtil.getMaxRailSpeed(getWorld(), (BlockRailBase) booster,this, posX,posY,posZ));
        }
        return CommonUtil.getMaxRailSpeed(getWorld(), (BlockRailBase) booster,this, posX,posY,posZ);
    }
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);

        nbttagcompound.setBoolean("firstLoad", firstLoad);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);

        firstLoad = nbttagcompound.getBoolean("firstLoad");
    }

    @Override
    public boolean interactFirst(EntityPlayer entityplayer) {
        if (super.interactFirst(entityplayer))                                    { return true; }

        // Prevent interactions if we are mounted on a seat
        if (entityplayer.ridingEntity instanceof EntitySeat)                      { return true; }

        // --- LOCKED CARTS ---
        // Prevent interactions if locked + not a trusted user + cannot be ridden while locked
        if (!getWorld().isRemote && getTrainLockedFromPacket()) {
            boolean isTrustedPlayer = isPlayerTrusted(entityplayer.getDisplayName());
            if (!entityplayer.getDisplayName().equalsIgnoreCase(getTrainOwner()) && !isTrustedPlayer && !canBeRiddenWhileLocked(this)) {
                if (!getWorld().isRemote)
                    entityplayer.addChatMessage(new ChatComponentText("Train is locked by " + getTrainOwner() + "."));
                return true;
            }
        }

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
        }

        // --- ENTERING SEAT ---
        //be sure the player has permission to enter the transport, and that the transport has the main seat open.
        if (getRiderOffsets() != null && getPermissions(entityplayer, false) && !entityplayer.isSneaking()) {
            for (EntitySeat seat : seats) {
                //1.12 is stupid, sometimes when the passenger is null, it returns the player
                if (!getWorld().isRemote && (seat.getPassenger() == null
                        || seat.getPassenger().getEntityId()==entityplayer.getEntityId())) {
                    seat.addPassenger(entityplayer);
                    entityplayer.mountEntity(seat);
                    return true;
                }
            }
        }

        // --- INVENTORY GUI ---
        if (TrainUtils.onOpeningInventory(this, entityplayer))                    { return true; }


        if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, entityplayer))) {
            return true;
        }

        return getWorld().isRemote;
    }

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

    /**
     * Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity
     */
    @Override
    public void applyEntityCollision(Entity par1Entity) {}

    public void multiplyVelocity(double vel) {
        if (bogieBack == null || bogieFront == null) { return; } //This method can fire before the stock fully initializes, so we need to make sure bogies exist.
        bogieBack.multiplyVelocity(vel);
        bogieFront.multiplyVelocity(vel);
    }

    @Override
    public boolean isLinked() {return frontLink !=null || backLink!=null;}


    /*
     * =========================================== VANILLA OVERRIDES ===========================================
     **/

    /**
     * Return false if this cart should not call IRail.onMinecartPass() and should ignore Powered Rails.
     * @return True if this cart should call IRail.onMinecartPass().
     */
    @Override
    public boolean shouldDoRailFunctions() { return true; }

    @Override
    public void moveMinecartOnRail(int i, int j, int k, double d) {}
    @Override
    public int getMinecartType() { return 0; }

    /**
     * Used in SoundUpdaterRollingStock
     */
    public int getMotionXClient() {
        return dataWatcher.getWatchableObjectInt(14);
    }

    /**
     * Used in SoundUpdaterRollingStock
     */
    public int getMotionZClient() {
        return dataWatcher.getWatchableObjectInt(21);
    }

    @Override
    public List<ItemStack> getItemsDropped() {
        List<ItemStack> items = new ArrayList<ItemStack>();

        items.add(ItemRollingStock.setPersistentData(new ItemStack(getItem()), this, getUniqueTrainID(), trainCreator, trainOwner, getColor()));
        return items;
    }


    public ItemStack[] getInventory() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void setSeats(EntitySeat seat, int seatNumber){
        if (seats.size() < seatNumber || seats.isEmpty()) { //there is a case where seatNumber == 0 so seats.size() was always ==.
            seats.add(seat);
        } else {
            seats.set(seatNumber, seat);
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) { return true; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {return !isDead && entityplayer.getDistanceSqToEntity(this) <= 64D; }

    /**
     * <h2>Permissions handler</h2>
     * Used to check if the player has permission to do whatever it is the player is trying to do. Yes I could be more vague with that.
     *
     * @param player the player attenpting to interact.
     * @param driverOnly can this action only be done by the driver/conductor?
     * @return if the player has permission to continue
     */
    public boolean getPermissions(EntityPlayer player, boolean driverOnly) {
        //make sure the player is not null, and be sure that driver only rules are applied.
        if (player ==null) {
            return false;
        } else if (driverOnly && (!(player.ridingEntity instanceof EntitySeat) || ! ((EntitySeat) player.ridingEntity).isControlSeat())){
            return false;
        }

        //be sure operators and owners can do whatever
        if ((player.capabilities.isCreativeMode && player.canCommandSenderUseCommand(2, ""))
                || (getOwner()!=null && getOwner() == player.getGameProfile())
                || isPlayerTrusted(player.getDisplayName())
                || canBeRiddenWhileLocked(this)) {
            return true;
        }

        return !getTrainLockedFromPacket();
    }
}