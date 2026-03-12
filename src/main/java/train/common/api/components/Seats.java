package train.common.api.components;

import ebf.tim.entities.EntitySeat;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
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
                if(host.ticksExisted % 18 == 0)
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
        }
    }

    private boolean addPassenger(EntitySeat seat, EntityLivingBase passenger) {
        if (passenger != null && (seat.riddenByEntity == null || seat.riddenByEntity == passenger)) { //1.12 is stupid, sometimes when the passenger is null, it returns the player
            passenger.mountEntity(seat);
            return true;

        }
        return false;
    }

    private void removePassenger(EntitySeat seat) {
        if (seat.riddenByEntity != null) {
            seat.riddenByEntity.ridingEntity = null;
            seat.riddenByEntity = null;
        }
    }

    private boolean canEnter(EntityPlayer player) {
        //be sure operators and owners can do whatever
        if ((player.capabilities.isCreativeMode && player.canCommandSenderUseCommand(2, "")) || host.getOwner() == player.getGameProfile() || host.isPlayerTrusted(player.getDisplayName()) || host.canBeRiddenWhileLocked())
            return true;

        return !host.getTrainLockedFromPacket();
    }

    /*
     * =========================================== PUBLIC UTILS ===========================================
     **/

    // Note: This needs to be called for both clients and servers at the moment, else either the player doesn't follow the seat, or menus can't be opened
    public boolean onEnteringSeat(EntityPlayer playerEntity) {
        if (canEnter(playerEntity) && !playerEntity.isSneaking()) {
            for (EntitySeat seat : seats) {
                if (addPassenger(seat, playerEntity))
                    return true;
            }
        }
        return false;
    }

    public int size() { return seats.size(); }

    public boolean isPassengerOwner(int i) {
        return !seats.isEmpty() && seats.get(i).riddenByEntity instanceof EntityPlayer && ((EntityPlayer) seats.get(i).riddenByEntity).getDisplayName().equalsIgnoreCase(host.getTrainOwner());
    }

    public EntityLivingBase getPassengerAtIndex(int i) {
        return (!seats.isEmpty() && seats.size() >= i) ? (EntityLivingBase)seats.get(i).riddenByEntity : null;
    }

    public EntityLivingBase getDriver() {
        return getPassengerAtIndex(0);
    }

    public boolean addPassengerAtIndex(int i, EntityLivingBase passenger) {
        return addPassenger(seats.get(i), passenger);
    }

    public void removePassengerAtIndex(int i) {
        removePassenger(seats.get(i));
    }

    public void setDead() {
        for (EntitySeat seat : seats) {
            seat.setDead();
            host.getWorld().removeEntity(seat);
        }
    }
}