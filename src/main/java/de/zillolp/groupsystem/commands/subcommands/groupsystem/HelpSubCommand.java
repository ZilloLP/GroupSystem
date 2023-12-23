package de.zillolp.groupsystem.commands.subcommands.groupsystem;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpSubCommand extends SubCommand {
    public HelpSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        for (String message : languageConfig.getTranslatedLanguages(Language.HELP_INFO, false)) {
            commandSender.sendMessage(message);
        }
        if (!(plugin.getPermissionConfig().hasPermission(commandSender, Permission.ADMIN_PERMISSION))) {
            return true;
        }
        for (String message : languageConfig.getTranslatedLanguages(Language.ADMIN_HELP_INFO, false)) {
            commandSender.sendMessage(message);
        }
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return true;
    }
}
