package de.zillolp.groupsystem.manager;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private final GroupSystem plugin;
    private final ConcurrentHashMap<UUID, PlayerProfile> playerProfiles;

    public PlayerManager(GroupSystem plugin) {
        this.plugin = plugin;
        this.playerProfiles = new ConcurrentHashMap<>();
    }

    public void registerPlayer(Player player, boolean isAsync) {
        if (!(isAsync)) {
            registerPlayer(player);
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                registerPlayer(player);
            }
        });
    }

    private void registerPlayer(Player player) {
        // When registering a player, the player profile must be created, and the individual packets for the information signs and tab list must be sent.
        playerProfiles.put(player.getUniqueId(), new PlayerProfile(plugin, player));
        plugin.getInfoSignManager().sendInfoSigns(player, true);
        plugin.getTablistManager().updateTeams();
    }

    public void unregisterPlayer(Player player, boolean isAsync) {
        if (!(isAsync)) {
            unregisterPlayer(player);
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                unregisterPlayer(player);
            }
        });
    }

    private void unregisterPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        // When unregistering a player, they must be removed from all lists and maps, except for those related to player profiles, as access to their profile should be possible even when they are offline.
        plugin.getSetupManager().getTimeProfiles().remove(uuid);
        plugin.getInfoSignManager().getPlayerInfoSigns().remove(uuid);
        plugin.getTablistManager().getPlayerTeams().remove(uuid);
        if (!(playerProfiles.containsKey(uuid))) {
            return;
        }
        playerProfiles.get(uuid).uploadData();
    }

    public ConcurrentHashMap<UUID, PlayerProfile> getPlayerProfiles() {
        return playerProfiles;
    }
}
