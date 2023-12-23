package de.zillolp.groupsystem.commands.subcommands.groups;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.config.customconfigs.GroupConfig;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CreateSubCommand extends SubCommand {
    public CreateSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        if (args.length < 4) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        String groupName = args[1];
        GroupConfig groupConfig = plugin.getGroupConfig();
        if (groupConfig.hasGroup(groupName)) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.GROUP_ALREADY_EXISTS));
            return true;
        }
        String inputString = args[2];
        if (!(languageConfig.isNumeric(inputString))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.WRONG_INPUT));
            return true;
        }
        int number = Integer.parseInt(inputString);
        StringBuilder prefix = new StringBuilder(args[3]);
        if (args.length > 4) {
            for (String argument : Arrays.copyOfRange(args, 4, args.length)) {
                prefix.append(" ").append(argument);
            }
        }
        groupConfig.createGroup(groupName, prefix.toString(), number);
        plugin.updateSubCommands(plugin.getAddSubCommand(), plugin.getDeleteSubCommand(), plugin.getSetSubCommand());
        commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.CREATE_GROUP).replace("%group%", groupName));
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return permissionConfig.hasPermission(commandSender, Permission.ADMIN_PERMISSION);
    }
}
