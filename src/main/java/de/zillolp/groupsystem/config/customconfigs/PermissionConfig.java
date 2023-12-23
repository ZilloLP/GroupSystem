package de.zillolp.groupsystem.config.customconfigs;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.enums.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionConfig extends CustomConfig {
    public PermissionConfig(GroupSystem plugin, String name) {
        super(plugin, name);
    }

    public boolean hasPermission(CommandSender commandSender, Permission permission) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        return hasPermission((Player) commandSender, permission);
    }

    public boolean hasPermission(Player player, Permission permission) {
        return player.hasPermission(fileConfiguration.getString(permission.name(), permission.getDefaultPermission()));
    }
}
