package de.zillolp.groupsystem.commands.subcommands.groups;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.database.DatabaseManager;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InfoSubCommand extends SubCommand {
    public InfoSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        if (args.length != 1 && args.length != 2) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        if (args.length == 1) {
            if (!(permissionConfig.hasPermission(commandSender, Permission.INFO_PERMISSION))) {
                commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.NO_PERMISSION));
                return true;
            }
            UUID uuid = ((Player) commandSender).getUniqueId();
            if (!(playerProfiles.containsKey(uuid))) {
                commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_PLAYER));
                return true;
            }
            sendInfoMessage(commandSender, playerProfiles.get(uuid));
            return true;
        }
        if (!(permissionConfig.hasPermission(commandSender, Permission.INFO_OTHER_PERMISSION))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.NO_PERMISSION));
            return true;
        }
        String playerName = args[1];
        DatabaseManager databaseManager = plugin.getDatabaseManager();
        if (!(databaseManager.playerExists(playerName))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_PLAYER));
            return true;
        }
        UUID uuid = databaseManager.getUUID(playerName);
        if (uuid == null || (!(playerProfiles.containsKey(uuid)))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_PLAYER));
            return true;
        }
        sendInfoMessage(commandSender, playerProfiles.get(uuid));
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return true;
    }

    private void sendInfoMessage(CommandSender commandSender, PlayerProfile playerProfile) {
        commandSender.sendMessage(languageConfig.getTranslatedLanguage(Language.GROUP_INFO));
        UUID uuid = playerProfile.getUuid();
        if ((!(playerProfile.hasGroup())) || (!(plugin.getGroupConfig().hasGroup(playerProfile.getGroup())))) {
            commandSender.sendMessage(languageConfig.getTranslatedLanguage(Language.GROUP_INFO_NO_GROUP));
            return;
        }
        commandSender.sendMessage(languageConfig.getReplacedLanguage(Language.GROUP_INFO_GROUP, uuid, false));
        if (playerProfile.getExpirationDate() == 0) {
            commandSender.sendMessage(languageConfig.getTranslatedLanguage(Language.GROUP_INFO_PERMANENT));
            return;
        }
        commandSender.sendMessage(languageConfig.getReplacedLanguage(Language.GROUP_INFO_TIME, uuid, false));
    }
}
