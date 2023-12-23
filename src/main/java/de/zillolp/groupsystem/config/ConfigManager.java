package de.zillolp.groupsystem.config;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.*;

public class ConfigManager {
    private final GroupSystem plugin;
    private PluginConfig pluginConfig;
    private GroupConfig groupConfig;
    private LanguageConfig languageConfig;
    private LocationConfig locationConfig;
    private MySQLConfig mySQLConfig;
    private PermissionConfig permissionConfig;

    public ConfigManager(GroupSystem plugin) {
        this.plugin = plugin;
        registerConfigs();
    }

    private void registerConfigs() {
        pluginConfig = new PluginConfig(plugin, "config.yml");
        groupConfig = new GroupConfig(plugin, "groups.yml");
        languageConfig = new LanguageConfig(plugin, "language.yml");
        locationConfig = new LocationConfig(plugin, "locations.yml");
        mySQLConfig = new MySQLConfig(plugin, "mysql.yml");
        permissionConfig = new PermissionConfig(plugin, "permissions.yml");
    }

    public void reloadConfigs() {
        // The file configuration is stored in RAM by Spigot, and by reloading the file configuration, it is thus updated.
        pluginConfig.loadConfiguration();
        groupConfig.loadConfiguration();
        languageConfig.loadConfiguration();
        locationConfig.loadConfiguration();
        permissionConfig.loadConfiguration();
        mySQLConfig.loadConfiguration();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public GroupConfig getGroupsConfig() {
        return groupConfig;
    }

    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }

    public LocationConfig getLocationsConfig() {
        return locationConfig;
    }

    public MySQLConfig getMySQLConfig() {
        return mySQLConfig;
    }

    public PermissionConfig getPermissionsConfig() {
        return permissionConfig;
    }
}
