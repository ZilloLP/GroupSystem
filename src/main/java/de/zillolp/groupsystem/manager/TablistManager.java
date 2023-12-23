package de.zillolp.groupsystem.manager;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.GroupConfig;
import de.zillolp.groupsystem.config.customconfigs.LanguageConfig;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import de.zillolp.groupsystem.updater.TablistUpdater;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TablistManager {
    private final GroupSystem plugin;
    private final LanguageConfig languageConfig;
    private final GroupConfig groupConfig;
    private final Scoreboard scoreboard;
    private final HashMap<UUID, PlayerTeam> playerTeams;
    private final TablistUpdater tablistUpdater;

    public TablistManager(GroupSystem plugin) {
        this.plugin = plugin;
        languageConfig = plugin.getLanguageConfig();
        groupConfig = plugin.getGroupConfig();
        scoreboard = new Scoreboard();
        playerTeams = new HashMap<>();
        // Creating updater for continuously updating the ranks in the tablist.
        tablistUpdater = new TablistUpdater(plugin, this, plugin.getPluginConfig().getFileConfiguration().getInt("tablist-ticks", 20));
    }

    public boolean hasTeam(UUID uuid) {
        if (!(playerTeams.containsKey(uuid))) {
            return false;
        }
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        if (!(playerProfiles.containsKey(uuid))) {
            return true;
        }
        PlayerProfile playerProfile = playerProfiles.get(uuid);
        String groupName = playerProfile.getGroup();
        String prefix = languageConfig.getReplacedColorCodes(groupConfig.getPrefix(groupName)) + " ";
        String priority = groupConfig.getPriority(groupName) + uuid.toString().substring(6);
        PlayerTeam playerTeam = playerTeams.get(uuid);
        // When the prefix and the priority in the tablist are the same, the player is on the correct team.
        return playerTeam.getPlayerPrefix().getString().equals(prefix) && playerTeam.getName().equals(priority);
    }

    public void registerTeam(Player player) {
        UUID uuid = player.getUniqueId();
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        if (!(playerProfiles.containsKey(uuid))) {
            return;
        }
        deleteTeam(player);
        PlayerProfile playerProfile = playerProfiles.get(uuid);
        String groupName = playerProfile.getGroup();
        String prefix = languageConfig.getReplacedColorCodes(groupConfig.getPrefix(groupName)) + " ";
        String priority = groupConfig.getPriority(groupName) + uuid.toString().substring(6);

        // The name of the team is the priority to correctly sort the tablist.
        PlayerTeam playerTeam = new PlayerTeam(scoreboard, priority);
        playerTeam.setPlayerPrefix(Component.literal(prefix));

        playerTeams.put(uuid, playerTeam);
        sendPacket(ClientboundSetPlayerTeamPacket.createPlayerPacket(playerTeam, priority, ClientboundSetPlayerTeamPacket.Action.ADD));
    }

    public void setTeam(Player player) {
        UUID uuid = player.getUniqueId();
        if (!(playerTeams.containsKey(uuid))) {
            return;
        }
        PlayerTeam playerTeam = playerTeams.get(uuid);
        Collection<String> players = playerTeam.getPlayers();
        // If the player was already in the team, we remove him again to avoid duplicates.
        players.remove(player.getDisplayName());
        players.add(player.getDisplayName());
        sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true));
    }

    public void deleteTeam(Player player) {
        UUID uuid = player.getUniqueId();
        if (!(playerTeams.containsKey(uuid))) {
            return;
        }
        sendPacket(ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeams.get(uuid)));
        playerTeams.remove(uuid);
    }

    // This method is intended to update the teams for all players, for example, when joining the server.
    public void updateTeams() {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            registerTeam(player1);
            setTeam(player1);
        }
    }

    private void sendPacket(Packet<?> packet) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().connection.send(packet);
        }
    }

    public HashMap<UUID, PlayerTeam> getPlayerTeams() {
        return playerTeams;
    }

    public TablistUpdater getTablistUpdater() {
        return tablistUpdater;
    }
}
