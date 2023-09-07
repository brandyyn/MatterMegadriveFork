package matteroverdrive.network.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import matteroverdrive.api.network.IMatterNetworkDispatcher;
import matteroverdrive.api.network.MatterNetworkTask;
import matteroverdrive.api.network.MatterNetworkTaskState;
import matteroverdrive.network.packet.TileEntityUpdatePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRemoveTask extends TileEntityUpdatePacket {
    int taskIndex;
    byte queueID;
    MatterNetworkTaskState task_state;

    public PacketRemoveTask() {
        super();
    }

    public PacketRemoveTask(TileEntity dispatcher, int taskIndex, byte queueID, MatterNetworkTaskState task_state) {
        super(dispatcher);
        this.taskIndex = taskIndex;
        this.queueID = queueID;
        this.task_state = task_state;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        taskIndex = buf.readInt();
        queueID = buf.readByte();
        task_state = MatterNetworkTaskState.get(buf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(taskIndex);
        buf.writeByte(queueID);
        buf.writeByte(task_state.ordinal());
    }

    public static class ServerHandler extends AbstractServerPacketHandler<PacketRemoveTask> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, PacketRemoveTask message, MessageContext ctx) {
            TileEntity entity = message.getTileEntity(player.worldObj);

            if (entity instanceof IMatterNetworkDispatcher) {
                IMatterNetworkDispatcher<MatterNetworkTask> dispatcher = (IMatterNetworkDispatcher<MatterNetworkTask>) entity;
                dispatcher.getTaskQueue(message.queueID).dropAt(message.taskIndex).setState(message.task_state);
                player.worldObj.markBlockForUpdate(entity.xCoord, entity.yCoord, entity.zCoord);
                entity.markDirty();
            }
            return null;
        }
    }
}
