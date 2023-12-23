package de.zillolp.groupsystem.database;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.PluginConfig;
import de.zillolp.groupsystem.profiles.PlayerProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private final GroupSystem plugin;
    private final PluginConfig pluginConfig;
    private final Logger logger;
    private final DatabaseConnector databaseConnector;
    private final String playerTable;

    public DatabaseManager(GroupSystem plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.logger = plugin.getLogger();
        this.databaseConnector = plugin.getDatabaseConnector();
        this.playerTable = pluginConfig.getFileConfiguration().getString("table-name", "groupsystem_players");
        // When initializing the manager, the database table should also be created immediately if it does not already exist.
        initialize();
    }

    private void initialize() {
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + playerTable + "(UUID VARCHAR(64), NAME VARCHAR(64), GROUP_NAME VARCHAR(64), EXPIRATION_DATE BIGINT);");
            preparedStatement.executeUpdate();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error initializing database", exception);
        }
    }

    public void createPlayer(UUID uuid, String name) {
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + playerTable + "(UUID, NAME, GROUP_NAME, EXPIRATION_DATE) VALUES (?, ?, ?, ?);");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, pluginConfig.getFileConfiguration().getString("default-group", "default"));
            preparedStatement.setLong(4, 0);
            preparedStatement.executeUpdate();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error creating player", exception);
        }
    }

    public boolean playerExists(String name) {
        boolean isExisting = false;
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT NAME FROM " + playerTable + " WHERE NAME= ?");
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            isExisting = resultSet.next() && resultSet.getString("NAME") != null;
            resultSet.close();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error checking player existence by name", exception);
        }
        return isExisting;
    }

    public boolean playerExists(UUID uuid, String name) {
        boolean isExisting = false;
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID, NAME FROM " + playerTable + " WHERE UUID= ?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            isExisting = resultSet.next() && resultSet.getString("UUID") != null;

            /* The "playerExists" method is primarily used when loading the profile.
            Since the player's name is also stored in the database, it needs to be checked whether the name has been changed, so that it can be updated in the database as well. */
            if (isExisting && (!(name.equals(resultSet.getString("NAME"))))) {
                PreparedStatement preparedStatement1 = connection.prepareStatement("UPDATE " + playerTable + " SET NAME= ? WHERE UUID= ?");
                preparedStatement1.setString(1, name);
                preparedStatement1.setString(2, uuid.toString());
                preparedStatement1.executeUpdate();
                preparedStatement1.closeOnCompletion();
            }
            resultSet.close();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error checking player existence by uuid", exception);
        }
        return isExisting;
    }

    public void loadProfiles() {
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID FROM " + playerTable);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                // All player profiles are loaded from the database, and if they have already been loaded, they will not be loaded again.
                ConcurrentHashMap<UUID, PlayerProfile> playerProfiles = plugin.getPlayerManager().getPlayerProfiles();
                if (playerProfiles.containsKey(uuid)) {
                    continue;
                }
                playerProfiles.put(uuid, new PlayerProfile(plugin, uuid));
            }
            resultSet.close();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error loading profiles", exception);
        }
    }

    public void loadProfile(PlayerProfile playerProfile) {
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + playerTable + " WHERE UUID= ?");
            preparedStatement.setString(1, playerProfile.getUuid().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                playerProfile.setName(resultSet.getString("NAME"));
                playerProfile.setGroup(resultSet.getString("GROUP_NAME"));
                playerProfile.setExpirationDate(resultSet.getLong("EXPIRATION_DATE"));
            }
            resultSet.close();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error loading profile", exception);
        }
    }

    public void uploadProfile(PlayerProfile playerProfile) {
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + playerTable + " SET GROUP_NAME= ?, EXPIRATION_DATE= ? WHERE UUID= ?");
            preparedStatement.setString(1, playerProfile.getGroup());
            preparedStatement.setLong(2, playerProfile.getExpirationDate());
            preparedStatement.setString(3, playerProfile.getUuid().toString());
            preparedStatement.executeUpdate();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error uploading profile", exception);
        }
    }

    public UUID getUUID(String playerName) {
        UUID uuid = null;
        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID FROM " + playerTable + " WHERE NAME= ?");
            preparedStatement.setString(1, playerName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                uuid = UUID.fromString(resultSet.getString("UUID"));
            }
            resultSet.close();
            preparedStatement.closeOnCompletion();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error getting uuid", exception);
        }
        return uuid;
    }
}
