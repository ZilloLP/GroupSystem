package de.zillolp.groupsystem.infosigns;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.LocationConfig;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import de.zillolp.groupsystem.updater.InfoSignUpdater;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InfoSignManager {
    private final GroupSystem plugin;
    private final LocationConfig locationConfig;
    private final ConcurrentHashMap<UUID, ArrayList<InfoSign>> playerInfoSigns;
    private final String sectionName;
    private final InfoSignUpdater infoSignUpdater;

    public InfoSignManager(GroupSystem plugin) {
        this.plugin = plugin;
        locationConfig = plugin.getLocationConfig();
        playerInfoSigns = new ConcurrentHashMap<>();
        sectionName = "info-signs";
        // Creating updater for continuously updating the info signs.
        infoSignUpdater = new InfoSignUpdater(plugin, this, plugin.getPluginConfig().getFileConfiguration().getInt("sign-ticks", 20));
    }

    public void sendInfoSigns(Player player, boolean forceUpdate) {
        UUID uuid = player.getUniqueId();
        String[] lines = getLines(uuid);
        if (!(playerInfoSigns.containsKey(uuid))) {
            ArrayList<InfoSign> infoSigns = new ArrayList<>();
            for (Location location : locationConfig.getLocations(sectionName)) {
                infoSigns.add(new InfoSign(player, location, lines));
            }
            playerInfoSigns.put(uuid, infoSigns);
            return;
        }
        for (InfoSign infoSign : playerInfoSigns.get(uuid)) {
            infoSign.update(lines, forceUpdate);
        }
    }

    public void saveSignLocation(Location location) {
        locationConfig.saveInfoSignLocation(location);
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!(playerInfoSigns.containsKey(uuid))) {
                continue;
            }
            playerInfoSigns.get(uuid).add(new InfoSign(player, location, getLines(uuid)));
        }
    }

    public void removeSignLocation(Location signLocation) {
        locationConfig.removeLocation(sectionName, signLocation);
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!(playerInfoSigns.containsKey(uuid))) {
                continue;
            }
            ArrayList<InfoSign> infoSigns = playerInfoSigns.get(uuid);
            for (InfoSign infoSign : infoSigns) {
                Location location = infoSign.getLocation();
                // Only the X, Y, and Z coordinates are checked, as the sign is a block, and therefore Yaw and Pitch always remain 0.
                if (location.getBlockX() != signLocation.getBlockX() || location.getBlockY() != signLocation.getBlockY() || location.getBlockZ() != signLocation.getBlockZ()) {
                    continue;
                }
                infoSigns.remove(infoSign);
                break;
            }
        }
    }

    public boolean hasSignLocation(Location signLocation) {
        for (Location location : locationConfig.getLocations(sectionName)) {
            // Only the X, Y, and Z coordinates are checked, as the sign is a block, and therefore Yaw and Pitch always remain 0.
            if (location.getBlockX() == signLocation.getBlockX() && location.getBlockY() == signLocation.getBlockY() && location.getBlockZ() == signLocation.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    private String[] getLines(UUID uuid) {
        String[] lines = new String[4];
        ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
        if (!(playerProfiles.containsKey(uuid))) {
            return lines;
        }
        PlayerProfile playerProfile = playerProfiles.get(uuid);
        String[] signLines = plugin.getLanguageConfig().getReplacedLanguages(Language.INFO_SIGN_LINES, uuid, false);
        lines[0] = signLines[0];
        /* Perhaps a bit too complicated, but it serves its purpose.
        In summary, the individual lines from the config are assembled onto the sign,
        and since there are specific cases where the line must be a certain one, it is built together like a construction kit.
        The individual lines are thus also defined, which is okay for now, but should be revised in the future for more customization. */
        if ((!(playerProfile.hasGroup())) || (!(plugin.getGroupConfig().hasGroup(playerProfile.getGroup())))) {
            lines[1] = signLines[2];
            lines[2] = signLines[4];
            lines[3] = signLines[7];
            return lines;
        }
        lines[1] = signLines[1];
        lines[2] = signLines[3];
        if (playerProfile.getExpirationDate() == 0) {
            lines[3] = signLines[5];
            return lines;
        }
        lines[3] = signLines[6];
        return lines;
    }

    public ConcurrentHashMap<UUID, ArrayList<InfoSign>> getPlayerInfoSigns() {
        return playerInfoSigns;
    }

    public InfoSignUpdater getInfoSignUpdater() {
        return infoSignUpdater;
    }
}
