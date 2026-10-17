package com.arashi.xposed;

import java.util.Calendar;

/** Uses the clock's own calendar so timezone and demo time stay consistent. */
public final class StatusBarClockFormat {
    private static final String[] WEEKDAYS = { "日", "一", "二", "三", "四", "五", "六" };

    private StatusBarClockFormat() {}

    public static String suffix(Calendar calendar) {
        return " " + (calendar.get(Calendar.MONTH) + 1) + "月"
                + calendar.get(Calendar.DAY_OF_MONTH) + "日周"
                + WEEKDAYS[calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY];
    }
}
