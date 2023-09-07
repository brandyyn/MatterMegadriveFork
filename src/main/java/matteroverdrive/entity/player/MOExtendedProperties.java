package matteroverdrive.entity.player;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.events.MOEventQuest;
import matteroverdrive.api.quest.IQuestReward;
import matteroverdrive.api.quest.QuestStack;
import matteroverdrive.data.quest.PlayerQuestData;
import matteroverdrive.gui.GuiDataPad;
import matteroverdrive.init.MatterOverdriveItems;
import matteroverdrive.network.packet.client.quest.PacketSyncQuests;
import matteroverdrive.network.packet.client.quest.PacketUpdateQuest;
import matteroverdrive.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MOExtendedProperties implements IExtendedEntityProperties {
    public static final String EXT_PROP_NAME = "MOPlayer";
    private EntityPlayer player;
    private PlayerQuestData questData;

    public MOExtendedProperties(EntityPlayer player) {
        this.player = player;
        questData = new PlayerQuestData(this);
    }

    public static void register(EntityPlayer player) {
        player.registerExtendedProperties(EXT_PROP_NAME, new MOExtendedProperties(player));
    }

    public static MOExtendedProperties get(EntityPlayer player) {
        return (MOExtendedProperties) player.getExtendedProperties(EXT_PROP_NAME);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound questNBT = new NBTTagCompound();
        questData.writeToNBT(questNBT, EnumSet.allOf(PlayerQuestData.DataType.class));
        compound.setTag("QuestData", questNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound questNBT = compound.getCompoundTag("QuestData");
        questData.readFromNBT(questNBT, EnumSet.allOf(PlayerQuestData.DataType.class));
    }

    public void sync(EnumSet<PlayerQuestData.DataType> dataTypes) {
        if (player != null && !player.worldObj.isRemote && player instanceof EntityPlayerMP) {
            MatterOverdrive.packetPipeline.sendTo(new PacketSyncQuests(questData, dataTypes), (EntityPlayerMP) player);
        }
    }

    public void copy(MOExtendedProperties oterExtendetProperies) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        oterExtendetProperies.saveNBTData(tagCompound);
        loadNBTData(tagCompound);
    }

    public void addQuest(QuestStack questStack) {
        if (!MinecraftForge.EVENT_BUS.post(new MOEventQuest.Added(questStack, player))) {
            if (isServer()) {
                if (questData.getActiveQuests().size() <= 0 && questData.getCompletedQuests().size() <= 0) {
                    player.inventory.addItemStackToInventory(new ItemStack(MatterOverdriveItems.dataPad));
                }
                QuestStack addedQuest = questData.addQuest(questStack);
                if (addedQuest != null) {
                    addedQuest.getQuest().initQuestStack(player.getRNG(), addedQuest, player);
                    MatterOverdrive.packetPipeline.sendTo(new PacketUpdateQuest(addedQuest, PacketUpdateQuest.ADD_QUEST), (EntityPlayerMP) player);
                }
            } else {
                QuestStack addedQuest = questData.addQuest(questStack);
                ClientProxy.questHud.addStartedQuest(addedQuest);
            }
        }
    }

    public void update(Side side) {
        if (side.equals(Side.SERVER)) {
            questData.manageQuestCompletion();
        }
    }

    public boolean hasCompletedQuest(QuestStack questStack) {
        return questData.hasCompletedQuest(questStack);
    }

    public boolean hasQuest(QuestStack questStack) {
        return questData.hasQuest(questStack);
    }

    public void onQuestCompleted(QuestStack questStack, int index) {
        if (isServer()) {

            List<IQuestReward> rewards = new ArrayList<>();
            questStack.addRewards(rewards, getPlayer());
            int xp = questStack.getXP(getPlayer());
            MOEventQuest.Completed event = new MOEventQuest.Completed(questStack, player, xp, rewards);

            if (!MinecraftForge.EVENT_BUS.post(event)) {
                questData.addQuestToCompleted(questStack);
                getPlayer().addExperience(event.xp);
                InventoryPlayer inventoryPlayer = getPlayer().inventory;
                for (IQuestReward reward : event.rewards) {
                    reward.giveReward(questStack, getPlayer());
                }
                questStack.getQuest().onCompleted(questStack, player);
                player.addChatMessage(new ChatComponentText(String.format("[Matter Megadrive] %1$s completed %2$s", player.getDisplayName(), questStack.getTitle(player))));
            }
            MatterOverdrive.packetPipeline.sendTo(new PacketUpdateQuest(index, questStack, PacketUpdateQuest.COMPLETE_QUEST), (EntityPlayerMP) player);
        } else {
            ClientProxy.questHud.addCompletedQuest(questStack);
            getQuestData().getCompletedQuests().add(questStack);
            getQuestData().removeQuest(index);
            if (Minecraft.getMinecraft().currentScreen instanceof GuiDataPad) {
                ((GuiDataPad) Minecraft.getMinecraft().currentScreen).refreshQuests(this);
            }
        }
    }

    public void onQuestAbandoned(QuestStack questStack) {
        if (!isServer()) {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiDataPad) {
                ((GuiDataPad) Minecraft.getMinecraft().currentScreen).refreshQuests(this);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateQuestFromServer(int index, QuestStack questStack) {
        if (index < getQuestData().getActiveQuests().size()) {
            ClientProxy.questHud.addObjectivesChanged(getQuestData().getActiveQuests().get(index), questStack);
            getQuestData().getActiveQuests().set(index, questStack);
        }
    }

    public boolean isServer() {
        return player != null && !player.worldObj.isRemote;
    }

    public PlayerQuestData getQuestData() {
        return questData;
    }

    public void onEvent(Event event) {
        questData.onEvent(event);
    }

    @Override
    public void init(Entity entity, World world) {

    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
