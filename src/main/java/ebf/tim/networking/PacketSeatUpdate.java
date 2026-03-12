package ebf.tim.networking;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.EntityRollingStock;

/**
 * Bi-directional packet for entering a stock's seat from outside, or switching seats from inside
 */
public class PacketSeatUpdate implements IMessage {

    private int stockId, playerId, seatID;

    public PacketSeatUpdate() {};
    public PacketSeatUpdate(int stockId, int playerId, int seatID) {
        this.stockId = stockId;
        this.playerId = playerId;
        this.seatID = seatID;
    }

    @Override
    public void fromBytes(ByteBuf bbuf) {
        stockId = bbuf.readInt();
        playerId = bbuf.readInt();
        seatID = bbuf.readInt();
    }

    @Override
    public void toBytes(ByteBuf bbuf) {
        bbuf.writeInt(stockId);
        bbuf.writeInt(playerId);
        bbuf.writeInt(seatID);
    }

    public static class Handler implements IMessageHandler<PacketSeatUpdate,IMessage> {
        @Override public IMessage onMessage(PacketSeatUpdate message, MessageContext ctx) {
            World world = ctx.side == Side.SERVER ? ctx.getServerHandler().playerEntity.worldObj : Minecraft.getMinecraft().theWorld;
            EntityRollingStock stock = (EntityRollingStock) world.getEntityByID(message.stockId);
            EntityPlayer player = (EntityPlayer) world.getEntityByID(message.playerId);

            stock.seats.updateFromPacket(message.seatID, player);

            if (ctx.side == Side.SERVER) {
                Traincraft.updateChannel.sendToAllAround(new PacketSeatUpdate(message.stockId, message.playerId, message.seatID),
                        new NetworkRegistry.TargetPoint(stock.dimension, stock.posX, stock.posY, stock.posZ, 256D));
            }
            return null;
        }
    }
}