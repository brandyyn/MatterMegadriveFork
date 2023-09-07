package matteroverdrive.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.Reference;
import matteroverdrive.client.data.Color;
import matteroverdrive.items.includes.MOItemEnergyContainer;
import matteroverdrive.util.MOEnergyHelper;
import matteroverdrive.util.MOStringHelper;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

import java.util.List;

public class Battery extends MOItemEnergyContainer {
    Color color;
    IIcon overlay;

    public Battery(String name, int capacity, Color color, int input, int output) {
        super(name, capacity, input, output);
        this.color = color;
        this.setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List list) {
        ItemStack unpowered = new ItemStack(item);
        ItemStack powered = new ItemStack(item);
        setEnergyStored(powered, getMaxEnergyStored(powered));
        list.add(unpowered);
        list.add(powered);
    }

    @Override
    public void addDetails(ItemStack itemstack, EntityPlayer player, List infos) {
        super.addDetails(itemstack, player, infos);
        infos.add(EnumChatFormatting.GRAY + MOStringHelper.translateToLocal("gui.tooltip.energy.io") + ": " + maxReceive + "/" + maxExtract + MOEnergyHelper.ENERGY_UNIT + "/t");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister p_94581_1_) {
        this.itemIcon = p_94581_1_.registerIcon(Reference.MOD_ID + ":" + "battery");
        overlay = p_94581_1_.registerIcon(Reference.MOD_ID + ":" + "battery_overlay");
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 2;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        if (pass == 1) {
            return overlay;
        } else {
            return itemIcon;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack itemStack, int pass) {
        if (pass == 1) {
            return color.getColor();
        }
        return super.getColorFromItemStack(itemStack, pass);
    }

    @Override
    public void setEnergyStored(ItemStack container, int amount) {
        super.setEnergyStored(container, amount);
    }
}
