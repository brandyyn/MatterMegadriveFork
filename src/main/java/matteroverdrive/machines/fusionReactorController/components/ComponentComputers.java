package matteroverdrive.machines.fusionReactorController.components;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.data.Inventory;
import matteroverdrive.machines.MachineComponentAbstract;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.machines.fusionReactorController.TileEntityMachineFusionReactorController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.EnumSet;

@Optional.InterfaceList({
        @Optional.Interface(modid = "ComputerCraft", iface = "dan200.computercraft.api.peripheral.IPeripheral")
})
public class ComponentComputers extends MachineComponentAbstract<TileEntityMachineFusionReactorController> implements IPeripheral {

    private String[] methodNames = new String[]
            {
                    "getStatus",
                    "isValid",
                    "getEnergyGenerated",
                    "getMatterUsed",
                    "getEnergyStored",
                    "getMatterStored"
            };
    private String peripheralName = "mo_fusion_reactor_controller";

    public ComponentComputers(TileEntityMachineFusionReactorController machine) {
        super(machine);
    }

    //region Computer Methods
    private Object[] callMethod(int method, Object[] args) {
        switch (method) {
            case 0:
                return computerGetStatus(args);
            case 1:
                return computerIsValid(args);
            case 2:
                return computerGetEnergyGenerated(args);
            case 3:
                return computerGetMatterUsed(args);
            case 4:
                return computerGetEnergyStored(args);
            case 5:
                return computerGetMatterStored(args);
            default:
                throw new IllegalArgumentException("Invalid method id");
        }
    }

    private Object[] computerGetStatus(Object[] args) {
        return new Object[]{machine.getMonitorInfo()};
    }

    private Object[] computerIsValid(Object[] args) {
        return new Object[]{machine.isValidStructure()};
    }

    private Object[] computerGetEnergyGenerated(Object[] args) {
        return new Object[]{machine.getEnergyPerTick()};
    }

    private Object[] computerGetMatterUsed(Object[] args) {
        return new Object[]{machine.getMatterDrainPerTick()};
    }

    private Object[] computerGetEnergyStored(Object[] args) {
        return new Object[]{machine.getEnergyStored(ForgeDirection.UNKNOWN)};
    }

    private Object[] computerGetMatterStored(Object[] args) {
        return new Object[]{machine.getMatterStored()};
    }
    //endregion

    //region ComputerCraft
    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType() {
        return peripheralName;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames() {
        return methodNames;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        try {
            return callMethod(method, arguments);
        } catch (Exception e) {
            throw new LuaException(e.getMessage());
        }
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void attach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void detach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public boolean equals(IPeripheral other) {
        return false;
    }
    //endregion

    //region Open Computers
    @Optional.Method(modid = "OpenComputers")
    public String[] methods() {
        return methodNames;
    }

    @Optional.Method(modid = "OpenComputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        int methodId = Arrays.asList(methodNames).indexOf(method);

        if (methodId == -1) {
            throw new RuntimeException("The method " + method + " does not exist");
        }

        return callMethod(methodId, args.toArray());
    }

    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return peripheralName;
    }
    //endregion

    //region Component Functions
    @Override
    public void readFromNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {

    }

    @Override
    public void registerSlots(Inventory inventory) {

    }

    @Override
    public void update(TileEntityMachineFusionReactorController machine) {

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
    public void onActiveChange(TileEntityMachineFusionReactorController machine) {

    }

    @Override
    public void onAwake(TileEntityMachineFusionReactorController machine, Side side) {

    }

    @Override
    public void onPlaced(World world, EntityLivingBase entityLiving, TileEntityMachineFusionReactorController machine) {

    }
    //endregion
}
