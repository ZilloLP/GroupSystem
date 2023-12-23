package de.zillolp.groupsystem.commands.subcommands.groupsystem;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        if (args.length != 1) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        plugin.getConfigManager().reloadConfigs();
        commandSender.sendMessage(plugin.getLanguageConfig().getLanguageWithPrefix(Language.RELOAD_SETTINGS));
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return permissionConfig.hasPermission(commandSender, Permission.ADMIN_PERMISSION);
    }
}
