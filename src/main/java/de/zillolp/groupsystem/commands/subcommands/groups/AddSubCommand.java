package de.zillolp.groupsystem.commands.subcommands.groups;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.database.DatabaseManager;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import de.zillolp.groupsystem.profiles.TimeProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AddSubCommand extends SubCommand {
    public AddSubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        super(plugin, mainCommand, subCommands);
    }

    public boolean onCommand(GroupSystem plugin, CommandSender commandSender, Command command, String[] args) {
        if (args.length != 4) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        String type = args[1];
        if ((!(type.equalsIgnoreCase("permanent"))) && (!(type.equalsIgnoreCase("temporarily")))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_COMMAND));
            return true;
        }
        String playerName = args[2];
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
        String groupName = args[3];
        if (!(plugin.getGroupConfig().hasGroup(groupName))) {
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.GROUP_DOESNT_EXISTS));
            return true;
        }
        PlayerProfile playerProfile = plugin.getPlayerManager().getPlayerProfiles().get(uuid);
        if (type.equalsIgnoreCase("temporarily")) {
            plugin.getSetupManager().getTimeProfiles().put(((Player) commandSender).getUniqueId(), new TimeProfile(uuid, groupName));
            commandSender.sendMessage(languageConfig.getLanguageWithPrefix(Language.GIVE_DAYS));
            return true;
        }
        playerProfile.setGroup(groupName);
        playerProfile.setExpirationDate(0);
        plugin.getDatabaseManager().uploadProfile(playerProfile);
        commandSender.sendMessage(languageConfig.getReplacedLanguage(Language.ADD_GROUP_PERMANENT, uuid, true));
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender commandSender) {
        return permissionConfig.hasPermission(commandSender, Permission.ADMIN_PERMISSION);
    }
}
