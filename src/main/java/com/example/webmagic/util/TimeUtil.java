package com.example.webmagic.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.example.webmagic.service.DoubanApiService.DOUBAN_DATE_PATTERN;

/**
 * @author yinfelix
 * @date 2020/6/16
 */
@Slf4j
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

    public static LocalDate parseDateString(String dateString) {
        if (!StringUtils.isEmpty(dateString)) {
            /*
            yyyy-M
             */
            if (dateString.matches("^\\d{4}(-)(1[0-2]|\\d)")) {
                return LocalDate.parse(dateString + "-1", DateTimeFormatter.ofPattern(DOUBAN_DATE_PATTERN));
            }
            /*
            yyyy-M-d
             */
            if (dateString.matches("^\\d{4}(-)(1[0-2]\\d)\\1([1-2]\\d|30|31)$")) {
                return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DOUBAN_DATE_PATTERN));
            }
            /*
            yyyy-MM
             */
            if (dateString.matches("^\\d{4}(-)(1[0-2]|0\\d)")) {
                return LocalDate.parse(dateString + "-1", DateTimeFormatter.ofPattern(DOUBAN_DATE_PATTERN));
            }
            /*
            yyyy-MM-dd
             */
            if (dateString.matches("^\\d{4}(-)(1[0-2]|0\\d)\\1([0-2]\\d|30|31)$")) {
                return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        }
        log.error("{}无法解析！", dateString);
        return null;
    }
}
