package com.example.webmagic.util;

/**
 * @author yinfelix
 * @date 2020/6/16
 */
public class TimeUtil {
    private static final String TIME_UNIT_MINUTE = "分钟";
    private static final String TIME_UNIT_HOUR = "小时";
    private static final String TIME_UNIT_DAY = "天";

    public static Long getTimeInMinutes(String timeString) {
        if (timeString.endsWith(TIME_UNIT_MINUTE)) {
            return Long.parseLong(timeString.replace(TIME_UNIT_MINUTE, ""));
        }
        if (timeString.endsWith(TIME_UNIT_HOUR)) {
            long timeInHours = Long.parseLong(timeString.replace(TIME_UNIT_HOUR, ""));
            return timeInHours * 60;
        }
        if (timeString.endsWith(TIME_UNIT_DAY)) {
            long timeInDays = Long.parseLong(timeString.replace(TIME_UNIT_DAY, ""));
            return timeInDays * 24 * 60;
        }
        return 0L;
    }
}
