package de.zillolp.groupsystem.listener;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerChatListener implements Listener {
    private final GroupSystem plugin;

    public PlayerChatListener(GroupSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        if (!(playerProfiles.containsKey(uuid))) {
            return;
        }
        PlayerProfile playerProfile = playerProfiles.get(uuid);
        String groupName = playerProfile.getGroup();
        // If the player is not in any group or the group does not exist, there is also no prefix, so the format does not need to be changed.
        if ((!(playerProfile.hasGroup())) || (!(plugin.getGroupConfig().hasGroup(groupName)))) {
            return;
        }
        event.setFormat(plugin.getLanguageConfig().getReplacedLanguage(Language.CHAT_FORMAT, uuid, false) + " %2$s");
    }
}
