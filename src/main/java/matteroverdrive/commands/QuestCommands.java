package matteroverdrive.commands;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.quest.IQuest;
import matteroverdrive.api.quest.QuestStack;
import matteroverdrive.data.quest.PlayerQuestData;
import matteroverdrive.entity.player.MOExtendedProperties;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class QuestCommands extends CommandBase {
    private Random random = new Random();

    @Override
    public String getCommandName() {
        return "quest";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "quest <action> [parameters] <name> <player>";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] parameters) {
        if (parameters.length > 0) {
            if (parameters[0].equalsIgnoreCase("add")) {
                if (parameters.length > 1) {
                    EntityPlayer entityPlayer;
                    if (parameters.length > 2) {
                        entityPlayer = getPlayer(commandSender, parameters[2]);
                    } else {
                        entityPlayer = commandSender.getEntityWorld().getPlayerEntityByName(commandSender.getCommandSenderName());
                    }

                    if (entityPlayer != null) {
                        MOExtendedProperties extendedProperties = MOExtendedProperties.get(entityPlayer);
                        QuestStack questStack = MatterOverdrive.questFactory.generateQuestStack(parameters[1]);
                        if (questStack != null) {
                            extendedProperties.addQuest(questStack);
                        } else {
                            throw new CommandException("Could not find a quest with the given name.");
                        }
                    } else {
                        throw new CommandException("Invalid Player.");
                    }
                } else {
                    throw new CommandException("No random quest parameters.");
                }
            } else if (parameters[0].equalsIgnoreCase("remove")) {
                if (parameters.length > 1) {
                    EntityPlayer entityPlayer;
                    if (parameters.length > 2) {
                        entityPlayer = getPlayer(commandSender, parameters[2]);
                    } else {
                        entityPlayer = commandSender.getEntityWorld().getPlayerEntityByName(commandSender.getCommandSenderName());
                    }

                    if (entityPlayer != null) {
                        MOExtendedProperties extendedProperties = MOExtendedProperties.get(entityPlayer);
                        if (parameters[1].equalsIgnoreCase("all")) {
                            extendedProperties.getQuestData().clearActiveQuests();
                            extendedProperties.getQuestData().clearCompletedQuests();
                            extendedProperties.sync(EnumSet.allOf(PlayerQuestData.DataType.class));
                        } else if (parameters[1].equalsIgnoreCase("active")) {
                            extendedProperties.getQuestData().clearActiveQuests();
                            extendedProperties.sync(EnumSet.of(PlayerQuestData.DataType.ACTIVE_QUESTS));
                        } else if (parameters[1].equalsIgnoreCase("completed")) {
                            extendedProperties.getQuestData().clearCompletedQuests();
                            extendedProperties.sync(EnumSet.of(PlayerQuestData.DataType.COMPLETED_QUESTS));
                        } else {
                            throw new CommandException("Invalid quest type.");
                        }
                    } else {
                        throw new CommandException("Invalid Player.");
                    }
                } else {
                    throw new CommandException("No remove quests parameters.");
                }
            } else if (parameters[0].equalsIgnoreCase("contract")) {
                if (parameters.length > 1) {
                    IQuest quest = MatterOverdrive.quests.getQuestByName(parameters[1]);
                    if (quest != null) {
                        EntityPlayer entityPlayer;
                        if (parameters.length > 2) {
                            entityPlayer = getPlayer(commandSender, parameters[2]);
                        } else {
                            entityPlayer = commandSender.getEntityWorld().getPlayerEntityByName(commandSender.getCommandSenderName());
                        }

                        QuestStack questStack = MatterOverdrive.questFactory.generateQuestStack(random, quest);
                        entityPlayer.inventory.addItemStackToInventory(questStack.getContract());
                    } else {
                        throw new CommandException("No such quest.");
                    }
                }
            } else {
                throw new CommandException("Invalid quest command.");
            }
        } else {
            throw new CommandException("Invalid command.");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] parameters) {
        List<String> commands = new ArrayList<>();

        if (parameters.length == 2) {
            if (parameters[0].equalsIgnoreCase("add") || parameters[0].equalsIgnoreCase("contract")) {
                for (String questName : MatterOverdrive.quests.getAllQuestName()) {
                    commands.add(questName);
                }
            } else if (parameters[0].equalsIgnoreCase("remove")) {
                commands.add("all");
                commands.add("active");
                commands.add("completed");
            }
        } else if (parameters.length == 1) {
            commands.add("add");
            commands.add("remove");
            commands.add("contract");
        } else if (parameters.length == 3) {
            for (Object player : commandSender.getEntityWorld().playerEntities) {
                commands.add(((EntityPlayer) player).getCommandSenderName());
            }
        }
        return commands;
    }
}
