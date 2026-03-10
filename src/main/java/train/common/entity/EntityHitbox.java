package train.common.entity;

import ebf.tim.entities.EntitySeat;
import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.Vec3d;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.EntityDamageSource;
import train.common.api.AbstractTrains;
import train.common.api.EntityBogie;
import train.common.api.EntityRollingStock;
import train.common.api.Locomotive;
import train.common.core.handlers.ConfigHandler;

import java.util.ArrayList;
import java.util.List;

public class EntityHitbox {

    private final EntityRollingStock host;
    private Vec3f hostSize;

    private final List<CollisionBox> hitboxes = new ArrayList<>();
    private CollisionBox front,back;

    /*
     * =========================================== INIT ===========================================
     **/

    public EntityHitbox(EntityRollingStock host) {
        this.host = host;
    }

    public void init() {
        hostSize = new Vec3f(host.getHitboxSize()); // Cache it once on init, it's not gonna change anyway

        float depth = hostSize.xCoord + host.getOptimalDistance();
        for (float f = 0; f < depth - (hostSize.zCoord * 0.25f); f += hostSize.zCoord) {
            CollisionBox c = new CollisionBox(host);
            c.boundingBox.setBounds(-hostSize.zCoord * 0.5, 0, -hostSize.zCoord * 0.5, hostSize.zCoord * 0.5, hostSize.yCoord, hostSize.zCoord * 0.5);
            hitboxes.add(c);
            host.getWorld().spawnEntityInWorld(c);
        }
        front = hitboxes.get(0);
        back = hitboxes.get(hitboxes.size() - 1);
        updatePositions();
    }

    /*
     * =========================================== UPDATE ===========================================
     **/

    public void update() {
        updatePositions();
        updateCollisions();
    }

    private void updatePositions() {
        for(int i = 0; i< hitboxes.size(); i++) {
            // Offsets each hitbox in a line from the host's front to its back, taking into account pitch and yaw; then append the host's absolute position
            Vec3d newPos = CommonUtil.rotateDistance(-host.getOptimalDistance() + ((hostSize.xCoord / hitboxes.size()) * (i + 0.5f)), -host.rotationPitch, host.rotationYaw).addVector(host.posX, host.posY - 0.35, host.posZ);
            hitboxes.get(i).setPosition(newPos.xCoord, newPos.yCoord, newPos.zCoord);
        }
    }

    private void updateCollisions() {
        List<Entity> collidingEntities = new ArrayList<>();

        for (Object obj : host.getWorld().getEntitiesWithinAABBExcludingEntity(host, host.boundingBox.expand(hostSize.xCoord + 4, hostSize.xCoord + 4, hostSize.xCoord + 4))) {

            // Skip our own hitboxes and those in the same linked train
            if (obj instanceof CollisionBox) {
                if (hitboxes.contains((CollisionBox)obj) || host.links.contains(((CollisionBox)obj).host))
                    continue;
            }
            // Collide with carts, but not our own stocks/bogies
            else if (obj instanceof EntityMinecart) {
                if (obj instanceof AbstractTrains || obj instanceof EntityBogie)
                    continue;

            }
            // Skip everything else but living entities (ex. seats), excluding if they are riding something else (ex. passengers in seats)
            else if (!(obj instanceof EntityLivingBase) || ((Entity) obj).ridingEntity != null) {
                continue;
            }

            if (intersectsWith((Entity) obj))
                collidingEntities.add((Entity)obj);
        }

        for (Entity e : collidingEntities) {
            // On client we need to push away players.
            if (host.getWorld().isRemote) {
                if (e instanceof EntityLivingBase) {
                    e.applyEntityCollision(host);
                }
            }
            else {
                if (e instanceof CollisionBox) {
                    EntityRollingStock other = ((CollisionBox) e).host;

                    // Attempt to link if both are attaching
                    if (host.links.getIsAttaching() && other.links.getIsAttaching())
                        host.links.link(other);

                    // Else push us back
                    else appendMovement(e, 0.005f);
                }
                else {
                    // Hurt entity if going fast
                    if (Math.abs(host.motionX) + Math.abs(host.motionZ) > 0.25f)
                        e.attackEntityFrom(new EntityDamageSource(host.getClass().toString(), host), (float) (Math.abs(host.motionX) + Math.abs(host.motionZ)) * 0.5f);

                    // Push us back
                    appendMovement(e, 0.005f);
                }
            }
        }
    }

    private void appendMovement(Entity e, float strength) {
        // Don't receive movement if the config is off, we're a locomotive, or the linked train has an active locomotive
        if (ConfigHandler.PUSHABLE_ROLLINGSTOCK && !(host instanceof Locomotive) && !(host.links.isActiveLocoLinked())) {
            double distanceFront = Math.sqrt((e.posX - front.posX) * (e.posX - front.posX) + (e.posZ - front.posZ) * (e.posZ - front.posZ));
            double distanceBack = Math.sqrt((e.posX - back.posX) * (e.posX - back.posX) + (e.posZ - back.posZ) * (e.posZ - back.posZ));

            host.appendMovement(distanceFront < distanceBack ? -strength : strength);
        }
    }

    private boolean intersectsWith(Entity e) {
        for (CollisionBox box : hitboxes) {
            if (e.boundingBox.intersectsWith(box.boundingBox))
                return true;
        }
        return false;
    }

    /*
     * =========================================== PUBLIC UTILS ===========================================
     **/

    public void setDead() {
        for (CollisionBox box : hitboxes) {
            box.setDead();
            host.getWorld().removeEntity(box);
        }
    }

    public Entity[] getParts() { return hitboxes.toArray(new Entity[]{}); }
    public Vec3f getFrontPos() { return new Vec3f(front.posX, front.posY, front.posZ); }
    public Vec3f getBackPos() { return new Vec3f(back.posX, back.posY, back.posZ); }
}