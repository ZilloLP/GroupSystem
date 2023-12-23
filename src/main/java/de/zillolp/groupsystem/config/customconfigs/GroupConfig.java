package de.zillolp.groupsystem.config.customconfigs;

import de.zillolp.groupsystem.GroupSystem;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;

public class GroupConfig extends CustomConfig {
    private final String path;

    public GroupConfig(GroupSystem plugin, String name) {
        super(plugin, name);
        path = "Groups.";
    }

    public boolean hasGroup(String groupName) {
        return fileConfiguration.contains(path + groupName.toLowerCase());
    }

    public void createGroup(String groupName, String prefix, int sortNumber) {
        String section = path + groupName.toLowerCase();
        fileConfiguration.set(section + ".name", groupName);
        fileConfiguration.set(section + ".prefix", prefix);
        fileConfiguration.set(section + ".sort-number", sortNumber);
        save();
    }

    public void deleteGroup(String groupName) {
        fileConfiguration.set(path + groupName.toLowerCase(), null);
        save();
    }

    public String getGroupName(String groupName) {
        groupName = groupName.toLowerCase();
        String section = path + groupName + ".name";
        if (!(fileConfiguration.contains(section))) {
            return groupName;
        }
        return fileConfiguration.getString(section, groupName + " ");
    }

    public void setGroupName(String oldGroupName, String groupName) {
        if (oldGroupName.equalsIgnoreCase(groupName)) {
            fileConfiguration.set(path + groupName.toLowerCase() + ".name", groupName);
            save();
            return;
        }
        createGroup(groupName, getPrefix(oldGroupName), getSortNumber(groupName));
        deleteGroup(oldGroupName);
    }

    public String getPrefix(String groupName) {
        groupName = groupName.toLowerCase();
        return fileConfiguration.getString(path + groupName + ".prefix", groupName + " ");
    }

    public void setPrefix(String groupName, String prefix) {
        groupName = groupName.toLowerCase();
        fileConfiguration.set(path + groupName + ".prefix", prefix);
        save();
    }

    public String getPriority(String groupName) {
        groupName = groupName.toLowerCase();
        return String.format("%02d", getSortNumber(groupName));
    }

    private int getSortNumber(String groupName) {
        groupName = groupName.toLowerCase();
        return fileConfiguration.getInt(path + groupName + ".sort-number", 0);
    }

    public void setSortNumber(String groupName, int number) {
        groupName = groupName.toLowerCase();
        fileConfiguration.set(path + groupName + ".sort-number", number);
        save();
    }

    public String[] getGroups() {
        String section = "Groups";
        ArrayList<String> groups = new ArrayList<>();
        if (!(fileConfiguration.isConfigurationSection(section))) {
            return groups.toArray(new String[0]);
        }
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(section);
        if (configurationSection == null) {
            return groups.toArray(new String[0]);
        }
        groups.addAll(configurationSection.getKeys(false));
        return groups.toArray(new String[0]);
    }
}
