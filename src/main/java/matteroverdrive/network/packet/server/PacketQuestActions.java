package matteroverdrive.network.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import matteroverdrive.api.quest.QuestStack;
import matteroverdrive.data.quest.PlayerQuestData;
import matteroverdrive.entity.player.MOExtendedProperties;
import matteroverdrive.items.Contract;
import matteroverdrive.network.packet.PacketAbstract;
import matteroverdrive.network.packet.client.quest.PacketSyncQuests;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;

public class PacketQuestActions extends PacketAbstract {
    public static final int QUEST_ACTION_ABONDON = 0;
    public static final int QUEST_ACTION_COMPLETE = 1;
    public static final int QUEST_ACTION_ADD = 2;
    int command;
    int questID;
    int playerID;

    public PacketQuestActions() {
    }

    public PacketQuestActions(int command, int questID, int playerID) {
        this.command = command;
        this.questID = questID;
        this.playerID = playerID;
    }

    public PacketQuestActions(int command, int questID, EntityPlayer entityPlayer) {
        this.command = command;
        this.questID = questID;
        this.playerID = entityPlayer.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.command = buf.readInt();
        this.questID = buf.readInt();
        this.playerID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(command);
        buf.writeInt(questID);
        buf.writeInt(playerID);
    }

    public static class ServerHandler extends AbstractServerPacketHandler<PacketQuestActions> {
        public ServerHandler() {
        }

        @Override
        public IMessage handleServerMessage(EntityPlayer player, PacketQuestActions message, MessageContext ctx) {
            Entity entity = player.worldObj.getEntityByID(message.playerID);
            if (entity instanceof EntityPlayer) {
                MOExtendedProperties extendedProperties = MOExtendedProperties.get((EntityPlayer) entity);
                if (extendedProperties != null) {
                    if (message.questID < extendedProperties.getQuestData().getActiveQuests().size()) {
                        if (message.command == QUEST_ACTION_COMPLETE) {
                            QuestStack questStack = extendedProperties.getQuestData().getActiveQuests().get(message.questID);
                            if (QuestStack.canComplete((EntityPlayer) entity, questStack)) {
                                questStack.markComplited(player, true);
                            }
                        } else if (message.command == QUEST_ACTION_ABONDON) {
                            extendedProperties.getQuestData().removeQuest(message.questID);
                            return new PacketSyncQuests(extendedProperties.getQuestData(), EnumSet.of(PlayerQuestData.DataType.ACTIVE_QUESTS));
                        }
                    }
                    if (message.command == QUEST_ACTION_ADD) {
                        ItemStack contract = extendedProperties.getPlayer().inventory.getStackInSlot(message.questID);
                        if (contract.getItem() instanceof Contract) {
                            extendedProperties.addQuest(((Contract) contract.getItem()).getQuest(contract));
                            extendedProperties.getPlayer().inventory.decrStackSize(message.questID, 1);
                        }

                    }
                }
            }
            return null;
        }
    }
}
