package de.zillolp.groupsystem.listener;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.config.customconfigs.LanguageConfig;
import de.zillolp.groupsystem.config.customconfigs.PermissionConfig;
import de.zillolp.groupsystem.enums.Language;
import de.zillolp.groupsystem.enums.Permission;
import de.zillolp.groupsystem.infosigns.InfoSignManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class InfoSignListener implements Listener {
    private final GroupSystem plugin;
    private final LanguageConfig languageConfig;
    private final PermissionConfig permissionConfig;
    private final InfoSignManager infoSignManager;

    public InfoSignListener(GroupSystem plugin) {
        this.plugin = plugin;
        languageConfig = plugin.getLanguageConfig();
        permissionConfig = plugin.getPermissionConfig();
        infoSignManager = plugin.getInfoSignManager();
    }

    @EventHandler
    public void onSignChangeEvent(SignChangeEvent event) {
        String line = event.getLine(0);
        if ((line == null) || (!(line.equalsIgnoreCase(plugin.getPluginConfig().getFileConfiguration().getString("create-tag", "[Info]"))))) {
            return;
        }
        Player player = event.getPlayer();
        if (!(permissionConfig.hasPermission(player, Permission.ADMIN_PERMISSION))) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Block block = event.getBlock();
                Location location = block.getLocation();
                if (infoSignManager.hasSignLocation(location)) {
                    player.sendMessage(languageConfig.getLanguageWithPrefix(Language.SIGN_ALREADY_EXISTS));
                    return;
                }
                infoSignManager.saveSignLocation(location);
                player.sendMessage(languageConfig.getLanguageWithPrefix(Language.CREATE_SIGN));
            }
        });
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getType().name().contains("SIGN"))) {
            return;
        }
        Player player = event.getPlayer();
        Location location = block.getLocation();
        if (!(infoSignManager.hasSignLocation(location))) {
            return;
        }
        if (!(permissionConfig.hasPermission(player, Permission.ADMIN_PERMISSION))) {
            event.setCancelled(true);
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                infoSignManager.removeSignLocation(location);
                player.sendMessage(languageConfig.getLanguageWithPrefix(Language.DELETE_SIGN));
            }
        });
    }

    // The following events are for the resend of the packets that are "lost" during, for example, a world change.

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || (!(block.getType().name().contains("SIGN")))) {
            return;
        }
        Location location = block.getLocation();
        if (!(infoSignManager.hasSignLocation(location))) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                infoSignManager.sendInfoSigns(event.getPlayer(), true);
            }
        }, 1);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                infoSignManager.sendInfoSigns(event.getPlayer(), true);
            }
        }, 1);
    }
}
