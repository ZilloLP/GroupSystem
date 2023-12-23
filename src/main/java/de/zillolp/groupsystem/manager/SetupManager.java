package de.zillolp.groupsystem.manager;

import de.zillolp.groupsystem.profiles.TimeProfile;

import java.util.HashMap;
import java.util.UUID;

public class SetupManager {
    private final HashMap<UUID, TimeProfile> timeProfiles;

    public SetupManager() {
        timeProfiles = new HashMap<>();
    }

    public HashMap<UUID, TimeProfile> getTimeProfiles() {
        return timeProfiles;
    }
}
