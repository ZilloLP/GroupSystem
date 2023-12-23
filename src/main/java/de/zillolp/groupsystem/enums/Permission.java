package de.zillolp.groupsystem.enums;

public enum Permission {

    ADMIN_PERMISSION("groupsystem.admin"),
    INFO_PERMISSION("groupsystem.info"),
    INFO_OTHER_PERMISSION("groupsystem.info.other");

    private final String defaultPermission;

    Permission(String defaultPermission) {
        this.defaultPermission = defaultPermission;
    }

    public String getDefaultPermission() {
        return defaultPermission;
    }
}
