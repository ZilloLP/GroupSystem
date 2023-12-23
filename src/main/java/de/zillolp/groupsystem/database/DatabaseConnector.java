package de.zillolp.groupsystem.database;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.DriverDataSource;
import de.zillolp.groupsystem.GroupSystem;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

public class DatabaseConnector {
    private final HikariDataSource hikariDataSource;
    private boolean isConnected;

    public DatabaseConnector(GroupSystem plugin, boolean useMySQL, String filename, String serverName, String port, String databaseName, String user, String password) {
        hikariDataSource = new HikariDataSource();
        hikariDataSource.setMaximumPoolSize(5);
        if (useMySQL) {
            hikariDataSource.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
            hikariDataSource.addDataSourceProperty("serverName", serverName);
            hikariDataSource.addDataSourceProperty("port", port);
            hikariDataSource.addDataSourceProperty("databaseName", databaseName);
            hikariDataSource.addDataSourceProperty("user", user);
            hikariDataSource.addDataSourceProperty("password", password);
        } else {
            File databaseFile = new File(plugin.getDataFolder(), filename + ".db");
            if (!(databaseFile.exists())) {
                try {
                    databaseFile.createNewFile();
                } catch (IOException exception) {
                    plugin.getLogger().log(Level.SEVERE, "Error creating database file", exception);
                }
            }
            hikariDataSource.setDataSource(new DriverDataSource("jdbc:sqlite:" + databaseFile.getAbsolutePath(), "org.sqlite.JDBC", new Properties(), user, password));
            hikariDataSource.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        }
        hikariDataSource.setConnectionTimeout(34000);
        hikariDataSource.setMaxLifetime(28740000);
        //It is checked whether the database is connected. If not, the datasource will be closed.
        try (Connection connection = getConnection()) {
            isConnected = connection.isValid(1);
        } catch (SQLException exception) {
            hikariDataSource.close();
            isConnected = false;
        }
    }

    public void close() {
        if (hikariDataSource.isClosed()) {
            return;
        }
        hikariDataSource.close();
    }

    public boolean hasConnection() {
        return isConnected;
    }

    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }
}
