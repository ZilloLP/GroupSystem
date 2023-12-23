package de.zillolp.groupsystem.commands.maincommands;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.config.customconfigs.LanguageConfig;
import de.zillolp.groupsystem.enums.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupsCommand extends MainCommand {

    public GroupsCommand(GroupSystem plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        LanguageConfig languageConfig = plugin.getLanguageConfig();
        if (!(plugin.getDatabaseConnector().hasConnection())) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.NO_DATABASE_CONNECTION));
            return true;
        }
        if (!(commandSender instanceof Player)) {
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        if (!(subCommands.containsKey(args[0].toLowerCase()))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        SubCommand subCommand = subCommands.get(args[0]).get(0);
        if (!(subCommand.hasPermission(commandSender))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.NO_PERMISSION));
            return true;
        }
        return subCommand.onCommand(plugin, commandSender, command, args);
    }
}
