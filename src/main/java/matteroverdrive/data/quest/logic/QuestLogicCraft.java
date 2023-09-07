package matteroverdrive.data.quest.logic;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import matteroverdrive.api.quest.IQuestReward;
import matteroverdrive.api.quest.QuestStack;
import matteroverdrive.data.quest.QuestItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Random;

public class QuestLogicCraft extends QuestLogicRandomItem {
    int minCraftCount;
    int maxCraftCount;
    int xpPerCraft;

    public QuestLogicCraft(ItemStack itemStack) {
        this(itemStack, 0, 0, 0);
    }

    public QuestLogicCraft(ItemStack itemStack, int minCraftCount, int maxCraftCount) {
        this(itemStack, minCraftCount, maxCraftCount, 0);
    }

    public QuestLogicCraft(ItemStack itemStack, int minCraftCount, int maxCraftCount, int xpPerCraft) {
        init(new QuestItem[]{QuestItem.fromItemStack(itemStack)}, minCraftCount, maxCraftCount, xpPerCraft);
    }

    public QuestLogicCraft(ItemStack[] itemStacks, int minCraftCount, int maxCraftCount, int xpPerCraft) {
        QuestItem[] questItems = new QuestItem[itemStacks.length];
        for (int i = 0; i < itemStacks.length; i++) {
            questItems[i] = QuestItem.fromItemStack(itemStacks[i]);
        }
        init(questItems, minCraftCount, maxCraftCount, xpPerCraft);
    }

    public QuestLogicCraft(QuestItem questItem, int minCraftCount, int maxCraftCount, int xpPerCraft) {
        init(new QuestItem[]{questItem}, minCraftCount, maxCraftCount, xpPerCraft);
    }

    public QuestLogicCraft(QuestItem[] questItem, int minCraftCount, int maxCraftCount, int xpPerCraft) {
        init(questItem, minCraftCount, maxCraftCount, xpPerCraft);
    }

    protected void init(QuestItem[] items, int minCraftCount, int maxCraftCount, int xpPerCraft) {
        init(items);
        this.minCraftCount = minCraftCount;
        this.maxCraftCount = maxCraftCount;
        this.xpPerCraft = xpPerCraft;
    }

    @Override
    public String modifyInfo(QuestStack questStack, String info) {
        info = info.replace("$craftMaxAmount", Integer.toString(getMaxCraftCount(questStack)));
        ItemStack itemStack = getItem(questStack);
        info = info.replace("$craftItem", itemStack != null ? itemStack.getDisplayName() : "Unknown Item");
        return info;
    }

    @Override
    public boolean isObjectiveCompleted(QuestStack questStack, EntityPlayer entityPlayer, int objectiveIndex) {
        return getCraftCount(questStack) >= getMaxCraftCount(questStack);
    }

    public int getCraftCount(QuestStack questStack) {
        if (hasTag(questStack)) {
            return getTag(questStack).getInteger("CraftCount");
        }
        return 0;
    }

    public void setCraftCount(QuestStack questStack, int count) {
        initTag(questStack);
        getTag(questStack).setInteger("CraftCount", count);
    }

    public int getMaxCraftCount(QuestStack questStack) {
        if (hasTag(questStack)) {
            ItemStack itemStack = getItem(questStack);
            return itemStack.stackSize + getTag(questStack).getInteger("MaxCraftCount");
        }
        return 0;
    }

    @Override
    public String modifyObjective(QuestStack questStack, EntityPlayer entityPlayer, String objective, int objectiveIndex) {
        objective = objective.replace("$craftAmount", Integer.toString(getCraftCount(questStack)));
        objective = objective.replace("$craftMaxAmount", Integer.toString(getMaxCraftCount(questStack)));
        ItemStack itemStack = getItem(questStack);
        objective = objective.replace("$craftItem", itemStack != null ? itemStack.getDisplayName() : "Unknown Item");
        return objective;
    }

    @Override
    public void initQuestStack(Random random, QuestStack questStack) {
        initTag(questStack);
        initItemType(random, questStack);
        getTag(questStack).setInteger("MaxCraftCount", random(random, minCraftCount, maxCraftCount));
    }

    @Override
    public boolean onEvent(QuestStack questStack, Event event, EntityPlayer entityPlayer) {
        if (event instanceof PlayerEvent.ItemCraftedEvent) {
            ItemStack itemStack = getItem(questStack);
            if (itemStack != null && ((PlayerEvent.ItemCraftedEvent) event).crafting.isItemEqual(itemStack)) {
                if (getCraftCount(questStack) < getMaxCraftCount(questStack)) {
                    setCraftCount(questStack, getCraftCount(questStack) + 1);

                    if (isObjectiveCompleted(questStack, entityPlayer, 0) && autoComplete) {
                        questStack.markComplited(entityPlayer, false);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onTaken(QuestStack questStack, EntityPlayer entityPlayer) {

    }

    @Override
    public void onCompleted(QuestStack questStack, EntityPlayer entityPlayer) {

    }

    @Override
    public void modifyRewards(QuestStack questStack, EntityPlayer entityPlayer, List<IQuestReward> rewards) {

    }

    @Override
    public int modifyXP(QuestStack questStack, EntityPlayer entityPlayer, int originalXp) {
        return originalXp + xpPerCraft * getMaxCraftCount(questStack);
    }
}
