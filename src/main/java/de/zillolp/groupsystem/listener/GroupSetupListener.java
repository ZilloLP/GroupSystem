package de.zillolp.groupsystem.listener;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.LanguageConfig;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.TimeType;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import de.zillolp.groupsystem.profiles.TimeProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GroupSetupListener implements Listener {
    private final GroupSystem plugin;
    private final LanguageConfig languageConfig;

    public GroupSetupListener(GroupSystem plugin) {
        this.plugin = plugin;
        languageConfig = plugin.getLanguageConfig();
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        HashMap<UUID, TimeProfile> timeProfiles = plugin.getSetupManager().getTimeProfiles();
        if (!(timeProfiles.containsKey(uuid))) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        if (!(languageConfig.isNumeric(message))) {
            player.sendMessage(languageConfig.getLanguageWithPrefix(Language.WRONG_INPUT));
            return;
        }
        int number = Integer.parseInt(message);
        TimeProfile timeProfile = timeProfiles.get(uuid);
        // The individual input times and the current system time are added together to determine the expiration date.
        switch (timeProfile.getTimeType()) {
            case DAYS:
                addTime(player, timeProfile, TimeType.HOURS, 86400000L * number, languageConfig.getLanguageWithPrefix(Language.GIVE_HOURS));
                break;
            case HOURS:
                addTime(player, timeProfile, TimeType.MINUTES, 3600000L * number, languageConfig.getLanguageWithPrefix(Language.GIVE_MINUTES));
                break;
            case MINUTES:
                addTime(player, timeProfile, TimeType.SECONDS, 60000L * number, languageConfig.getLanguageWithPrefix(Language.GIVE_SECONDS));
                break;
            case SECONDS:
                timeProfile.addTime(1000L * (number + 1));
                long time = timeProfile.getTime();
                UUID timeUUID = timeProfile.getUuid();
                ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
                if (!(playerProfiles.containsKey(timeUUID))) {
                    player.sendMessage(languageConfig.getLanguageWithPrefix(Language.UNKNOWN_PLAYER));
                    break;
                }
                PlayerProfile playerProfile = playerProfiles.get(timeUUID);
                playerProfile.setGroup(timeProfile.getGroupName());
                playerProfile.setExpirationDate(System.currentTimeMillis() + time);
                plugin.getDatabaseManager().uploadProfile(playerProfile);
                for (String language : languageConfig.getReplacedLanguages(Language.ADD_GROUP_TEMPORARILY, timeUUID, true)) {
                    player.sendMessage(language);
                }
                timeProfiles.remove(uuid);
                break;
        }
    }

    private void addTime(Player player, TimeProfile timeProfile, TimeType timeType, long time, String message) {
        timeProfile.setTimeType(timeType);
        timeProfile.addTime(time);
        player.sendMessage(message);
    }
}
