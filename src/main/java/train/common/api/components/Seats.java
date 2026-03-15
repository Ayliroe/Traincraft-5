package train.common.api.components;

import ebf.tim.entities.EntitySeat;
import ebf.tim.networking.PacketSeatUpdate;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import train.common.Traincraft;
import train.common.api.EntityRollingStock;

import java.util.LinkedList;
import java.util.List;

public class Seats {

    private final EntityRollingStock host;

    private final List<EntitySeat> seats = new LinkedList<>();
    private float[][] riderOffsets;

    /*
     * =========================================== INIT ===========================================
     **/

    public Seats(EntityRollingStock host) {
        this.host = host;
    }

    public void init() {
        riderOffsets = host.getRiderOffsets();

        for (int i = 0; i < riderOffsets.length; i++) {
            EntitySeat seat = new EntitySeat(host, i == 0);
            seats.add(seat);
            host.getWorld().spawnEntityInWorld(seat);
        }
    }

    /*
     * =========================================== UPDATE ===========================================
     **/

    public void update() {
        updatePassengers();
        updatePositions();
    }

    private void updatePassengers() {
        for (EntitySeat seat : seats) {
            if (seat.riddenByEntity != null) {

                //just so we aren't doing it *every* tick, but still frequent enough to not let the passenger actually take damage
                if(host.ticksExisted % 18 == 0 && seat.riddenByEntity instanceof EntityLivingBase)
                    ((EntityLivingBase)seat.riddenByEntity).addPotionEffect(new PotionEffect(Potion.resistance.id, 20, 5, true));

                if (seat.riddenByEntity.isDead || seat != seat.riddenByEntity.ridingEntity) {
                    removePassenger(seat);
                }
            }
        }
    }

    private void updatePositions() {
        for (int i = 0; i < seats.size(); i++) {
            Vec3f pos = new Vec3f(riderOffsets[i]).rotatePoint(host.rotationPitch, 180 + host.rotationYaw, 0f).addVector(host.posX, host.posY, host.posZ);
            seats.get(i).setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
            seats.get(i).updateRiderPosition(); // Import to prevent the rider from lagging behind at speed
        }
    }

    /*
     * =========================================== SET PASSENGERS ===========================================
     **/

    public boolean onEnteringSeat(Entity passenger) {
        if (!host.getWorld().isRemote && !passenger.isSneaking()) {
            for (EntitySeat seat : seats) {
                if (canEnter(seat, passenger)) {
                    Traincraft.updateChannel.sendToServer(new PacketSeatUpdate(host.getEntityId(), passenger.getEntityId(), seats.indexOf(seat)));
                    return true;
                }
            }
        }
        return false;
    }

    public void updateFromPacket(int seatID, Entity passenger) {
        for (EntitySeat seat : seats) {
            if (seat.riddenByEntity == passenger)
                removePassenger(seat);
        }

        EntitySeat seat = seats.get(seatID);
        if (canEnter(seat, passenger)) {
            passenger.mountEntity(seat);
        }
    }

    private void removePassenger(EntitySeat seat) {
        if (seat.riddenByEntity != null) {
            seat.riddenByEntity.ridingEntity = null;
            seat.riddenByEntity = null;
        }
    }

    // 1.12 is stupid, sometimes when the passenger is null, it returns the player
    private boolean canEnter(EntitySeat seat, Entity passenger) { return seat.riddenByEntity == null || seat.riddenByEntity == passenger; }

    /*
     * =========================================== PUBLIC UTILS ===========================================
     **/

    public int size() { return seats.size(); }

    public boolean isPassengerOwner(int i) {
        return !seats.isEmpty() && seats.get(i).riddenByEntity instanceof EntityPlayer && ((EntityPlayer) seats.get(i).riddenByEntity).getDisplayName().equalsIgnoreCase(host.getTrainOwner());
    }

    public Entity getPassengerAtIndex(int i) {
        return (!seats.isEmpty() && seats.size() >= i) ? seats.get(i).riddenByEntity : null;
    }

    public Entity getDriver() { return getPassengerAtIndex(0); }

    public void setDead() {
        for (EntitySeat seat : seats) {
            seat.setDead();
            host.getWorld().removeEntity(seat);
        }
    }
}