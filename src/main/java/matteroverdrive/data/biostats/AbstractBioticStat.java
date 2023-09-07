package matteroverdrive.data.biostats;

import matteroverdrive.api.android.IBionicStat;
import matteroverdrive.client.render.HoloIcon;
import matteroverdrive.client.render.HoloIcons;
import matteroverdrive.entity.player.AndroidPlayer;
import matteroverdrive.util.MOStringHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBioticStat implements IBionicStat {
    int xp;
    String name;
    IBionicStat root;
    List<IBionicStat> competitors;
    List<ItemStack> requiredItems;
    List<IBionicStat> enabledBlacklist;
    int maxLevel;
    boolean showOnHud;
    boolean showOnWheel;
    HoloIcon icon;

    public AbstractBioticStat(String name, int xp) {
        this.name = name;
        this.xp = xp;
        competitors = new ArrayList<>();
        requiredItems = new ArrayList<>();
        enabledBlacklist = new ArrayList<>();
        maxLevel = 1;
    }

    @Override
    public String getUnlocalizedName() {
        return name;
    }

    @Override
    public String getDisplayName(AndroidPlayer androidPlayer, int level) {
        return MOStringHelper.translateToLocal("biotic_stat." + name + ".name");
    }

    @Override
    public boolean isEnabled(AndroidPlayer android, int level) {
        return checkBlacklistActive(android, level);
    }

    public String getDetails(int level) {
        return MOStringHelper.translateToLocal("biotic_stat." + name + ".details");
    }

    @Override
    public boolean canBeUnlocked(AndroidPlayer android, int level) {
        //if the root is not unlocked then this stat can't be unlocked
        if (root != null && !android.isUnlocked(root, root.maxLevel())) {
            return false;
        }
        //if any of the competitors are unlocked then this stat can't be unlocked
        if (areCompeditrosUnlocked(android))
            return false;
        if (requiredItems.size() > 0 && !android.getPlayer().capabilities.isCreativeMode) {
            for (ItemStack item : requiredItems) {

                if (!hasItem(android, item)) {
                    return false;
                }
            }
        }
        return android.isAndroid() && (android.getPlayer().capabilities.isCreativeMode || android.getPlayer().experienceLevel >= xp);
    }

    protected boolean hasItem(AndroidPlayer player, ItemStack stack) {
        int amountCount = stack.stackSize;
        for (int i = 0; i < player.getPlayer().inventory.getSizeInventory(); i++) {
            ItemStack s = player.getPlayer().inventory.getStackInSlot(i);
            if (s != null && s.isItemEqual(stack)) {
                amountCount -= s.stackSize;
            }
        }

        return amountCount <= 0;
    }

    @Override
    public void onUnlock(AndroidPlayer android, int level) {
        android.getPlayer().addExperienceLevel(-xp);
        consumeItems(android);
    }

    //consume all the necessary items from the player inventory
    //does not check if the items exist
    protected void consumeItems(AndroidPlayer androidPlayer) {
        for (ItemStack itemStack : requiredItems) {
            int itemCount = itemStack.stackSize;
            for (int j = 0; j < androidPlayer.getPlayer().inventory.getSizeInventory(); j++) {
                ItemStack pStack = androidPlayer.getPlayer().inventory.getStackInSlot(j);
                if (pStack != null && pStack.isItemEqual(itemStack)) {
                    int countShouldTake = Math.min(itemCount, pStack.stackSize);
                    androidPlayer.getPlayer().inventory.decrStackSize(j, countShouldTake);
                    itemCount -= countShouldTake;
                }

                if (itemCount <= 0)
                    return;
            }
        }
    }

    @Override
    public void onTooltip(AndroidPlayer android, int level, List<String> list, int mouseX, int mouseY) {
        String name = getDisplayName(android, level);
        if (maxLevel() > 1) {
            name += String.format(" [%s/%s]", level, maxLevel());
        }
        list.add(EnumChatFormatting.WHITE + name);
        String details = getDetails(level);
        String[] detailsSplit = details.split("/n/");
        for (String detail : detailsSplit) {
            list.add(EnumChatFormatting.GRAY + detail);
        }

        String requires = "";

        if (root != null) {
            requires += EnumChatFormatting.GOLD + String.format("[%s%s]", root.getDisplayName(android, 0), root.maxLevel() > 1 ? " " + root.maxLevel() : "");
        }

        if (requiredItems.size() > 0) {
            for (ItemStack itemStack : requiredItems) {
                if (!requires.isEmpty()) {
                    requires += EnumChatFormatting.GRAY + ", ";
                }
                if (itemStack.stackSize > 1) {
                    requires += EnumChatFormatting.WHITE.toString() + itemStack.stackSize + "x";
                }

                requires += EnumChatFormatting.WHITE + "[" + itemStack.getDisplayName() + "]";
            }
        }

        if (!requires.isEmpty()) {
            list.add(MOStringHelper.translateToLocal("gui.tooltip.requires") + ": " + requires);
        }

        if (competitors.size() > 0) {
            String locks = EnumChatFormatting.RED + MOStringHelper.translateToLocal("gui.tooltip.locks") + ": ";
            for (IBionicStat compeditor : competitors) {
                locks += String.format("[%s] ", compeditor.getDisplayName(android, 0));
            }
            list.add(locks);
        }

        if (level < maxLevel()) {
            list.add((android.getPlayer().experienceLevel < xp ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + "XP: " + xp);
        }
    }

    public boolean checkBlacklistActive(AndroidPlayer androidPlayer, int level) {
        for (IBionicStat stat : enabledBlacklist) {
            if (stat.isActive(androidPlayer, level)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void registerIcons(HoloIcons holoIcons) {
        icon = holoIcons.registerIcon("biotic_stat_" + name, 18);
    }

    public void addReqiredItm(ItemStack stack) {
        requiredItems.add(stack);
    }

    @Override
    public boolean showOnHud(AndroidPlayer android, int level) {
        return showOnHud;
    }

    @Override
    public boolean showOnWheel(AndroidPlayer androidPlayer, int level) {
        return showOnWheel;
    }

    @Override
    public int maxLevel() {
        return maxLevel;
    }

    public IBionicStat getRoot() {
        return root;
    }

    public void setRoot(IBionicStat stat) {
        this.root = stat;
    }

    public void addCompetitor(IBionicStat stat) {
        this.competitors.add(stat);
    }

    public void removeCompetitor(IBionicStat competitor) {
        this.competitors.remove(competitor);
    }

    public List<IBionicStat> getCompetitors() {
        return competitors;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setShowOnHud(boolean showOnHud) {
        this.showOnHud = showOnHud;
    }

    public void setShowOnWheel(boolean showOnWheel) {
        this.showOnWheel = showOnWheel;
    }


    public List<ItemStack> getRequiredItems() {
        return requiredItems;
    }

    public List<IBionicStat> getEnabledBlacklist() {
        return enabledBlacklist;
    }

    public void addToEnabledBlacklist(IBionicStat stat) {
        enabledBlacklist.add(stat);
    }

    @Override
    public HoloIcon getIcon(int level) {
        return icon;
    }

    @Override
    public int getXP(AndroidPlayer androidPlayer, int level) {
        return xp;
    }

    public boolean areCompeditrosUnlocked(AndroidPlayer androidPlayer) {
        if (competitors.size() > 0) {
            for (IBionicStat competitor : competitors) {
                if (androidPlayer.isUnlocked(competitor, 0)) {
                    return true;
                }
            }
        }
        return false;
    }
}
