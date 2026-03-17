package train.common.api.components;

import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import train.common.api.EntityBogie;
import train.common.api.EntityRollingStock;
import train.common.api.TrainUtils;

import java.util.Collections;

public class Bogies {

    private final EntityRollingStock host;

    private EntityBogie bogieFront = null;
    private EntityBogie bogieBack = null;

    private float[] rotationPoints;

    /*
     * =========================================== INIT ===========================================
     **/

    public Bogies(EntityRollingStock host) {
        this.host = host;
    }

    public void init() {
        rotationPoints = host.rotationPoints();
        if (!host.getWorld().isRemote) {
            double[] offset = CommonUtil.rotatePoint(rotationPoints[1], 0, 180 + host.rotationYaw);
            bogieBack = new EntityBogie(host, host.posX + offset[0], host.posY, host.posZ + offset[2], false);

            offset = CommonUtil.rotatePoint(rotationPoints[0], 0, 180 + host.rotationYaw);
            bogieFront = new EntityBogie(host, host.posX + offset[0], host.posY, host.posZ + offset[2], true);
        }
    }

    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setTag("Motion", TrainUtils.newDoubleNBTList(bogieBack.velocity[0], bogieBack.velocity[1], bogieFront.velocity[0], bogieFront.velocity[1]));
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist1 = nbttagcompound.getTagList("Motion", 6);
        bogieBack.velocity[0] = nbttaglist1.func_150309_d(0);
        bogieBack.velocity[1] = nbttaglist1.func_150309_d(1);
        bogieFront.velocity[0] = nbttaglist1.func_150309_d(2);
        bogieFront.velocity[1] = nbttaglist1.func_150309_d(3);
    }

    /**
     * Triggered when the host is successfully spawned in the world (not just constructed), so this the best place to do the same for the bogies
     * Attempting to spawn the bogies in init() doesn't work well on map load, or even crashes if they are set to forceSpawn,
     * and spawning them in update() doesn't result in their movements properly syncing with the host's
     */
    public void entitySpawn() {
        if (!host.getWorld().isRemote) {
            // We *have* to use addLoadedEntities(), because if spawnEntityInWorld() triggers at map load Forge adds each bogie twice then stupidly complains they are duplicated
            host.getWorld().addLoadedEntities(Collections.singletonList(bogieBack));
            host.getWorld().addLoadedEntities(Collections.singletonList(bogieFront));
        }
    }

    /**
     * The clientside bogies are spawned at the will of Minecraft/FML client instance, so we receive a packet from each one after that happens
     * so the clientside host can sync its movement with them.
     **/
    public void setBogieFromServer(EntityBogie bogie, boolean isFront) {
        if (host.getWorld().isRemote) {
            if (isFront)    bogieFront = bogie;
            else            bogieBack = bogie;
        }
    }

    /*
     * =========================================== UPDATE ===========================================
     * Normally there's no need to do null checks, but clientside bogies are only valid once the client spawns them so it's needed here.
     **/

    public void update() {
        if (bogieBack != null && bogieFront != null) {
            bogieBack.update();
            bogieFront.update();
            host.setPositionAndRotation(pos().xCoord, pos().yCoord, pos().zCoord, yaw(), pitch());
        }
    }

    public void setVelocity(double velocity) {
        if (!host.getWorld().isRemote) {
            bogieBack.setVelocity(velocity);
            bogieFront.setVelocity(velocity);
        }
    }

    public void addVelocity(double velocity) {
        if (!host.getWorld().isRemote) {
            bogieBack.addVelocity(velocity);
            bogieFront.addVelocity(velocity);
        }
    }

    public void multiplyVelocity(double vel) {
        if (!host.getWorld().isRemote) {
            bogieBack.multiplyVelocity(vel);
            bogieFront.multiplyVelocity(vel);
        }
    }

    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        if (!host.getWorld().isRemote) {
            double velocity = Math.sqrt(Math.pow(p_70024_1_, 2) + Math.pow(p_70024_5_, 2));
            bogieBack.addVelocity(velocity);
            bogieFront.addVelocity(velocity);
        }
    }

    public double velocity() { return Math.sqrt(bogieBack.velocity[0] * bogieBack.velocity[0] + bogieBack.velocity[1] * bogieBack.velocity[1]); }

    private Vec3f pos() { return new Vec3f(rotationPoints[1], 0, 0).rotatePoint(0, yaw(), 0).addVector(bogieBack.posX, (bogieBack.posY+bogieFront.posY) * 0.5, bogieBack.posZ); }

    // TC's internal utils for atan2 are faster but too imprecise and cause hacky movement, so we use java ones here
    public float yaw() { return (float)Math.toDegrees(Math.atan2(bogieBack.posZ - bogieFront.posZ, bogieBack.posX - bogieFront.posX)); }

    private float pitch() { return CommonUtil.calculatePitch(bogieFront.posY, bogieBack.posY, Math.abs(rotationPoints[0]) + Math.abs(rotationPoints[1])); }
                // CommonUtil.atan2degreesf(bogieFront.posY - bogieBack.posY, Math.sqrt(Math.pow(bogieBack.posX - bogieFront.posX) + Math.pow(bogieBack.posZ - bogieFront.posZ)));

    public float isDerailed() { return (bogieBack == null || bogieFront == null) ? 1f : (bogieBack.isOnRail ? 0f : 0.5f) + (bogieFront.isOnRail ? 0f : 0.5f); }

    public void setDead() {
        if (bogieBack != null)
            bogieBack.setDead();
        if (bogieFront != null)
            bogieFront.setDead();
    }
}