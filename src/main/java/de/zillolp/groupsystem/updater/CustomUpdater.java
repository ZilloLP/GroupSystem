package de.zillolp.groupsystem.updater;

import de.zillolp.groupsystem.GroupSystem;

public abstract class CustomUpdater implements Runnable {
    protected final GroupSystem plugin;
    private final int taskID;

    public CustomUpdater(GroupSystem plugin, int ticks) {
        this.plugin = plugin;
        taskID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 0, ticks);
    }

    @Override
    public void run() {
        tick();
    }

    protected abstract void tick();

    public void stop() {
        plugin.getServer().getScheduler().cancelTask(taskID);
    }
}
