package com.netcafe.util;

import java.time.Duration;

public class TimeUtil {

    public static String formatDuration(long seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long secs = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
