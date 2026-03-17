package train.common.core.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import train.common.api.EntityBogie;
import train.common.api.EntityRollingStock;

public class PacketSendBogieID implements IMessage {

    int stockID, bogieID;
    boolean isFront;

    public PacketSendBogieID() {}
    public PacketSendBogieID(int stockID, int bogieID, boolean isFront) {
        this.stockID = stockID;
        this.bogieID = bogieID;
        this.isFront = isFront;
    }

    @Override
    public void fromBytes(ByteBuf bbuf) {
        this.stockID = bbuf.readInt();
        this.bogieID = bbuf.readInt();
        this.isFront = bbuf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf bbuf) {
        bbuf.writeInt(this.stockID);
        bbuf.writeInt(this.bogieID);
        bbuf.writeBoolean(this.isFront);
    }

    public static class Handler implements IMessageHandler<PacketSendBogieID, IMessage> {
        @Override
        public IMessage onMessage(PacketSendBogieID message, MessageContext context) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null) {
                Entity entity = mc.theWorld.getEntityByID(message.stockID);
                Entity bogie = mc.theWorld.getEntityByID(message.bogieID);
                if (entity instanceof EntityRollingStock && bogie instanceof EntityBogie) {
                    ((EntityRollingStock) entity).bogies.setBogieFromServer((EntityBogie)bogie, message.isFront);
                    ((EntityBogie)bogie).host = ((EntityRollingStock) entity);
                }
            }
            return null;
        }
    }
}