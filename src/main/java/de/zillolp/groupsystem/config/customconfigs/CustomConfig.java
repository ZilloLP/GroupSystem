package de.zillolp.groupsystem.config.customconfigs;

import de.zillolp.groupsystem.GroupSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomConfig {
    protected final GroupSystem plugin;
    private final File file;
    protected FileConfiguration fileConfiguration;

    public CustomConfig(GroupSystem plugin, String name) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), name);
        if (!(file.exists())) {
            plugin.saveResource(name, true);
        }
        loadConfiguration();
    }

    public void loadConfiguration() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    protected void save() {
        try {
            fileConfiguration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().throwing(this.getClass().getName(), "save", exception);
        }
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }
}
