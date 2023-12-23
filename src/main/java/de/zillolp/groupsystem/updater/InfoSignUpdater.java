package de.zillolp.groupsystem.updater;

import de.zillolp.groupsystem.GroupSystem;
import de.zillolp.groupsystem.infosigns.InfoSignManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InfoSignUpdater extends CustomUpdater {
    private final InfoSignManager infoSignManager;

    public InfoSignUpdater(GroupSystem plugin, InfoSignManager infoSignManager, int ticks) {
        super(plugin, ticks);
        this.infoSignManager = infoSignManager;
    }

    @Override
    protected void tick() {
        if (infoSignManager == null) {
            plugin.getLogger().warning("InfoSignManager is null in InfoSignUpdater.tick()");
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            infoSignManager.sendInfoSigns(player, false);
        }
    }
}
