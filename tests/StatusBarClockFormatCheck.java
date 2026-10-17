import com.arashi.xposed.StatusBarClockFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class StatusBarClockFormatCheck {
    private static void expect(Calendar calendar, String text) {
        String result = StatusBarClockFormat.suffix(calendar);
        if (!text.equals(result)) throw new AssertionError(result + " != " + text);
    }

    public static void main(String[] args) {
        Calendar clock = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"));
        clock.clear();
        clock.set(2026, Calendar.OCTOBER, 17, 23, 59);
        expect(clock, " 10月17日周六");
        clock.add(Calendar.MINUTE, 1);
        expect(clock, " 10月18日周日");
        clock.set(2026, Calendar.DECEMBER, 31, 23, 59);
        expect(clock, " 12月31日周四");
        clock.add(Calendar.MINUTE, 1);
        expect(clock, " 1月1日周五");
        clock.set(2024, Calendar.FEBRUARY, 28, 23, 59);
        clock.add(Calendar.MINUTE, 1);
        expect(clock, " 2月29日周四");
        clock.clear();
        clock.set(2026, Calendar.OCTOBER, 18, 0, 30);
        clock.getTimeInMillis();
        clock.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        expect(clock, " 10月17日周六");
        for (int i = 0; i < 7; i++) {
            clock.set(2026, Calendar.OCTOBER, 18 + i, 12, 0);
            expect(clock, " 10月" + (18 + i) + "日周" + "日一二三四五六".charAt(i));
        }
        System.out.println("PASS: one space, weekday mapping, midnight, year, leap day and timezone");
    }
}
