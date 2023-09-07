package matteroverdrive.network.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import matteroverdrive.machines.transporter.TileEntityMachineTransporter;
import matteroverdrive.network.packet.TileEntityUpdatePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketSyncTransportProgress extends TileEntityUpdatePacket {
    int progress;

    public PacketSyncTransportProgress() {
    }

    public PacketSyncTransportProgress(TileEntityMachineTransporter transporter) {
        super(transporter);
        this.progress = transporter.getTransportTime();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        progress = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(progress);
    }

    public static class ClientHandler extends AbstractClientPacketHandler<PacketSyncTransportProgress> {
        public ClientHandler() {
        }

        @Override
        public IMessage handleClientMessage(EntityPlayer player, PacketSyncTransportProgress message, MessageContext ctx) {
            TileEntity entity = message.getTileEntity(player.worldObj);
            if (entity instanceof TileEntityMachineTransporter) {
                ((TileEntityMachineTransporter) entity).setTransportTime(message.progress);
            }
            return null;
        }
    }
}
