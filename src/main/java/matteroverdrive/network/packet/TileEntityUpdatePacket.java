package matteroverdrive.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityUpdatePacket extends PacketAbstract {
    public int x;
    public int y;
    public int z;

    public TileEntityUpdatePacket() {
        super();
    }

    public TileEntityUpdatePacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public TileEntityUpdatePacket(TileEntity entity) {
        this(entity.xCoord, entity.yCoord, entity.zCoord);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public TileEntity getTileEntity(World world) {
        return world.getTileEntity(x, y, z);
    }
}
