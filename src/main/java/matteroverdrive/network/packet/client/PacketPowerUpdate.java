package matteroverdrive.network.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import matteroverdrive.network.packet.TileEntityUpdatePacket;
import matteroverdrive.tile.MOTileEntityMachineEnergy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketPowerUpdate extends TileEntityUpdatePacket {
    int energy;

    public PacketPowerUpdate() {
    }

    public PacketPowerUpdate(MOTileEntityMachineEnergy entityMachineEnergy) {
        super(entityMachineEnergy.xCoord, entityMachineEnergy.yCoord, entityMachineEnergy.zCoord);
        energy = entityMachineEnergy.getEnergyStorage().getEnergyStored();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        energy = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(energy);
    }

    public static class ClientHandler extends AbstractClientPacketHandler<PacketPowerUpdate> {

        public ClientHandler() {
        }

        @Override
        public IMessage handleClientMessage(EntityPlayer player, PacketPowerUpdate message, MessageContext ctx) {
            TileEntity tileEntity = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (tileEntity instanceof MOTileEntityMachineEnergy) {
                ((MOTileEntityMachineEnergy) tileEntity).setEnergyStored(message.energy);
            }
            return null;
        }
    }
}
