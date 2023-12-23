package de.zillolp.groupsystem.commands.subcommands;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.LanguageConfig;
import de.zillolp.groupsystem.config.customconfigs.PermissionConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SubCommand {
    protected final GroupSystem plugin;
    protected final LanguageConfig languageConfig;
    protected final PermissionConfig permissionConfig;
    private final String mainCommand;
    private String[] subCommands;

    public SubCommand(GroupSystem plugin, String mainCommand, String... subCommands) {
        this.plugin = plugin;
        this.languageConfig = plugin.getLanguageConfig();
        this.permissionConfig = plugin.getPermissionConfig();
        this.mainCommand = mainCommand;
        this.subCommands = subCommands;
    }

    public abstract boolean onCommand(GroupSystem cookieClicker, CommandSender commandSender, Command command, String[] args);

    public abstract boolean hasPermission(CommandSender commandSender);

    public void setSubCommands(String[] subCommands) {
        this.subCommands = subCommands;
    }

    public String getMainCommand() {
        return mainCommand;
    }

    public List<String> getTabCommands(String subCommand, String command, int position) {
        if (subCommands.length < position) {
            return new ArrayList<>();
        }
        position--;
        List<String> tabCommands = Arrays.asList(subCommands[position].split(";"));
        if (position > 0) {
            position--;
        } else if (mainCommand.equalsIgnoreCase(command)) {
            return tabCommands;
        }
        for (String replacedCommand : subCommands[position].split(";")) {
            if (replacedCommand.equalsIgnoreCase(command) && subCommand.equalsIgnoreCase(mainCommand)) {
                return tabCommands;
            }
        }
        return new ArrayList<>();
    }
}
