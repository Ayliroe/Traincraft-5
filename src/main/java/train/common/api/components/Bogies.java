package train.common.api.components;

import cpw.mods.fml.common.network.NetworkRegistry;
import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import train.common.Traincraft;
import train.common.api.EntityBogie;
import train.common.api.EntityRollingStock;
import train.common.api.TrainUtils;
import train.common.core.network.PacketRollingStockRotation;
import train.common.core.util.TraincraftUtil;

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
        double[] offset = CommonUtil.rotatePoint(rotationPoints[0], 0, 180 + host.rotationYaw);
        bogieFront = new EntityBogie(host, host.posX + offset[0], host.posY, host.posZ + offset[2]);

        offset = CommonUtil.rotatePoint(rotationPoints[1], 0, 180 + host.rotationYaw);
        bogieBack = new EntityBogie(host, host.posX + offset[0], host.posY, host.posZ + offset[2]);

        host.getWorld().spawnEntityInWorld(bogieBack);
        host.getWorld().spawnEntityInWorld(bogieFront);
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

    /*
     * =========================================== UPDATE ===========================================
     **/

    // All bogie updates are serverside, so the client is updated through packages
    public void update() {
        bogieFront.update();
        bogieBack.update();
        sendPositionToClient();
        host.setPositionAndRotation(pos().xCoord, pos().yCoord, pos().zCoord, yaw(), pitch());
    }

    private void sendPositionToClient() {
        if (!host.getWorld().isRemote) {
            Traincraft.rotationChannel.sendToAllAround(new PacketRollingStockRotation(host, bogieBack.posX, bogieBack.posY, bogieBack.posZ, bogieFront.posX, bogieFront.posY, bogieFront.posZ),
                    new NetworkRegistry.TargetPoint(host.getWorld().provider.dimensionId, host.posX, host.posY, host.posZ, 300.0D));
        }
    }

    public void receivePositionFromServer(double backx, double backy, double backz, double frontx, double fronty, double frontz) {
        bogieBack.setPosition(backx, backy, backz);
        bogieFront.setPosition(frontx, fronty, frontz);
    }

    public void setVelocity(double velocity) {
        bogieBack.setVelocity(velocity);
        bogieFront.setVelocity(velocity);
    }

    public void addVelocity(double velocity) {
        bogieBack.addVelocity(velocity);
        bogieFront.addVelocity(velocity);
    }

    public void multiplyVelocity(double vel) {
        bogieBack.multiplyVelocity(vel);
        bogieFront.multiplyVelocity(vel);
    }

    public void setVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        double velocity = Math.sqrt(Math.pow(p_70024_1_,2)+Math.pow(p_70024_5_,2));
        bogieBack.setVelocity(velocity);
        bogieFront.setVelocity(velocity);
    }

    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        double velocity = Math.sqrt(Math.pow(p_70024_1_,2)+Math.pow(p_70024_5_,2));
        bogieBack.addVelocity(velocity);
        bogieFront.addVelocity(velocity);
    }

    public double velocity() { return Math.sqrt(bogieBack.velocity[0] * bogieBack.velocity[0] + bogieBack.velocity[1] * bogieBack.velocity[1]); }

    public Vec3f pos() { return new Vec3f(rotationPoints[1], 0, 0).rotatePoint(0, yaw(), 0).addVector(bogieBack.posX, (bogieBack.posY+bogieFront.posY) * 0.5, bogieBack.posZ); }

    public float yaw() { return TraincraftUtil.atan2degreesf(bogieBack.posZ - bogieFront.posZ, bogieBack.posX - bogieFront.posX); }

    public float pitch() { return CommonUtil.calculatePitch(bogieFront.posY, bogieBack.posY, Math.abs(rotationPoints[0]) + Math.abs(rotationPoints[1])); }
                // CommonUtil.atan2degreesf(bogieFront.posY - bogieBack.posY, Math.sqrt(Math.pow(bogieBack.posX - bogieFront.posX) + Math.pow(bogieBack.posZ - bogieFront.posZ)));

    public float isDerailed() { return (bogieBack.isOnRail ? 0f : 0.5f) + (bogieFront.isOnRail ? 0f : 0.5f); }

    public void setDead() {
        bogieFront.setDead();
        bogieBack.setDead();
    }
}