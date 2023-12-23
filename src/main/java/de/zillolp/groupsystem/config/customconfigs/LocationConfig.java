package de.zillolp.groupsystem.config.customconfigs;

import de.zillolp.groupsystem.GroupSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;

public class LocationConfig extends CustomConfig {
    private final String path;

    public LocationConfig(GroupSystem plugin, String name) {
        super(plugin, name);
        path = "locations.";
    }

    public Location getLocation(String locationName) {
        String section = path + locationName;
        String worldName = fileConfiguration.getString(section + ".world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        double x = fileConfiguration.getDouble(section + ".x");
        double y = fileConfiguration.getDouble(section + ".y");
        double z = fileConfiguration.getDouble(section + ".z");
        float yaw = (float) fileConfiguration.getDouble(section + ".yaw");
        float pitch = (float) fileConfiguration.getDouble(section + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public ArrayList<Location> getLocations(String section) {
        ArrayList<Location> locations = new ArrayList<>();
        if (!(fileConfiguration.isConfigurationSection(path + section))) {
            return locations;
        }
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(path + section);
        if (configurationSection == null) {
            return locations;
        }
        for (String locationName : configurationSection.getKeys(false)) {
            locations.add(getLocation(section + "." + locationName));
        }
        return locations;
    }

    public void saveLocation(String locationName, Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        String section = path + locationName;
        fileConfiguration.set(section + ".world", world.getName());
        fileConfiguration.set(section + ".x", location.getX());
        fileConfiguration.set(section + ".y", location.getY());
        fileConfiguration.set(section + ".z", location.getZ());
        fileConfiguration.set(section + ".yaw", location.getYaw());
        fileConfiguration.set(section + ".pitch", location.getPitch());
        save();
    }

    public void saveInfoSignLocation(Location location) {
        boolean isExisting = false;
        String section = "info-signs.";
        int number = 0;
        do {
            if (fileConfiguration.contains(path + section + number)) {
                number++;
                continue;
            }
            saveLocation(section + number, location);
            isExisting = true;
        } while (!(isExisting));
        save();
    }

    public void removeLocation(String section, Location location) {
        if (!(fileConfiguration.isConfigurationSection(path + section))) {
            return;
        }
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(path + section);
        if (configurationSection == null) {
            return;
        }
        for (String locationName : configurationSection.getKeys(false)) {
            Location location1 = getLocation(section + "." + locationName);
            if (location.getBlockX() != location1.getBlockX() || location.getBlockY() != location1.getBlockY() || location.getBlockZ() != location1.getBlockZ()) {
                continue;
            }
            removeLocation(section + "." + locationName);
            break;
        }
    }

    public void removeLocation(String locationName) {
        fileConfiguration.set(path + locationName, null);
        save();
    }
}
