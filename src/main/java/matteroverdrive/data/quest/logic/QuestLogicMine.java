package matteroverdrive.data.quest.logic;

import cpw.mods.fml.common.eventhandler.Event;
import matteroverdrive.api.quest.IQuestReward;
import matteroverdrive.api.quest.QuestStack;
import matteroverdrive.data.quest.QuestBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestLogicMine extends AbstractQuestLogic {
    QuestBlock[] blocks;
    boolean hasMetadata;
    int metadata;
    int minMineCount;
    int maxMineCount;
    int xpPerMine;
    boolean randomBlock;

    public QuestLogicMine(Block block, int minMineCount, int maxMineCount, int xpPerMine) {
        this.blocks = new QuestBlock[]{QuestBlock.fromBlock(block)};
        this.minMineCount = minMineCount;
        this.maxMineCount = maxMineCount;
        this.xpPerMine = xpPerMine;
        this.randomBlock = true;
    }

    public QuestLogicMine(Block block, int minMineCount, int maxMineCount, int xpPerMine, int metadata) {
        this(block, minMineCount, maxMineCount, xpPerMine);
        this.metadata = metadata;
        this.hasMetadata = true;
    }

    @Override
    public String modifyInfo(QuestStack questStack, String info) {
        info = info.replace("$maxMineAmount", Integer.toString(getMaxMineCount(questStack)));
        Block block = getBlock(questStack);
        info = info.replace("$mineBlock", block != null ? block.getLocalizedName() : "Unknown Block");
        return info;
    }

    @Override
    public boolean isObjectiveCompleted(QuestStack questStack, EntityPlayer entityPlayer, int objectiveIndex) {
        return getMineCount(questStack) >= getMaxMineCount(questStack);
    }

    @Override
    public String modifyObjective(QuestStack questStack, EntityPlayer entityPlayer, String objective, int objectiveIndex) {
        objective = objective.replace("$mineAmount", Integer.toString(getMineCount(questStack)));
        objective = objective.replace("$maxMineAmount", Integer.toString(getMaxMineCount(questStack)));
        Block block = getBlock(questStack);
        objective = objective.replace("$mineBlock", block != null ? block.getLocalizedName() : "Unknown Block");
        return objective;
    }

    @Override
    public void initQuestStack(Random random, QuestStack questStack) {
        initTag(questStack);
        initBlockType(random, questStack);
        getTag(questStack).setInteger("MaxMineCount", random(random, minMineCount, maxMineCount));
    }

    private void initBlockType(Random random, QuestStack questStack) {
        if (randomBlock) {
            List<Integer> avalibleBlocks = new ArrayList<>();
            for (int i = 0; i < blocks.length; i++) {
                Block block = blocks[i].getBlock();
                if (block != null) {
                    avalibleBlocks.add(i);
                }
            }
            if (avalibleBlocks.size() > 0) {
                setBlockType(questStack, avalibleBlocks.get(random.nextInt(avalibleBlocks.size())));
            }
        } else {
            for (int i = 0; i < blocks.length; i++) {
                Block block = blocks[i].getBlock();
                if (block != null) {
                    setBlockType(questStack, i);
                }
            }
        }
    }

    @Override
    public boolean onEvent(QuestStack questStack, Event event, EntityPlayer entityPlayer) {
        if (event instanceof BlockEvent.HarvestDropsEvent) {
            BlockEvent.HarvestDropsEvent harvestEvent = (BlockEvent.HarvestDropsEvent) event;
            Block block = getBlock(questStack);
            if (block != null && harvestEvent.block == block && (!hasMetadata || harvestEvent.blockMetadata == metadata)) {
                if (getMineCount(questStack) < getMaxMineCount(questStack)) {
                    setMineCount(questStack, getMineCount(questStack) + 1);
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
    public int modifyXP(QuestStack questStack, EntityPlayer entityPlayer, int originalXp) {
        return originalXp + getMaxMineCount(questStack) * xpPerMine;
    }

    @Override
    public void onCompleted(QuestStack questStack, EntityPlayer entityPlayer) {

    }

    @Override
    public void modifyRewards(QuestStack questStack, EntityPlayer entityPlayer, List<IQuestReward> rewards) {

    }

    public int getMineCount(QuestStack questStack) {
        if (hasTag(questStack)) {
            return getTag(questStack).getInteger("MineCount");
        }
        return 0;
    }

    public void setMineCount(QuestStack questStack, int mineCount) {
        initTag(questStack);
        getTag(questStack).setInteger("MineCount", mineCount);
    }

    public int getMaxMineCount(QuestStack questStack) {
        if (hasTag(questStack)) {
            return getTag(questStack).getInteger("MaxMineCount");
        }
        return 0;
    }

    public int getBlockType(QuestStack questStack) {
        if (hasTag(questStack)) {
            return getTag(questStack).getByte("BlockType");
        }
        return 0;
    }

    public void setBlockType(QuestStack questStack, int blockType) {
        initTag(questStack);
        getTag(questStack).setByte("BlockType", (byte) blockType);
    }

    public Block getBlock(QuestStack questStack) {
        int blockType = getBlockType(questStack);
        if (blockType < blocks.length) {
            return blocks[blockType].getBlock();
        }
        return null;
    }

    public QuestLogicMine setRandomBlock(boolean randomBlock) {
        this.randomBlock = randomBlock;
        return this;
    }
}
