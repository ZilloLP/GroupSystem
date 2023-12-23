package de.zillolp.groupsystem.profiles;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.PluginConfig;
import de.zillolp.groupsystem.database.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerProfile {
    private final PluginConfig pluginConfig;
    private final DatabaseManager databaseManager;
    private final UUID uuid;
    private String name;
    private String groupName;
    private long expirationDate;

    /* This constructor is only used by the database to create the profile for an offline player.
    As soon as the player is online, the other constructor is used because it sets data that is not needed when the player is offline. */

    public PlayerProfile(GroupSystem plugin, UUID uuid) {
        pluginConfig = plugin.getPluginConfig();
        databaseManager = plugin.getDatabaseManager();
        this.uuid = uuid;
        databaseManager.loadProfile(this);
    }

    public PlayerProfile(GroupSystem plugin, Player player) {
        pluginConfig = plugin.getPluginConfig();
        databaseManager = plugin.getDatabaseManager();
        uuid = player.getUniqueId();
        name = player.getName();
        if (!(databaseManager.playerExists(uuid, name))) {
            databaseManager.createPlayer(uuid, name);
            groupName = plugin.getPluginConfig().getFileConfiguration().getString("default-group", "default");
            expirationDate = 0;
            plugin.updateSubCommands(plugin.getAddSubCommand(), plugin.getInfoSubCommand(), plugin.getRemoveSubCommand());
            return;
        }
        databaseManager.loadProfile(this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Check if the player owns a group.
    public boolean hasGroup() {
        long expirationDate = getExpirationDate();
        long duration = expirationDate - System.currentTimeMillis();
        // When the time in a group has expired, the player is set in the default group.
        if (expirationDate != 0 && duration <= 0) {
            String groupName = getGroup();
            String defaultGroup = pluginConfig.getFileConfiguration().getString("default-group", "default");
            if (!(groupName.isEmpty()) && groupName.equalsIgnoreCase(defaultGroup)) {
                setGroup("");
            } else {
                setGroup(defaultGroup);
            }
            setExpirationDate(0);
            return true;
        }
        return !getGroup().isEmpty();
    }

    public String getGroup() {
        return groupName;
    }

    public void setGroup(String groupName) {
        this.groupName = groupName.toLowerCase();
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void uploadData() {
        /* When, for example, the player is leaving the server, if the player does not belong to any group, this information is only stored in the database at this point.
        Otherwise, the query is always performed using the hasGroup method, and thus the value in the database does not need to be updated when removing the group. */
        if (!(hasGroup())) {
            setGroup("");
            setExpirationDate(0);
        }
        databaseManager.uploadProfile(this);
    }
}
