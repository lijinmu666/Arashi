package com.arashi.xposed.hooks;

import android.content.Context;
import android.content.ContentResolver;
import android.util.Log;
import android.os.Bundle;

import io.github.libxposed.api.XposedModule;

import com.arashi.xposed.ConfigManager;
import com.arashi.xposed.HookConfig;
import com.arashi.xposed.Reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class DevOptionsHooks {

    private static final String TAG = "[Arashi-DevOpt]";

    private static final String PROP_ALLOW = "sys.allow.development";
    private static final String KEY_SPECIAL_ADB = "special_enable_adb_debug";

    private DevOptionsHooks() {
    }

    public static void hookSettings(XposedModule mod, ClassLoader cl) {


        hookSysPropGet(mod, cl);

        hookSysPropSet(mod, cl);

        hookEnabler(mod, cl);

        hookSpecialAdbDebug(mod, cl);

        hookSettingsSideMenu(mod, cl);

        hookDeveloperPanelHostCompatibility(mod, cl);

        hookDeveloperPanelPause(mod, cl);

        Log.i(TAG, "settings hooks initialized");
    }

    private static final String DEV_FRAGMENT =
            "com.android.settings.development.DevelopmentSettingsDashboardFragment";

    private static void hookSettingsSideMenu(XposedModule mod, ClassLoader cl) {
        try {
            Class<?> headListClass = Class.forName("com.android.settings.zyb.ui.ZybHeadList", false, cl);
            Method method = Reflect.findMethodExact(headListClass, "getZybSettingsHeaders", Context.class);
            if (method == null) {
                Log.w(TAG, "getZybSettingsHeaders not found");
                return;
            }
            mod.hook(method).intercept(chain -> {
                Object original = chain.proceed();
                if (!(original instanceof List)
                        || !ConfigManager.isEnabled(HookConfig.KEY_DEV_OPTIONS)
                        || !ConfigManager.isEnabled(HookConfig.KEY_DEV_OPTIONS_MENU)) {
                    return original;
                }
                List<?> headers = (List<?>) original;
                for (Object header : headers) {
                    Object fragment = Reflect.callMethod(header, "getFragmentName");
                    if (DEV_FRAGMENT.equals(fragment)) return original;
                }

                Context context = (Context) chain.getArg(0);
                int title = context.getResources().getIdentifier(
                        "development_settings_title", "string", "com.android.settings");
                int icon = context.getResources().getIdentifier(
                        "ic_settings_development", "drawable", "com.android.settings");
                if (title == 0 || icon == 0) {
                    Log.w(TAG, "developer menu title or icon resource missing");
                    return original;
                }

                Class<?> headerClass = Class.forName("com.android.settings.zyb.ui.ZybSettingsHeader", false, cl);
                Object injected = headerClass.getDeclaredConstructor().newInstance();
                setHeaderValue(headerClass, injected, "setTitleResId", int.class, title);
                setHeaderValue(headerClass, injected, "setImgResId", int.class, icon);
                setHeaderValue(headerClass, injected, "setFragmentName", String.class, DEV_FRAGMENT);
                setHeaderValue(headerClass, injected, "setVisible", boolean.class, true);
                List<Object> result = new ArrayList<>(headers);
                result.add(injected);
                Log.i(TAG, "developer options side menu entry injected");
                return result;
            });
            Log.i(TAG, "settings side menu hook registered");
        } catch (Throwable error) {
            Log.w(TAG, "settings side menu hook failed: " + error);
        }
    }

    private static void setHeaderValue(Class<?> owner, Object instance, String name,
                                       Class<?> parameter, Object value) throws Exception {
        Method setter = owner.getMethod(name, parameter);
        setter.invoke(instance, value);
    }

    /** ZybSettings is a two-pane host, while the stock dashboard assumes SettingsActivity. */
    private static void hookDeveloperPanelHostCompatibility(XposedModule mod, ClassLoader cl) {
        try {
            Class<?> fragmentClass = Class.forName(DEV_FRAGMENT, false, cl);
            Method method = Reflect.findMethodExact(fragmentClass, "onActivityCreated", Bundle.class);
            if (method == null) {
                Log.w(TAG, "developer dashboard onActivityCreated not found");
                return;
            }
            mod.hook(method).intercept(chain -> {
                try {
                    return chain.proceed();
                } catch (ClassCastException error) {
                    Object fragment = chain.getThisObject();
                    Object activity = Reflect.callMethod(fragment, "getActivity");
                    if (activity == null || !"com.android.settings.zyb.ZybSettings".equals(
                            activity.getClass().getName())) {
                        throw error;
                    }
                    try {
                        Object switchBar = Reflect.callMethod(activity, "getSwitchBar");
                        if (switchBar == null) throw error;
                        setField(fragment, "mSwitchBar", switchBar);
                        switchBar.getClass().getMethod("setTitle", CharSequence.class).invoke(
                                switchBar, ((Context) activity).getString(
                                        ((Context) activity).getResources().getIdentifier(
                                                "developer_options_main_switch_title", "string",
                                                "com.android.settings")));
                        switchBar.getClass().getMethod("show").invoke(switchBar);

                        Class<?> switchController = Class.forName(
                                "com.android.settings.development.DevelopmentSwitchBarController", false, cl);
                        Class<?> switchBarClass = Class.forName(
                                "com.android.settings.widget.SettingsMainSwitchBar", false, cl);
                        Class<?> lifecycleClass = Class.forName(
                                "com.android.settingslib.core.lifecycle.Lifecycle", false, cl);
                        Object lifecycle = Reflect.callMethod(fragment, "getSettingsLifecycle");
                        Constructor<?> constructor = switchController.getConstructor(
                                fragmentClass, switchBarClass, boolean.class, lifecycleClass);
                        Object controller = constructor.newInstance(fragment, switchBar, true, lifecycle);
                        setField(fragment, "mSwitchBarController", controller);
                        if (ConfigManager.isEnabled(HookConfig.KEY_DEV_OPTIONS)) {
                            Reflect.callMethod(fragment, "enableDeveloperOptions");
                        }
                        Log.i(TAG, "developer dashboard attached to ZybSettings side panel");
                        return null;
                    } catch (Throwable recoveryError) {
                        Log.w(TAG, "developer dashboard host adaptation failed: " + recoveryError);
                        return null;
                    }
                }
            });
            Log.i(TAG, "developer dashboard host compatibility hook registered");
        } catch (Throwable error) {
            Log.w(TAG, "developer dashboard host compatibility hook failed: " + error);
        }
    }

    private static void setField(Object instance, String name, Object value) throws Exception {
        Class<?> type = instance.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(instance, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void hookDeveloperPanelPause(XposedModule mod, ClassLoader cl) {
        try {
            Class<?> fragmentClass = Class.forName(DEV_FRAGMENT, false, cl);
            Method method = Reflect.findMethodExact(fragmentClass, "onPause");
            if (method == null) return;
            mod.hook(method).intercept(chain -> {
                Object result = chain.proceed();
                Object activity = Reflect.callMethod(chain.getThisObject(), "getActivity");
                if (activity != null && "com.android.settings.zyb.ZybSettings".equals(
                        activity.getClass().getName())) {
                    Object switchBar = Reflect.callMethod(activity, "getSwitchBar");
                    if (switchBar != null) Reflect.callMethod(switchBar, "hide");
                }
                return result;
            });
        } catch (Throwable error) {
            Log.w(TAG, "developer panel pause hook failed: " + error);
        }
    }

private static void hookSysPropGet(XposedModule mod, ClassLoader cl) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties", false, cl);
            Method m = Reflect.findMethodExact(clz, "getBoolean", String.class, boolean.class);
            if (m == null) {
                Log.w(TAG, "SystemProperties.getBoolean not found");
                return;
            }
            mod.hook(m).intercept(chain -> {
                if (PROP_ALLOW.equals(chain.getArg(0))
                        && ConfigManager.isEnabled(HookConfig.KEY_DEV_OPTIONS)) {
                    return true;
                }
                return chain.proceed();
            });
            Log.i(TAG, "SystemProperties.getBoolean hook registered");
        } catch (Throwable t) {
            Log.w(TAG, "SystemProperties.getBoolean hook failed: " + t);
        }
    }

private static void hookSysPropSet(XposedModule mod, ClassLoader cl) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties", false, cl);
            Method m = Reflect.findMethodExact(clz, "set", String.class, String.class);
            if (m == null) {
                Log.w(TAG, "SystemProperties.set not found");
                return;
            }
            mod.hook(m).intercept(chain -> {
                if (PROP_ALLOW.equals(chain.getArg(0))
                        && "false".equals(chain.getArg(1))
                        && ConfigManager.isEnabled(HookConfig.KEY_DEV_OPTIONS)) {
                    Log.i(TAG, "block sys.allow.development -> false");
                    return null;
                }
                return chain.proceed();
            });
            Log.i(TAG, "SystemProperties.set hook registered");
        } catch (Throwable t) {
            Log.w(TAG, "SystemProperties.set hook failed: " + t);
        }
    }

private static void hookEnabler(XposedModule mod, ClassLoader cl) {
        try {
            Class<?> clz = Class.forName(
                    "com.android.settingslib.development.DevelopmentSettingsEnabler", false, cl);
            Method m = Reflect.findMethodExact(clz, "isDevelopmentSettingsEnabled",
                    Context.class);
            if (m == null) {
                Log.w(TAG, "isDevelopmentSettingsEnabled not found");
                return;
            }
            mod.hook(m).intercept(chain -> {
                if (ConfigManager.isEnabled(HookConfig.KEY_DEV_OPTIONS)) {
                    return true;
                }
                return chain.proceed();
            });
            Log.i(TAG, "isDevelopmentSettingsEnabled hook registered");
        } catch (Throwable t) {
            Log.w(TAG, "isDevelopmentSettingsEnabled hook failed: " + t);
        }
    }

private static void hookSpecialAdbDebug(XposedModule mod, ClassLoader cl) {
        try {
            Class<?> clz = Class.forName("android.provider.Settings$Global", false, cl);
            Method m = Reflect.findMethodExact(clz, "getInt",
                    ContentResolver.class, String.class, int.class);
            if (m == null) {
                Log.w(TAG, "Settings.Global.getInt not found");
                return;
            }
            mod.hook(m).intercept(chain -> {
                if (KEY_SPECIAL_ADB.equals(chain.getArg(1))
                        && ConfigManager.isEnabled(HookConfig.KEY_DEV_OPTIONS)) {
                    return 1;
                }
                return chain.proceed();
            });
            Log.i(TAG, "Settings.Global.getInt hook registered");
        } catch (Throwable t) {
            Log.w(TAG, "Settings.Global.getInt hook failed: " + t);
        }
    }
}
