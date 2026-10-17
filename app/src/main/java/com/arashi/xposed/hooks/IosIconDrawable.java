package com.arashi.xposed.hooks;

import android.graphics.*;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

/** Keeps the app artwork and changes only its outer mask. */
final class IosIconDrawable extends Drawable implements Drawable.Callback {
    private static final Path MASK = mask();
    private final Drawable original;
    private final Drawable artwork;
    private boolean drawing;
    private final Paint composite = new Paint(Paint.ANTI_ALIAS_FLAG);

    IosIconDrawable(Drawable original, String ignoredIdentity) {
        this.original = original;
        Drawable source = original;
        // The dock's drawable already clips to the old corner shape. Remove
        // that wrapper so it cannot cut off the new mask from inside.
        if (source.getClass().getName().equals(
                "com.zuoyebang.iot.pad.launcher.mysubject.presentation.view.dock.j")) {
            try {
                java.lang.reflect.Field field = source.getClass().getDeclaredField("a");
                field.setAccessible(true);
                Object inner = field.get(source);
                if (inner instanceof Drawable) source = (Drawable) inner;
            } catch (ReflectiveOperationException ignored) {}
        }
        artwork = source;
        original.setCallback(this);
    }

    Drawable original() { return original; }

    private static Path mask() {
        Path path = new Path();
        // Superellipse with exponent 5: smooth iOS-like continuous corners.
        for (int i = 0; i <= 128; i++) {
            double angle = i * Math.PI * 2 / 128;
            double x = Math.cos(angle), y = Math.sin(angle);
            float px = (float) (50 + 50 * Math.copySign(Math.pow(Math.abs(x), .4), x));
            float py = (float) (50 + 50 * Math.copySign(Math.pow(Math.abs(y), .4), y));
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }
        path.close();
        return path;
    }

    @Override public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) return;
        int save = canvas.saveLayer(new RectF(bounds), composite);
        drawing = true;
        try {
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(bounds.width() / 100f, bounds.height() / 100f);
            canvas.clipPath(MASK);
            if (artwork instanceof AdaptiveIconDrawable) {
                AdaptiveIconDrawable adaptive = (AdaptiveIconDrawable) artwork;
                drawLayer(canvas, adaptive.getBackground(), 0, 100);
                drawLayer(canvas, adaptive.getForeground(), -25, 125);
            } else {
                drawLayer(canvas, artwork, 0, 100);
            }
        } finally {
            drawing = false;
            canvas.restoreToCount(save);
        }
    }
    private static void drawLayer(Canvas canvas, Drawable layer, int start, int end) {
        if (layer == null) return;
        Rect old = new Rect(layer.getBounds());
        try { layer.setBounds(start, start, end, end); layer.draw(canvas); }
        finally { layer.setBounds(old); }
    }
    @Override public int getIntrinsicWidth() { return original.getIntrinsicWidth(); }
    @Override public int getIntrinsicHeight() { return original.getIntrinsicHeight(); }
    @Override public void setAlpha(int alpha) { composite.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter value) { composite.setColorFilter(value); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public void invalidateDrawable(Drawable who) { if (!drawing) invalidateSelf(); }
    @Override public void scheduleDrawable(Drawable who, Runnable task, long when) { scheduleSelf(task, when); }
    @Override public void unscheduleDrawable(Drawable who, Runnable task) { unscheduleSelf(task); }
}
