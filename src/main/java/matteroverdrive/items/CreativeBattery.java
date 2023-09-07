package matteroverdrive.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.client.data.Color;
import matteroverdrive.util.MOEnergyHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CreativeBattery extends Battery {
    public CreativeBattery(String name, int capacity, Color color, int input, int output) {
        super(name, capacity, color, input, output);
        this.setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List list) {
        ItemStack unpowered = new ItemStack(item);
        list.add(unpowered);
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        return capacity;
    }

    @Override
    public void setEnergyStored(ItemStack container, int amount) {
        MOEnergyHelper.setDefaultEnergyTag(container, capacity);
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        return Math.min(this.maxExtract, maxExtract);
    }

    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        return 0;
    }

}
