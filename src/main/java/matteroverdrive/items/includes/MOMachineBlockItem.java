package matteroverdrive.items.includes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.api.inventory.IUpgrade;
import matteroverdrive.util.MOEnergyHelper;
import matteroverdrive.util.MOStringHelper;
import matteroverdrive.util.MatterHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MOMachineBlockItem extends ItemBlock {
    public MOMachineBlockItem(Block block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List infos, boolean p_77624_4_) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            if (MOStringHelper.hasTranslation(getUnlocalizedName() + ".details")) {
                infos.add(EnumChatFormatting.GRAY + MOStringHelper.translateToLocal(getUnlocalizedName() + ".details"));
            }

            if (stack.hasTagCompound()) {
                if (stack.getTagCompound().hasKey("Energy") && stack.getTagCompound().hasKey("MaxEnergy")) {
                    infos.add(EnumChatFormatting.YELLOW + MOEnergyHelper.formatEnergy(stack.getTagCompound().getInteger("Energy"), stack.getTagCompound().getInteger("MaxEnergy")));
                    if (stack.getTagCompound().hasKey("PowerSend") && stack.getTagCompound().hasKey("PowerReceive")) {
                        infos.add("Send/Receive: " + MOStringHelper.formatNumber(stack.getTagCompound().getInteger("PowerSend")) + "/" + MOStringHelper.formatNumber(stack.getTagCompound().getInteger("PowerReceive")) + MOEnergyHelper.ENERGY_UNIT + "/t");
                    }
                }
                if (stack.getTagCompound().hasKey("Matter") && stack.getTagCompound().hasKey("MaxMatter")) {
                    infos.add(EnumChatFormatting.BLUE + MatterHelper.formatMatter(stack.getTagCompound().getInteger("Matter"), stack.getTagCompound().getInteger("MaxMatter")));

                    if (stack.getTagCompound().hasKey("MatterSend") && stack.getTagCompound().hasKey("MatterReceive")) {
                        infos.add(EnumChatFormatting.DARK_BLUE + "Send/Receive: " + MOStringHelper.formatNumber(stack.getTagCompound().getInteger("MatterSend")) + "/" + MOStringHelper.formatNumber(stack.getTagCompound().getInteger("MatterReceive")) + MatterHelper.MATTER_UNIT + "/t");
                    }
                }

                showItems(stack, player, infos);
            }
        } else {
            infos.add(MOStringHelper.MORE_INFO);
        }


    }

    public String getItemStackDisplayName(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            return super.getItemStackDisplayName(itemStack) + String.format(EnumChatFormatting.AQUA + " [%s]" + EnumChatFormatting.RESET, MOStringHelper.translateToLocal("item.info.configured"));
        } else {
            return super.getItemStackDisplayName(itemStack);
        }
    }

    @Override
    public int getDamage(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Energy") && stack.getTagCompound().hasKey("MaxEnergy")) {
            return stack.getTagCompound().getInteger("MaxEnergy") - stack.getTagCompound().getInteger("Energy") + 1;
        }
        return 0;
    }

    @Override
    public int getDisplayDamage(ItemStack stack) {
        return this.getDamage(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("MaxEnergy")) {
            return stack.getTagCompound().getInteger("MaxEnergy");
        }
        return 0;
    }

    private void showItems(ItemStack itemStack, EntityPlayer player, List infos) {
        NBTTagList stackTagList = itemStack.getTagCompound().getCompoundTag("Machine").getTagList("Items", Constants.NBT.TAG_COMPOUND);

        if (stackTagList.tagCount() > 0) {
            infos.add("");
            infos.add(EnumChatFormatting.YELLOW + "Inventory:");
            for (int i = 0; i < stackTagList.tagCount(); i++) {
                ItemStack stack = ItemStack.loadItemStackFromNBT(stackTagList.getCompoundTagAt(i));
                if (stack.getItem() instanceof IUpgrade) {
                    infos.add("   " + EnumChatFormatting.GREEN + stack.getDisplayName());
                } else {
                    infos.add("   " + infos.add(stack.getDisplayName()));
                }
            }
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getDamage(stack) > 0;
    }
}
