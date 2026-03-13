package train.common.core.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ebf.tim.utility.DebugUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import train.common.api.EntityRollingStock;

/**
 * Sent to nearby clients to update the rotation of rolling stock.<p>
 * <p>
 * Field names adapted from 1.6 Packet code.
 */
public class PacketRollingStockRotation implements IMessage {

    int entityID;
    double frontx=0,fronty=0,frontz=0,backx=0,backy=0,backz=0;

    public PacketRollingStockRotation() {}

    public PacketRollingStockRotation(EntityRollingStock entity, double backx, double backy, double backz, double frontx, double fronty, double frontz) {
        this.entityID = entity.getEntityId();

        this.frontx = frontx;
        this.fronty = fronty;
        this.frontz = frontz;
        this.backx = backx;
        this.backy = backy;
        this.backz = backz;

    }

    @Override
    public void fromBytes(ByteBuf bbuf) {
        this.entityID = bbuf.readInt();
        this.frontx = bbuf.readDouble();
        this.fronty = bbuf.readDouble();
        this.frontz = bbuf.readDouble();
        this.backx = bbuf.readDouble();
        this.backy = bbuf.readDouble();
        this.backz = bbuf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf bbuf) {
        bbuf.writeInt(this.entityID);
        bbuf.writeDouble(frontx);
        bbuf.writeDouble(fronty);
        bbuf.writeDouble(frontz);
        bbuf.writeDouble(backx);
        bbuf.writeDouble(backy);
        bbuf.writeDouble(backz);
    }

    public static class Handler implements IMessageHandler<PacketRollingStockRotation, IMessage> {
        @Override
        public IMessage onMessage(PacketRollingStockRotation message, MessageContext context) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null) {
                Entity entity = mc.theWorld.getEntityByID(message.entityID);
                if (entity instanceof EntityRollingStock) {
                    EntityRollingStock rollingStock = (EntityRollingStock) entity;

                    rollingStock.bogies.receivePositionFromServer(message.backx, message.backy, message.backz, message.frontx, message.fronty, message.frontz);
                }
            }

            return null;
        }
    }
}