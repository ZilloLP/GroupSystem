package de.zillolp.groupsystem.commands.subcommands.groups;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.config.customconfigs.GroupConfig;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DeleteSubCommand extends SubCommand {
    public DeleteSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        String groupName = args[1];
        GroupConfig groupConfig = plugin.getGroupConfig();
        if (!(groupConfig.hasGroup(groupName))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.GROUP_DOESNT_EXISTS));
            return true;
        }
        String defaultGroup = plugin.getPluginConfig().getFileConfiguration().getString("default-group", "default");
        for (PlayerProfile playerProfile : plugin.getPlayerManager().getPlayerProfiles().values()) {
            if (!(playerProfile.getGroup().equalsIgnoreCase(groupName))) {
                continue;
            }
            playerProfile.setGroup(defaultGroup);
            plugin.getDatabaseManager().uploadProfile(playerProfile);
        }
        commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.DELETE_GROUP).replace("%group%", groupConfig.getGroupName(groupName)));
        groupConfig.deleteGroup(groupName);
        plugin.updateSubCommands(plugin.getAddSubCommand(), plugin.getDeleteSubCommand(), plugin.getSetSubCommand());
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return permissionConfig.hasPermission(commandSender, Permission.ADMIN_PERMISSION);
    }
}
