package matteroverdrive.starmap.data;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.starmap.GalacticPosition;
import matteroverdrive.util.MOLog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TravelEvent {
    //region Private Vars
    private long timeStart;
    private int timeLength;
    private GalacticPosition from, to;
    private ItemStack ship;
    //endregion

    //region Constructors
    public TravelEvent() {

    }

    public TravelEvent(NBTTagCompound tagCompound) {
        readFromNBT(tagCompound);
    }

    public TravelEvent(ByteBuf buf) {
        readFromBuffer(buf);
    }

    public TravelEvent(World world, GalacticPosition from, GalacticPosition to, ItemStack shipStack, Galaxy galaxy) {
        timeStart = world.getTotalWorldTime();
        this.from = from;
        this.to = to;
        this.ship = shipStack;
        calculateTravelTime(galaxy, from, to);
    }
    //endregion

    private void calculateTravelTime(Galaxy galaxy, GalacticPosition from, GalacticPosition to) {
        this.timeLength = (int) (from.distanceToLY(galaxy, to) * Galaxy.LY_TO_TICKS);
        if (this.timeLength == 0)
            this.timeLength = (int) (from.distanceToAU(galaxy, to) * Galaxy.AU_TO_TICKS);
    }

    //region Read - Write
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagCompound shipStackNBT = new NBTTagCompound();
        if (ship != null) {
            ship.writeToNBT(shipStackNBT);
            tagCompound.setTag("Ship", shipStackNBT);
        }
        tagCompound.setInteger("TimeLength", timeLength);
        tagCompound.setLong("TimeStart", timeStart);
        tagCompound.setTag("From", from.toNBT());
        tagCompound.setTag("To", to.toNBT());
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        from = new GalacticPosition(tagCompound.getCompoundTag("From"));
        to = new GalacticPosition(tagCompound.getCompoundTag("To"));
        if (tagCompound.hasKey("Ship", Constants.NBT.TAG_COMPOUND)) {
            try {
                ship = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("Ship"));
            } catch (Exception e) {
                MOLog.warn("Could not load ship from NBT in travel event", e);
            }
        }
        timeLength = tagCompound.getInteger("TimeLength");
        timeStart = tagCompound.getLong("TimeStart");
    }

    public void readFromBuffer(ByteBuf buf) {
        from = new GalacticPosition(buf);
        to = new GalacticPosition(buf);
        ship = ByteBufUtils.readItemStack(buf);
        timeLength = buf.readInt();
        timeStart = buf.readLong();
    }

    public void writeToBuffer(ByteBuf buf) {
        from.writeToBuffer(buf);
        to.writeToBuffer(buf);
        ByteBufUtils.writeItemStack(buf, ship);
        buf.writeInt(timeLength);
        buf.writeLong(timeStart);
    }
    //endregion

    //region Getters and Setters
    public int getTimeLength() {
        return (int) Math.ceil(timeLength * Galaxy.GALAXY_TRAVEL_TIME_MULTIPLY);
    }

    public void setTimeLength(int timeLength) {
        this.timeLength = timeLength;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public long getTimeRemainning(World world) {
        return (timeStart + getTimeLength()) - world.getTotalWorldTime();
    }

    public double getPercent(World world) {
        return 1d - (double) ((timeStart + timeLength) - world.getTotalWorldTime()) / (double) timeLength;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public ItemStack getShip() {
        return ship;
    }

    public void setShip(ItemStack ship) {
        this.ship = ship;
    }

    public GalacticPosition getTo() {
        return to;
    }

    public void setTo(GalacticPosition to) {
        this.to = to;
    }

    public GalacticPosition getFrom() {
        return from;
    }

    public void setFrom(GalacticPosition from) {
        this.from = from;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        writeToNBT(tagCompound);
        return tagCompound;
    }

    public boolean isValid(Galaxy galaxy) {
        if (this.from != null && this.to != null) {
            Planet from = galaxy.getPlanet(this.from);
            Planet to = galaxy.getPlanet(this.to);
            if (from != null && to != null) {
                return ship != null;
            }
        }
        return false;
    }

    public boolean isComplete(World world) {
        return getTimeRemainning(world) <= 0;
    }
    //endregion
}
