package de.zillolp.groupsystem.commands.subcommands.groups;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.database.DatabaseManager;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RemoveSubCommand extends SubCommand {
    public RemoveSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        String playerName = args[1];
        DatabaseManager databaseManager = plugin.getDatabaseManager();
        if (!(databaseManager.playerExists(playerName))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_PLAYER));
            return true;
        }
        UUID uuid = databaseManager.getUUID(playerName);
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        if (uuid == null || (!(playerProfiles.containsKey(uuid)))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_PLAYER));
            return true;
        }
        PlayerProfile playerProfile = playerProfiles.get(uuid);
        commandSender.sendMessage(languageConfig.getReplacedLanguage(Language.REMOVE_PLAYER_GROUP, uuid, true));
        playerProfile.setGroup("");
        playerProfile.setExpirationDate(0);
        databaseManager.uploadProfile(playerProfile);
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return permissionConfig.hasPermission(commandSender, Permission.ADMIN_PERMISSION);
    }
}
