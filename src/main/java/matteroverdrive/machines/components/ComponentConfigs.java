package matteroverdrive.machines.components;

import cpw.mods.fml.relauncher.Side;
import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.data.Inventory;
import matteroverdrive.machines.MOTileEntityMachine;
import matteroverdrive.machines.MachineComponentAbstract;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.machines.configs.IConfigProperty;
import matteroverdrive.machines.configs.IConfigurable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ComponentConfigs extends MachineComponentAbstract<MOTileEntityMachine> implements IConfigurable {
    private Map<String, IConfigProperty> propertyMap;

    public ComponentConfigs(MOTileEntityMachine machine) {
        super(machine);
        propertyMap = new HashMap<>();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
        if (categories.contains(MachineNBTCategory.CONFIGS)) {
            for (IConfigProperty property : propertyMap.values()) {
                property.readFromNBT(nbt);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
        if (categories.contains(MachineNBTCategory.CONFIGS)) {
            for (IConfigProperty property : propertyMap.values()) {
                property.writeToNBT(nbt);
            }
        }
    }

    @Override
    public void registerSlots(Inventory inventory) {

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

    @Override
    public Map<String, IConfigProperty> getValues() {
        return propertyMap;
    }

    @Override
    public IConfigProperty getProperty(String name) {
        return propertyMap.get(name);
    }

    public void addProperty(IConfigProperty property) {
        propertyMap.put(property.getKey(), property);
    }

    public boolean getBoolean(String key, boolean def) {
        IConfigProperty property = propertyMap.get(key);
        if (property != null && property.getType() == Boolean.class) {
            return (Boolean) property.getValue();
        }
        return def;
    }

    public Integer getInteger(String key, int def) {
        IConfigProperty property = propertyMap.get(key);
        if (property != null && property.getType().equals(Integer.class)) {
            return (Integer) property.getValue();
        }
        return def;
    }

    public Integer getEnum(String key, int def) {
        IConfigProperty property = propertyMap.get(key);
        if (property != null && property.getType().equals(Enum.class)) {
            return (Integer) property.getValue();
        }
        return def;
    }

    public String getString(String key, String def) {
        IConfigProperty property = propertyMap.get(key);
        if (property != null && property.getType().equals(String.class)) {
            return (String) property.getValue();
        }
        return def;
    }
}
