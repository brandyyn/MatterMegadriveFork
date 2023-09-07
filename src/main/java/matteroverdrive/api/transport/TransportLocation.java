package matteroverdrive.api.transport;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Stores coordinates and the name of transport locations.
 * Used by the transporter.
 */
public class TransportLocation {
    /**
     * The X,Y,Z coordinates of the location.
     */
    public int x, y, z;
    /**
     * The name of the location.
     */
    public String name;

    //region constructors
    public TransportLocation(int x, int y, int z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    public TransportLocation(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.name = ByteBufUtils.readUTF8String(buf);
    }

    public TransportLocation(NBTTagCompound nbt) {
        if (nbt != null) {
            x = nbt.getInteger("tl_x");
            y = nbt.getInteger("tl_y");
            z = nbt.getInteger("tl_z");
            name = nbt.getString("tl_name");
        }
    }

    //endregion
    //region Buffer and NBT
    public void writeToBuffer(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setInteger("tl_x", x);
        nbtTagCompound.setInteger("tl_y", y);
        nbtTagCompound.setInteger("tl_z", z);
        nbtTagCompound.setString("tl_name", name);
    }
    //endregion

    /**
     * Sets the name of the transport location.
     *
     * @param name the new transport location name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the transport location coordinates.
     *
     * @param x the X coordinate of the location.
     * @param y the Y coordinate of the location.
     * @param z the Z coordinate of the location.
     */
    public void setPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Calculates and returns the distance between this location and the given coordinates.
     *
     * @param x1 the given X coordinate.
     * @param y1 the given Y coordinate.
     * @param z1 the given Z coordinate.
     * @return the distance between this transport location and the provided coordinates.
     */
    public int getDistance(int x1, int y1, int z1) {
        return (int) Math.round(Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1) + (z - z1) * (z - z1)));
    }
}
