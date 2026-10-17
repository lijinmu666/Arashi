package com.arashi.xposed.hooks;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.util.Log;
import com.arashi.xposed.ConfigManager;
import com.arashi.xposed.HookConfig;
import com.arashi.xposed.Reflect;
import io.github.libxposed.api.XposedModule;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class LauncherIconHooks {
    private static final java.util.Map<Drawable, String> IDENTITIES = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static final ThreadLocal<String> ICON_PACKAGE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> STYLING = new ThreadLocal<>();
    private static final String DOCK = "com.zuoyebang.iot.pad.launcher.mysubject.presentation.view.dock.DockView";
    private static final String PRESET = "com.zuoyebang.iot.pad.launcher.preset.view.PresetAppAdaptiveView";
    private LauncherIconHooks() {}

    public static void hookLauncher(XposedModule module, ClassLoader loader) {
        for (String name : new String[]{"setImageDrawable", "setImageBitmap", "setImageResource"}) {
            try {
                Class<?> arg = name.equals("setImageDrawable") ? Drawable.class
                        : name.equals("setImageBitmap") ? Bitmap.class : int.class;
                Method method = ImageView.class.getDeclaredMethod(name, arg);
                module.hook(method).intercept(chain -> {
                    Object result = chain.proceed();
                    if (Boolean.TRUE.equals(STYLING.get()) || !ConfigManager.isEnabled(HookConfig.KEY_IOS_ICONS)) return result;
                    ImageView image = (ImageView) chain.getThisObject();
                    try {
                        STYLING.set(true);
                        styleImage(image);
                    } catch (Throwable error) {
                        Log.w("[Arashi-Icons]", "Unable to style launcher icon", error);
                    } finally { STYLING.remove(); }
                    return result;
                });
            } catch (Throwable error) { Log.w("[Arashi-Icons]", "Image hook unavailable: " + name, error); }
        }
        try {
            Class<?> provider = Class.forName("g8.g0", false, loader);
            Method provide = Reflect.findMethodExact(provider, "a", String.class, int.class, java.util.function.Supplier.class);
            if (provide != null) module.hook(provide).intercept(chain -> {
                Object result = chain.proceed();
                if (result instanceof Drawable) IDENTITIES.put((Drawable) result, (String) chain.getArg(0));
                return result;
            });
        } catch (Throwable error) { Log.w("[Arashi-Icons]", "Icon identity hook unavailable", error); }
        try {
            Class<?> factory = Class.forName("g8.d", false, loader);
            Method create = Reflect.findMethodExact(factory, "a", Drawable.class,
                    Class.forName("androidx.appcompat.widget.h3", false, loader));
            if (create != null) module.hook(create).intercept(chain -> {
                String previous = ICON_PACKAGE.get();
                ICON_PACKAGE.set(IDENTITIES.get((Drawable) chain.getArg(0)));
                try { return chain.proceed(); }
                finally {
                    if (previous == null) ICON_PACKAGE.remove(); else ICON_PACKAGE.set(previous);
                }
            });
            Method render = Reflect.findMethodExact(factory, "d", Drawable.class, float.class, int.class);
            if (render != null) module.hook(render).intercept(chain -> {
                Object result = chain.proceed();
                if (!ConfigManager.isEnabled(HookConfig.KEY_IOS_ICONS) || !(result instanceof Bitmap)
                        || (((Integer) chain.getArg(2)) != 0 && ((Integer) chain.getArg(2)) != 2)) return result;
                Bitmap bitmap = (Bitmap) result;
                Drawable source = (Drawable) chain.getArg(0);
                if (source == null) return result;
                try {
                    Bitmap styled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    IosIconDrawable icon = new IosIconDrawable(source, ICON_PACKAGE.get());
                    icon.setBounds(0, 0, styled.getWidth(), styled.getHeight());
                    icon.draw(new Canvas(styled));
                    return styled;
                } catch (RuntimeException error) {
                    Log.w("[Arashi-Icons]", "Unable to render styled icon", error);
                    return result;
                }
            });
        } catch (Throwable error) { Log.w("[Arashi-Icons]", "Launcher icon factory unavailable", error); }
    }

    private static void styleImage(ImageView image) throws Exception {
        Drawable drawable = image.getDrawable();
        if (drawable == null || drawable instanceof IosIconDrawable) return;
        ViewParent parent = image.getParent();
        for (int depth = 0; parent instanceof View && depth < 8; depth++, parent = parent.getParent()) {
            String type = parent.getClass().getName();
            String identity = null;
            if (PRESET.equals(type)) {
                if (field(parent, "bgKousuanPk") != image) return;
                Object model = field(parent, "appInfoModel");
                if (model != null) identity = (String) field(model, "f30189a");
            } else if (DOCK.equals(type)) {
                if (field(parent, "imageView") != image) return;
                Object element = Reflect.callMethod(parent, "getDockElement");
                if (element != null) identity = (String) Reflect.callMethod(element, "getPackageName");
            } else continue;
            image.setImageDrawable(new IosIconDrawable(drawable, identity));
            return;
        }
    }
    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
