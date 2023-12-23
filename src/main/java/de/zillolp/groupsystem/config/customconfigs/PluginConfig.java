package de.zillolp.groupsystem.config.customconfigs;

import de.zillolp.groupsystem.GroupSystem;

public class PluginConfig extends CustomConfig {
    public PluginConfig(GroupSystem plugin, String name) {
        super(plugin, name);
    }

    public void setDefaultGroup(String groupName) {
        fileConfiguration.set("default-group", groupName.toLowerCase());
        save();
    }
}
