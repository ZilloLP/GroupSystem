package de.zillolp.groupsystem.listener;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.manager.PlayerManager;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerConnectionListener implements Listener {
    private final GroupSystem plugin;
    private final PlayerManager playerManager;

    public PlayerConnectionListener(GroupSystem plugin) {
        this.plugin = plugin;
        playerManager = plugin.getPlayerManager();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // The player will be synchronized when created if they have not been registered yet, so that the stats from the player profile can already be used during the JoinEvent.
        playerManager.registerPlayer(player, plugin.getDatabaseManager().playerExists(player.getName()));
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = playerManager.getPlayerProfiles();
        if (!(playerProfiles.containsKey(uuid))) {
            return;
        }
        PlayerProfile playerProfile = playerProfiles.get(uuid);
        String groupName = playerProfile.getGroup();
        // When the player has not been assigned to any group, no join message with a prefix is sent, as there is no prefix available.
        if ((!(plugin.getGroupConfig().hasGroup(groupName))) || (!(playerProfile.hasGroup()))) {
            return;
        }
        event.setJoinMessage(plugin.getLanguageConfig().getReplacedLanguage(Language.JOIN_MESSAGE, uuid, false));
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerManager.unregisterPlayer(player, true);
    }
}