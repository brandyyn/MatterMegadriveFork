package matteroverdrive.items.food;

import cpw.mods.fml.common.registry.GameRegistry;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.entity.player.AndroidPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class RomulanAle extends ItemFood {

    public RomulanAle(String name) {
        super(4, 0.6f, false);
        setUnlocalizedName(name);
        setTextureName(Reference.MOD_ID + ":" + name);
        setAlwaysEdible();
    }

    public void register() {
        setCreativeTab(MatterOverdrive.tabMatterOverdrive_food);
        GameRegistry.registerItem(this, getUnlocalizedName().substring(5));
    }

    @Override
    public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
        super.onEaten(itemStack, world, player);

        if (!player.capabilities.isCreativeMode && !world.isRemote) {
            --itemStack.stackSize;
        }


        if (!AndroidPlayer.get(player).isAndroid()) player.addPotionEffect(new PotionEffect(9, 160, 8));

        if (itemStack.stackSize > 0) {
            player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
            return itemStack;
        } else {
            return new ItemStack(Items.glass_bottle);
        }
    }


    @Override
    public EnumAction getItemUseAction(ItemStack p_77661_1_) {
        return EnumAction.drink;
    }
}
