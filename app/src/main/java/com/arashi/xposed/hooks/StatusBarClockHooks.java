package com.arashi.xposed.hooks;

import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.view.ViewParent;
import com.arashi.xposed.ConfigManager;
import com.arashi.xposed.HookConfig;
import com.arashi.xposed.Reflect;
import com.arashi.xposed.StatusBarClockFormat;
import io.github.libxposed.api.XposedModule;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;

/** Status bar date extension for the supplied Android 12 and 14 SystemUI builds. */
public final class StatusBarClockHooks {
    private static final String TAG = "[Arashi-Clock]";

    private StatusBarClockHooks() {}

    public static void hookSystemUI(XposedModule module, ClassLoader loader) {
        try {
            Class<?> clock = Class.forName("com.android.systemui.statusbar.policy.Clock", false, loader);
            Method smallTime = Reflect.findMethodExact(clock, "getSmallTime");
            Field calendar = clock.getDeclaredField("mCalendar");
            calendar.setAccessible(true);
            if (smallTime == null) throw new NoSuchMethodException("getSmallTime");
            module.hook(smallTime).intercept(chain -> {
                // Initialize the native time/accessibility formatters and preserve
                // AM/PM spans, 12/24-hour settings and optional seconds.
                Object original = chain.proceed();
                if (!ConfigManager.isEnabled(HookConfig.KEY_STATUS_BAR_DATE)) return original;
                TextView view = (TextView) chain.getThisObject();
                if (!isStatusClock(view) || !(original instanceof CharSequence)) return original;
                try {
                    Calendar current = (Calendar) calendar.get(view);
                    if (current != null) return new SpannableStringBuilder((CharSequence) original)
                            .append(StatusBarClockFormat.suffix(current));
                } catch (Exception error) { Log.w(TAG, "Unable to format status bar date", error); }
                return original;
            });
            // Native updates overwrite contentDescription after getSmallTime.
            // Keep the added date available to screen readers as well.
            for (Method method : clock.getDeclaredMethods()) {
                if (!method.getName().equals("updateClock")
                        && !method.getName().equals("dispatchDemoCommand")) continue;
                module.hook(method).intercept(chain -> {
                    Object result = chain.proceed();
                    if (ConfigManager.isEnabled(HookConfig.KEY_STATUS_BAR_DATE)) {
                        TextView view = (TextView) chain.getThisObject();
                        if (isStatusClock(view)) view.setContentDescription(view.getText());
                    }
                    return result;
                });
            }
            Log.i(TAG, "Status bar date hooks registered");
        } catch (Throwable error) { Log.w(TAG, "Status bar date hooks unavailable", error); }
    }

    private static boolean isStatusClock(TextView view) {
        // QS and other clocks must keep their original text and layout.
        if (view.getId() <= 0) return false;
        try {
            if (!"clock".equals(view.getResources().getResourceEntryName(view.getId()))) return false;
        } catch (Resources.NotFoundException ignored) { return false; }
        for (ViewParent parent = view.getParent(); parent instanceof View; parent = parent.getParent()) {
            for (Class<?> type = parent.getClass(); type != null; type = type.getSuperclass()) {
                if (type.getName().equals("com.android.systemui.statusbar.phone.PhoneStatusBarView")) return true;
            }
        }
        return false;
    }
}
