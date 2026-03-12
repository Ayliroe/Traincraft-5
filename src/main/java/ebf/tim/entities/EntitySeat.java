package ebf.tim.entities;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import train.common.api.EntityRollingStock;

public class EntitySeat extends EntityDragonPart {

    private final EntityRollingStock host;
    private final boolean isControlSeat;

    // Constructor called by both client and server hosts
    public EntitySeat(EntityRollingStock host, boolean isControlSeat) {
        super(host, "seat", 0.5f, 1.5f);
        this.host = host;
        this.isControlSeat = isControlSeat;
    }

    public boolean isControlSeat() { return isControlSeat; }
    public EntityRollingStock getHost() { return host; }

    /*
     * =========================================== MINECRAFT ENTITY ===========================================
     **/

    @Override
    public boolean shouldRiderSit() { return host.shouldRiderSit(); }

    @Override
    public double getMountedYOffset() { return 0d; }

    // Returning false in both of these disables writing the entity to disk
    @Override
    public boolean writeToNBTOptional(NBTTagCompound tagCompound) { return false; }
    @Override
    public boolean writeMountToNBT(NBTTagCompound tagCompound) { return false; }

    @Override
    public boolean canBeCollidedWith() { return false; }
}