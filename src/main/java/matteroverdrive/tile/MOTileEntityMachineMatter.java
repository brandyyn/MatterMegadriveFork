package matteroverdrive.tile;

import cpw.mods.fml.common.Optional;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.matter.IMatterHandler;
import matteroverdrive.compat.modules.waila.IWailaBodyProvider;
import matteroverdrive.data.MachineMatterStorage;
import matteroverdrive.fluids.FluidMatterPlasma;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.network.packet.client.PacketMatterUpdate;
import matteroverdrive.util.MatterHelper;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.EnumSet;
import java.util.List;

public abstract class MOTileEntityMachineMatter extends MOTileEntityMachineEnergy implements IMatterHandler, IWailaBodyProvider, IFluidHandler {
    protected MachineMatterStorage matterStorage;

    public MOTileEntityMachineMatter(int upgradesCount) {
        super(upgradesCount);
        matterStorage = new MachineMatterStorage(this, 32768);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
        super.writeCustomNBT(nbt, categories, toDisk);
        if (categories.contains(MachineNBTCategory.DATA) && getMatterStorage() != null) {
            getMatterStorage().writeToNBT(nbt);
        }

    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
        super.readCustomNBT(nbt, categories);
        if (categories.contains(MachineNBTCategory.DATA) && getMatterStorage() != null) {
            getMatterStorage().readFromNBT(nbt);
        }

    }

    @Override
    public int getMatterStored() {
        if (getMatterStorage() != null)
            return this.getMatterStorage().getMatterStored();
        return 0;
    }

    @Override
    public int getMatterCapacity() {
        if (getMatterStorage() != null)
            return getMatterStorage().getCapacity();
        return 0;
    }

    @Override
    public int receiveMatter(ForgeDirection side, int amount, boolean simulate) {
        if (getMatterStorage() != null)
            return getMatterStorage().receiveMatter(side, amount, simulate);
        return 0;
    }

    @Override
    public int extractMatter(ForgeDirection direction, int amount, boolean simulate) {
        if (getMatterStorage() != null)
            return getMatterStorage().extractMatter(direction, amount, simulate);
        return 0;
    }

    protected int modifyEnergyStored(int amount) {
        int energyModified = energyStorage.modifyEnergyStored(amount);
        if (energyModified != 0) {
            UpdateClientPower();
        }
        return energyModified;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (getMatterStorage() != null)
            return getMatterStorage().fill(resource, doFill);
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (getMatterStorage() != null)
            return getMatterStorage().drain(resource.amount, doDrain);
        else return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (getMatterStorage() != null)
            return getMatterStorage().drain(maxDrain, doDrain);
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluid instanceof FluidMatterPlasma;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return fluid instanceof FluidMatterPlasma;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        if (getMatterStorage() != null)
            return new FluidTankInfo[]{getMatterStorage().getInfo()};
        return new FluidTankInfo[0];
    }

    public MachineMatterStorage getMatterStorage()

    {
        return this.matterStorage;
    }

    public void setMatterStored(int matter) {
        if (getMatterStorage() != null)
            getMatterStorage().setMatterStored(matter);
    }

    public void updateClientMatter() {
        MatterOverdrive.packetPipeline.sendToAllAround(new PacketMatterUpdate(this), this, 64);
    }

    @Override
    public void readFromPlaceItem(ItemStack itemStack) {
        super.readFromPlaceItem(itemStack);

        if (itemStack != null && getMatterStorage() != null) {
            if (itemStack.hasTagCompound()) {
                getMatterStorage().readFromNBT(itemStack.getTagCompound());
            }
        }
    }

    @Override
    public void writeToDropItem(ItemStack itemStack) {
        super.writeToDropItem(itemStack);

        if (itemStack != null && getMatterStorage() != null) {
            if (getMatterStorage().getMatterStored() > 0) {
                if (!itemStack.hasTagCompound())
                    itemStack.setTagCompound(new NBTTagCompound());

                getMatterStorage().writeToNBT(itemStack.getTagCompound());
                itemStack.getTagCompound().setInteger("MaxMatter", matterStorage.getCapacity());
                itemStack.getTagCompound().setInteger("MatterSend", matterStorage.getMaxExtract());
                itemStack.getTagCompound().setInteger("MatterReceive", matterStorage.getMaxReceive());
            }
        }
    }

    //	WAILA
    @Optional.Method(modid = "Waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();

        if (te instanceof MOTileEntityMachineMatter) {
            MOTileEntityMachineMatter machine = (MOTileEntityMachineMatter) te;
            currenttip.add(EnumChatFormatting.AQUA + String.format("%s / %s %s", machine.getMatterStored(), machine.getMatterCapacity(), MatterHelper.MATTER_UNIT));

        } else {
            throw new RuntimeException("MOTileEntityMachineMatter WAILA provider is being used for something that is not a MOTileEntityMachineMatter: " + te.getClass());
        }

        return currenttip;
    }
}
