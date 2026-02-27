package train.common.mtc.packets.handlers;


import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ebf.tim.utility.DebugUtil;
import train.common.Traincraft;
import train.common.api.Locomotive;
import train.common.mtc.packets.PacketGetSomethingFromServer;
import train.common.mtc.packets.PacketThingFromServer;

public class PacketGetSomethingFromServerHandler implements IMessageHandler<PacketGetSomethingFromServer, IMessage> {

    @Override
    public PacketThingFromServer onMessage(PacketGetSomethingFromServer message, MessageContext ctx) {
        Locomotive trainEntity = (Locomotive)ctx.getServerHandler().playerEntity.worldObj.getEntityByID(message.entity);
        if (message.function == 1 && trainEntity !=null) {
           //Get all of the speed/stoppoint/speedchange data.

            PacketThingFromServer packetToSend = new PacketThingFromServer();
            assert packetToSend != null;
            packetToSend.speedLimit = Integer.valueOf(trainEntity.MTC.speedLimit);
            packetToSend.nextSpeedLimit = trainEntity.MTC.nextSpeedLimit;
            packetToSend.xFromStopPoint = trainEntity.MTC.xFromStopPoint;
            packetToSend.yFromStopPoint = trainEntity.MTC.yFromStopPoint;
            packetToSend.zFromStopPoint = trainEntity.MTC.zFromStopPoint;

            packetToSend.xFromSpeedChange = trainEntity.MTC.xSpeedLimitChange;
            packetToSend.yFromSpeedChange = trainEntity.MTC.ySpeedLimitChange;
            packetToSend.zFromSpeedChange = trainEntity.MTC.zSpeedLimitChange;
            DebugUtil.println("Completed creation!");
            DebugUtil.println(packetToSend.xFromSpeedChange);
            Traincraft.gsfsrChannel.sendToAll(packetToSend);

        }
        return null;
    }
}
