package de.zillolp.groupsystem;

import de.zillolp.groupsystem.commands.maincommands.GroupSystemCommand;
import de.zillolp.groupsystem.commands.maincommands.GroupsCommand;
import de.zillolp.groupsystem.commands.subcommands.SubCommand;
import de.zillolp.groupsystem.commands.subcommands.groups.*;
import de.zillolp.groupsystem.commands.subcommands.groupsystem.HelpSubCommand;
import de.zillolp.groupsystem.commands.subcommands.groupsystem.ReloadSubCommand;
import de.zillolp.groupsystem.config.ConfigManager;
import de.zillolp.groupsystem.config.customconfigs.*;
import de.zillolp.groupsystem.database.DatabaseConnector;
import de.zillolp.groupsystem.database.DatabaseManager;
import de.zillolp.groupsystem.infosigns.InfoSignManager;
import de.zillolp.groupsystem.listener.GroupSetupListener;
import de.zillolp.groupsystem.listener.InfoSignListener;
import de.zillolp.groupsystem.listener.PlayerChatListener;
import de.zillolp.groupsystem.listener.PlayerConnectionListener;
import de.zillolp.groupsystem.manager.PlayerManager;
import de.zillolp.groupsystem.manager.SetupManager;
import de.zillolp.groupsystem.manager.TablistManager;
import de.zillolp.groupsystem.profiles.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GroupSystem extends JavaPlugin {
    private PluginConfig pluginConfig;
    private GroupConfig groupConfig;
    private LanguageConfig languageConfig;
    private LocationConfig locationConfig;
    private MySQLConfig mySQLConfig;
    private PermissionConfig permissionConfig;
    private DatabaseConnector databaseConnector;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerManager playerManager;
    private SetupManager setupManager;
    private InfoSignManager infoSignManager;
    private TablistManager tablistManager;
    private AddSubCommand addSubCommand;
    private DeleteSubCommand deleteSubCommand;
    private InfoSubCommand infoSubCommand;
    private RemoveSubCommand removeSubCommand;
    private SetSubCommand setSubCommand;

    @Override
    public void onEnable() {
        initializeConfigs();
        connectDatabase();
        if (!databaseConnector.hasConnection()) {
            // Initialize the commands even in case of a non-existent database connection, as otherwise a Null Pointer Exception occurs.
            getCommand("groupsystem").setExecutor(new GroupSystemCommand(this));
            getCommand("groups").setExecutor(new GroupsCommand(this));
            getLogger().warning("The plugin isn't connected to the database.");
            return;
        }
        initializeManager();
        loadPlayers();
        initializeCommands();
        registerListener(Bukkit.getPluginManager());
    }

    @Override
    public void onDisable() {
        if (!(databaseConnector.hasConnection())) {
            return;
        }
        unloadPlayers();
        stopUpdater();
        databaseConnector.close();
    }

    private void initializeConfigs() {
        configManager = new ConfigManager(this);
        pluginConfig = configManager.getPluginConfig();
        groupConfig = configManager.getGroupsConfig();
        languageConfig = configManager.getLanguageConfig();
        locationConfig = configManager.getLocationsConfig();
        mySQLConfig = configManager.getMySQLConfig();
        permissionConfig = configManager.getPermissionsConfig();
    }

    private void connectDatabase() {
        boolean useMysql = pluginConfig.getFileConfiguration().getBoolean("mysql", false);
        FileConfiguration mysqlConfiguration = mySQLConfig.getFileConfiguration();
        String address = mysqlConfiguration.getString("Host", "");
        String port = mysqlConfiguration.getString("Port", "");
        String dbName = mysqlConfiguration.getString("Database", "");
        String username = mysqlConfiguration.getString("User", "");
        String password = mysqlConfiguration.getString("Password", "");
        // Create the DatabaseConnector through which the database connection is established via HikariCP.
        databaseConnector = new DatabaseConnector(this, useMysql, "groupsystem", address, port, dbName, username, password);
    }

    private void initializeManager() {
        databaseManager = new DatabaseManager(this);
        playerManager = new PlayerManager(this);
        setupManager = new SetupManager();
        infoSignManager = new InfoSignManager(this);
        tablistManager = new TablistManager(this);
    }

    private void initializeCommands() {
        // Initialization of all "groupsystem" maincommands and subcommands.
        GroupSystemCommand groupSystemCommand = new GroupSystemCommand(this);
        getCommand("groupsystem").setExecutor(groupSystemCommand);
        groupSystemCommand.registerSubCommand(new HelpSubCommand(this, "help"));
        groupSystemCommand.registerSubCommand(new ReloadSubCommand(this, "reload"));

        // Initialization of all "groups" maincommands and subcommands.
        GroupsCommand groupsCommand = new GroupsCommand(this);
        getCommand("groups").setExecutor(groupsCommand);
        groupsCommand.registerSubCommand(new CreateSubCommand(this, "create"));

        addSubCommand = new AddSubCommand(this, "add");
        groupsCommand.registerSubCommand(addSubCommand);

        deleteSubCommand = new DeleteSubCommand(this, "delete");
        groupsCommand.registerSubCommand(deleteSubCommand);

        infoSubCommand = new InfoSubCommand(this, "info");
        groupsCommand.registerSubCommand(infoSubCommand);

        removeSubCommand = new RemoveSubCommand(this, "remove");
        groupsCommand.registerSubCommand(removeSubCommand);

        setSubCommand = new SetSubCommand(this, "set");
        groupsCommand.registerSubCommand(setSubCommand);

        // Initialize the command tab options for the individual subcommands.
        updateSubCommands(addSubCommand, deleteSubCommand, infoSubCommand, removeSubCommand, setSubCommand);
    }

    private String[] getGroupSubCommands(SubCommand subCommand) {
        if (subCommand == null) {
            return new String[0];
        }
        StringBuilder groups = new StringBuilder();
        for (String groupName : groupConfig.getGroups()) {
            groups.append(groupName).append(";");
        }
        StringBuilder players = new StringBuilder();
        for (PlayerProfile playerProfile : playerManager.getPlayerProfiles().values()) {
            players.append(playerProfile.getName()).append(";");
        }
        // Sorting by subcommand for specific tab options.
        if (subCommand instanceof AddSubCommand) {
            return new String[]{"permanent;temporarily", players.toString(), groups.toString()};
        } else if (subCommand instanceof DeleteSubCommand) {
            return new String[]{groups.toString()};
        } else if (subCommand instanceof InfoSubCommand) {
            return new String[]{players.toString()};
        } else if (subCommand instanceof RemoveSubCommand) {
            return new String[]{players.toString()};
        } else if (subCommand instanceof SetSubCommand) {
            return new String[]{"name;prefix;priority", groups.toString()};
        }
        return new String[0];
    }

    public void updateSubCommands(SubCommand... subCommands) {
        /* To ensure that all group and player names are present in the tab options,
        setting the tab options must occur 4 ticks later, for example, if a new player or group has been created beforehand. */
        getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                for (SubCommand subCommand : subCommands) {
                    subCommand.setSubCommands(getGroupSubCommands(subCommand));
                }
            }
        }, 4L);
    }

    private void registerListener(PluginManager pluginManager) {
        pluginManager.registerEvents(new InfoSignListener(this), this);
        pluginManager.registerEvents(new PlayerChatListener(this), this);
        pluginManager.registerEvents(new PlayerConnectionListener(this), this);
        pluginManager.registerEvents(new GroupSetupListener(this), this);
    }

    private void loadPlayers() {
        /* First, the players who are on the server are registered so that when loading all profiles, they are not loaded twice.
        Additionally, this prevents short-term data that is not yet available. Since it is executed in the onEnable, it should be synchronous. */
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerManager.registerPlayer(player, false);
        }
        databaseManager.loadProfiles();
    }

    private void unloadPlayers() {
        // The unregistration of players must be done asynchronously since it is executed in the onDisable method.
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerManager.unregisterPlayer(player, false);
        }
    }

    private void stopUpdater() {
        infoSignManager.getInfoSignUpdater().stop();
        tablistManager.getTablistUpdater().stop();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public GroupConfig getGroupConfig() {
        return groupConfig;
    }

    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }

    public LocationConfig getLocationConfig() {
        return locationConfig;
    }

    public PermissionConfig getPermissionConfig() {
        return permissionConfig;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public SetupManager getSetupManager() {
        return setupManager;
    }

    public InfoSignManager getInfoSignManager() {
        return infoSignManager;
    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    public AddSubCommand getAddSubCommand() {
        return addSubCommand;
    }

    public DeleteSubCommand getDeleteSubCommand() {
        return deleteSubCommand;
    }

    public InfoSubCommand getInfoSubCommand() {
        return infoSubCommand;
    }

    public RemoveSubCommand getRemoveSubCommand() {
        return removeSubCommand;
    }

    public SetSubCommand getSetSubCommand() {
        return setSubCommand;
    }
}
