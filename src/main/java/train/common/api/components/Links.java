package train.common.api.components;

import cpw.mods.fml.common.network.NetworkRegistry;
import mods.railcraft.api.tracks.RailTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import train.common.Traincraft;
import train.common.api.AbstractTrains;
import train.common.api.EntityRollingStock;
import train.common.api.Locomotive;
import train.common.core.network.PacketSetTrainLockedToClient;
import train.common.entity.TrustedPlayer;

import java.util.ArrayList;
import java.util.List;

public class Links {

    private boolean isAttached = false;
    private boolean isAttaching = false;
    private double frontID;
    private double backID;
    private AbstractTrains front = null;
    private AbstractTrains back = null;
    private ArrayList<AbstractTrains> list = new ArrayList<>(); // This SHOULD remain private and without a getter, to not have code affecting this list in random places
    private Integer leadID = null;

    private final AbstractTrains host;

    public Links(AbstractTrains host) {
        this.host = host;
        list.add(host);
    }

    /*
     * =========================================== UTILS ===========================================
     **/

    public boolean isLinked()           { return isFrontLinked() || isBackLinked(); }
    public boolean isFrontLinked()      { return front != null; }
    public boolean isBackLinked()       { return back != null; }
    public AbstractTrains getFront()    { return front; }
    public AbstractTrains getBack()     { return back; }
    public int getListSize()            { return list.size(); }

    public AbstractTrains getLead()     { return leadID != null ? (AbstractTrains)host.getWorld().getEntityByID(leadID) : null; }
    public boolean contains(AbstractTrains other) {
        for (AbstractTrains listLink : list) {
            if (listLink.getEntityId() == other.getEntityId())
                return true;
        }
        return false;
    }

    public double getTotalMhpExcludingSelf() {
        double totalMhp = 0;
        for (AbstractTrains stock : list) {
            if (stock instanceof Locomotive && stock.uniqueID != host.uniqueID) {
                totalMhp += ((Locomotive)stock).getMHP();
            }
        }
        return totalMhp;
    }

    public boolean isActiveLocoLinked() {
        for(AbstractTrains stock : list) {
            if(stock instanceof Locomotive && ((Locomotive)stock).isLocoTurnedOn && !stock.canBePushed()) {
                return true;
            }
        }
        return false;
    }

    public boolean isLinkedStockLockedDown() {
        for (AbstractTrains stock : list) {
            if (stock != null && RailTools.isCartLockedDown(stock))
                return true;
        }
        return false;
    }

    public void updateSpring() {
        if (host instanceof EntityRollingStock) {
            double activeSpring = 0.5d;
            double passiveSpring = 0.25d;
            double springDist = 0d;
            int pullingDir = pullingLocomotiveDirection();
            if (front instanceof EntityRollingStock) {
                springDist += manageLink((EntityRollingStock) front) * (pullingDir == 1 ? activeSpring : (pullingDir == -1 ? 0 : passiveSpring));
            }
            if (back instanceof EntityRollingStock) {
                springDist -= manageLink((EntityRollingStock) back) * (pullingDir == -1 ? activeSpring : (pullingDir == 1 ? 0 : passiveSpring));
            }
            // Non-null springDist means this stock is allowed to be pulled and an active link is pulling or pushing it
            if (springDist != 0d) {
                ((EntityRollingStock) host).bogies.setVelocity(springDist);
            }
        }
    }

    private double manageLink(EntityRollingStock other) {
        // Don't apply spring movement if a non-passive loco
        if (host instanceof EntityRollingStock) {
            if (host instanceof Locomotive && !host.canBePushed())
                return 0d;

            double vecX = other.posX - host.posX;
            double vecZ = other.posZ - host.posZ;

            return MathHelper.sqrt_double(vecX * vecX + vecZ * vecZ) - (host.getOptimalDistance() + other.getOptimalDistance());
        }
        return 0;
    }

    /**
     * Finds the direction from which a locomotive is pulling/pushing from.
     * @return Returns 1 if from front, -1 if from back, or 0 if no pulling locomotive exists or this is the pulling locomotive.
     */
    private int pullingLocomotiveDirection() {
        if (host instanceof Locomotive && !host.canBePushed()) {
            return 0;
        }

        ArrayList<AbstractTrains> visited = new ArrayList<>();  // In case somebody makes a circular train
        visited.add(host);

        boolean visitingFront = true;

        AbstractTrains previousTrain = host;
        AbstractTrains train = front;
        while (!visited.contains(train)) {
            if (train == null) {
                // If we have reached the front end, reset and start from the back. If we reached that other end too, break.
                if (visitingFront) {
                    visitingFront = false;
                    train = back;
                    previousTrain = host;
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
            if (train.links.front != previousTrain) {
                previousTrain = train;
                train = train.links.front;
            }
            else {
                previousTrain = train;
                train = train.links.back;
            }
        }
        // Both front and back didn't find anything, so there's no pulling locomotive.
        return 0;
    }

    /*
     * =========================================== UPDATING ===========================================
     **/

    /**
     * Called on linking changes and when a train changes running states
     * @param list the list of entities in the list
     */
    public void setValuesOnLinkUpdate(ArrayList<AbstractTrains> list) {
        this.list=list;

        if (host instanceof Locomotive) {
            ((Locomotive)host).currentMassPulled=0;
            for (AbstractTrains t : list) {
                ((Locomotive)host).currentMassPulled += t.weightKg();
            }
        }
    }

    public void refreshLeadID() {
        if(leadID == null || leadID != host.getEntityId()) {
            updateLinks();
        }
    }

    /**
     * Server side method used to update the lock status and trusted players list for all cars in a given linked list belonging to the train owner.
     */
    public void propagateLockAndTrustedList(List<TrustedPlayer> trustedPlayerList) {
        boolean locked = host.getTrainLockedFromPacket();
        for (AbstractTrains car : list) {
            if (car != host && car.getTrainOwner().equalsIgnoreCase(host.getTrainOwner())) {
                car.setTrainLockedFromPacket(locked);
                car.setTrustedList(trustedPlayerList);
                Traincraft.lockChannel.sendToAllAround(new PacketSetTrainLockedToClient(locked, trustedPlayerList, car.getEntityId(), false),
                        new NetworkRegistry.TargetPoint(car.dimension, car.posX, car.posY, car.posZ, 256D));
            }
        }
    }

    public void updateLinks() {
        ArrayList<AbstractTrains> transports = new ArrayList<>();

        traverseLinks(host, transports);
        if(transports.size()<2){
            list = transports;
            leadID = host.getEntityId();
            return;
        }

        AbstractTrains frontTrain = findFront(transports);
        transports = new ArrayList<>();
        traverseLinks(frontTrain, transports);

        for (AbstractTrains t : transports) {
            t.links.list = transports;
            t.links.leadID = frontTrain.getEntityId();
            t.links.setValuesOnLinkUpdate(transports);
        }
    }

    private static void traverseLinks(AbstractTrains current, ArrayList<AbstractTrains> visited) {
        if (current == null || visited.contains(current)) {
            return;
        }

        visited.add(current);
        if (current.links.front != null) {
            traverseLinks(current.links.front, visited);
        }
        if (current.links.back != null) {
            traverseLinks(current.links.back, visited);
        }
    }

    private static AbstractTrains findFront(ArrayList<AbstractTrains> transports) {
        for (AbstractTrains train : transports) {
            if (train instanceof Locomotive && !train.canBePushed()) {
                return train;
            }
        }

        for (AbstractTrains train : transports) {
            if (train.links.front == null || train.links.back == null) {
                return train;
            }
        }

        return transports.get(0);
    }

    /*
     * =========================================== LINK/UNLINK ===========================================
     **/

    public boolean getIsAttaching() { return isAttaching; }
    public void setIsAttaching(boolean attaching) { this.isAttaching = attaching; }

    public void link(EntityRollingStock other) {
        if (host instanceof EntityRollingStock) {
            EntityPlayer player = host.getWorld().getClosestPlayerToEntity(host, 20);

            if (other.canBePushed() || host.canBePushed()) {
                EntityRollingStock stock = (EntityRollingStock)host;

                // Link to front or back based on the closest collision to the other stock
                if (stock.hitbox.getFrontPos().subtract(other.getPos()).length() < stock.hitbox.getBackPos().subtract(other.getPos()).length()) {
                    if (front == null) {
                        front = other;
                        frontID = other.uniqueID;
                    }
                } else {
                    if (back == null) {
                        back = other;
                        backID = other.uniqueID;
                    }
                }

                // Do the same for the other stock
                if (other.hitbox.getFrontPos().subtract(host.getPos()).length() < other.hitbox.getBackPos().subtract(host.getPos()).length()) {
                    if (other.links.front == null) {
                        other.links.front = host;
                        other.links.frontID = host.uniqueID;
                    }
                } else {
                    if (other.links.back == null) {
                        other.links.back = host;
                        other.links.backID = host.uniqueID;
                    }
                }
                other.links.isAttaching = false;
                isAttaching = false;
                other.links.isAttached = true;
                isAttached = true;

                updateLinks();


                if (player != null)
                    player.addChatMessage(new ChatComponentText("attached!"));

            } else {
                if (player != null) {
                    player.addChatComponentMessage(new ChatComponentText("One or more trains is not in towing mode."));
                    player.addChatComponentMessage(new ChatComponentText("Use a Stake while sneaking to toggle towing mode."));
                }
            }
        }
    }

    public void unlink() {
        if (isAttached) {
            detachLink(front);
            detachLink(back);
            front = null;
            back = null;
            isAttached = false;
            updateLinks();
        }
    }

    private void detachLink(AbstractTrains other) {
        if (other != null) {
            if (other.links.front == host) {
                other.links.frontID = 0;
                other.links.front = null;
                if (other.links.list != null){
                    other.links.list.clear();
                    other.links.list.add(other);
                    other.links.updateLinks();
                }

            } else if (other.links.back == host) {
                other.links.backID = 0;
                other.links.back = null;
                if (other.links.list != null){
                    other.links.list.clear();
                    other.links.list.add(other);
                    other.links.updateLinks();
                }
            }
        }
    }

    /**
     * As entities can't be registered in nbttagcompound I had to setup this
     * system... When world loads, only the (double) Link1 and Link2 are
     * known. This method search for the entity with the ID corresponding to
     * Link1 or Link2 When it finds it, (EntityRollingStock)frontLink and
     * backLink will be updated accordingly
     */
    public void restoreLinks() {
        if (host.addedToChunk && ((front == null && frontID != 0) || (back == null && backID != 0))) {
            List<?> list = host.getWorld().getEntitiesWithinAABBExcludingEntity(host, host.boundingBox.expand(15, 15, 15));

            if (list != null && !list.isEmpty()) {
                for (Object entity : list) {
                    if (entity instanceof EntityRollingStock) {
                        if (((EntityRollingStock) entity).uniqueID == frontID) {
                            front = (EntityRollingStock) entity;
                        } else if (((EntityRollingStock) entity).uniqueID == backID) {
                            back = (EntityRollingStock) entity;
                        }
                    }
                }
            }
        }
    }

    /*
     * =========================================== NBT ===========================================
     **/

    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("isAttached", isAttached);
        nbttagcompound.setDouble("frontID", frontID);
        nbttagcompound.setDouble("backID", backID);
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        isAttached = nbttagcompound.getBoolean("isAttached");
        frontID = nbttagcompound.getDouble("frontID");
        backID = nbttagcompound.getDouble("backID");
    }
}