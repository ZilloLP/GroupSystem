package de.zillolp.groupsystem.profiles;

import de.zillolp.groupsystem.enums.TimeType;

import java.util.UUID;

public class TimeProfile {
    private final UUID uuid;
    private final String groupName;
    private TimeType timeType;
    private long time;

    public TimeProfile(UUID uuid, String groupName) {
        this.uuid = uuid;
        this.groupName = groupName;
        timeType = TimeType.DAYS;
        time = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getGroupName() {
        return groupName;
    }

    public TimeType getTimeType() {
        return timeType;
    }

    public void setTimeType(TimeType timeType) {
        this.timeType = timeType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void addTime(long time) {
        setTime(getTime() + time);
    }
}
