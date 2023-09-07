package matteroverdrive.machines.components;


import cpw.mods.fml.relauncher.Side;
import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.api.network.IMatterNetworkFilter;
import matteroverdrive.data.Inventory;
import matteroverdrive.data.inventory.DestinationFilterSlot;
import matteroverdrive.machines.MOTileEntityMachine;
import matteroverdrive.machines.MachineComponentAbstract;
import matteroverdrive.machines.MachineNBTCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.EnumSet;

public class ComponentMatterNetworkConfigs extends MachineComponentAbstract<MOTileEntityMachine> {
    private String destinationFilter;
    private int destinationFilterSlot;

    public ComponentMatterNetworkConfigs(MOTileEntityMachine machine) {
        super(machine);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
        if (categories.contains(MachineNBTCategory.CONFIGS)) {
            if (nbt.hasKey("DestinationFilter", Constants.NBT.TAG_STRING))
                destinationFilter = nbt.getString("DestinationFilter");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
        if (categories.contains(MachineNBTCategory.CONFIGS)) {
            if (destinationFilter != null)
                nbt.setString("DestinationFilter", destinationFilter);
        }
    }

    @Override
    public void registerSlots(Inventory inventory) {
        destinationFilterSlot = inventory.AddSlot(new DestinationFilterSlot(false));
    }

    @Override
    public void update(MOTileEntityMachine machine) {

    }

    @Override
    public boolean isAffectedByUpgrade(UpgradeTypes type) {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void onActiveChange(MOTileEntityMachine machine) {

    }

    @Override
    public void onAwake(MOTileEntityMachine machine, Side side) {

    }

    @Override
    public void onPlaced(World world, EntityLivingBase entityLiving, MOTileEntityMachine machine) {

    }

    public NBTTagCompound getFilter() {
        if (machine.getStackInSlot(destinationFilterSlot) != null && machine.getStackInSlot(destinationFilterSlot).getItem() instanceof IMatterNetworkFilter) {
            return ((IMatterNetworkFilter) machine.getStackInSlot(destinationFilterSlot).getItem()).getFilter(machine.getStackInSlot(destinationFilterSlot));
        }
        return null;
    }

    public int getDestinationFilterSlot() {
        return destinationFilterSlot;
    }

    public void setDestinationFilter(String destinationFilter) {
        this.destinationFilter = destinationFilter;
    }

    public String getDestinationFilter() {
        return destinationFilter;
    }
}
