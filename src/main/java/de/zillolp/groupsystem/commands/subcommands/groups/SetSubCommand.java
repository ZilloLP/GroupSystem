package de.zillolp.groupsystem.commands.subcommands.groups;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.config.customconfigs.GroupConfig;
import de.zillolp.groupsystem.config.customconfigs.PluginConfig;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class SetSubCommand extends SubCommand {
    private final PluginConfig pluginConfig;

    public SetSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
        pluginConfig = plugin.getPluginConfig();
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        if (args.length < 4) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        String groupName = args[2];
        GroupConfig groupConfig = plugin.getGroupConfig();
        if (!(groupConfig.hasGroup(groupName))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.GROUP_DOESNT_EXISTS));
            return true;
        }
        String type = args[1];
        StringBuilder input = new StringBuilder(args[3]);
        if (args.length > 4) {
            if (!(type.equalsIgnoreCase("prefix"))) {
                commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
                return true;
            }
            for (String argument : Arrays.copyOfRange(args, 4, args.length)) {
                input.append(" ").append(argument);
            }
        }
        String inputString = input.toString();
        if (type.equalsIgnoreCase("name")) {
            groupConfig.setGroupName(groupName, inputString);
            if (pluginConfig.getFileConfiguration().getString("default-group", "default").equalsIgnoreCase(groupName)) {
                pluginConfig.setDefaultGroup(inputString);
            }
            for (PlayerProfile playerProfile : plugin.getPlayerManager().getPlayerProfiles().values()) {
                if (!(playerProfile.getGroup().equalsIgnoreCase(groupName))) {
                    continue;
                }
                playerProfile.setGroup(inputString);
                plugin.getDatabaseManager().uploadProfile(playerProfile);
            }
            plugin.updateSubCommands(plugin.getAddSubCommand(), plugin.getDeleteSubCommand(), plugin.getSetSubCommand());
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.SET_NAME).replace("%name%", inputString));
            return true;
        }
        if (type.equalsIgnoreCase("priority")) {
            if (!(languageConfig.isNumeric(inputString))) {
                commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.WRONG_INPUT));
                return true;
            }
            groupConfig.setSortNumber(groupName, Integer.parseInt(inputString));
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.SET_PRIORITY).replace("%priority%", inputString));
            return true;
        }
        groupConfig.setPrefix(groupName, inputString);
        String language = languageConfig.getLanguageWithPrefix(Language.SET_PREFIX);
        language = language.replace("%group%", groupConfig.getGroupName(groupName));
        language = language.replace("%prefix%", ChatColor.translateAlternateColorCodes('&', inputString));
        commandSender.sendMessage(language);
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return permissionConfig.hasPermission(commandSender, Permission.ADMIN_PERMISSION);
    }
}
