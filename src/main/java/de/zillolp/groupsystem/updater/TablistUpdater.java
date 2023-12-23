package de.zillolp.groupsystem.updater;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.manager.TablistManager;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TablistUpdater extends CustomUpdater {
    private final TablistManager tablistManager;

    public TablistUpdater(GroupSystem plugin, TablistManager tablistManager, int ticks) {
        super(plugin, ticks);
        this.tablistManager = tablistManager;
    }

    @Override
    protected void tick() {
        if (tablistManager == null) {
            plugin.getLogger().warning("TablistManager is null in TablistUpdater.tick()");
            return;
        }
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!(playerProfiles.containsKey(uuid))) {
                continue;
            }
            PlayerProfile playerProfile = playerProfiles.get(uuid);
            // If a player was previously assigned to a group and is now no longer, the rank must be removed from the tab list.
            if (tablistManager.getPlayerTeams().containsKey(uuid) && (!(playerProfile.hasGroup()))) {
                tablistManager.deleteTeam(player);
                continue;
            }
            // If the player has the correct rank in the tab list or is not in any group, there is no need to update.
            if (tablistManager.hasTeam(uuid) || (!(playerProfile.hasGroup()))) {
                continue;
            }
            tablistManager.registerTeam(player);
            tablistManager.setTeam(player);
        }
    }
}
