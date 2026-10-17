package com.arashi.xposed.hooks;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.AdaptiveIconDrawable;
import java.util.Locale;

/** Vector icon styling confined to the launcher's app icons. */
final class IosIconDrawable extends Drawable {
    private final Drawable original;
    private final String kind;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int opacity = 255;
    private ColorFilter filter;

    IosIconDrawable(Drawable original, String identity) {
        this.original = original;
        String name = identity == null ? "" : identity.toLowerCase(Locale.ROOT);
        if (name.contains("settings")) kind = "settings";
        else if (name.contains("calculator")) kind = "calculator";
        else if (name.contains("camera")) kind = "camera";
        else if (name.contains("gallery") || name.contains("photos")) kind = "photos";
        else if (name.contains("browser") || name.contains("zplearningwebsite")) kind = "browser";
        else if (name.contains("filemanager") || name.contains("documentsui")) kind = "files";
        else if (name.contains("appstore")) kind = "store";
        else kind = "original";
    }

    @Override public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) return;
        Paint composite = new Paint();
        composite.setAlpha(opacity);
        composite.setColorFilter(filter);
        int save = canvas.saveLayer(new RectF(bounds), composite);
        canvas.translate(bounds.left, bounds.top);
        canvas.scale(bounds.width() / 100f, bounds.height() / 100f);
        Path mask = new Path();
        // Smooth, continuous corners rather than a circular adaptive-icon mask.
        mask.moveTo(50, 0); mask.cubicTo(8, 0, 0, 8, 0, 50);
        mask.cubicTo(0, 92, 8, 100, 50, 100);
        mask.cubicTo(92, 100, 100, 92, 100, 50);
        mask.cubicTo(100, 8, 92, 0, 50, 0); mask.close();
        canvas.clipPath(mask);
        paint.setAlpha(255); paint.setColorFilter(null);
        paint.setStyle(Paint.Style.FILL);
        int top = Color.rgb(250, 250, 252), bottom = Color.rgb(235, 236, 241);
        if (kind.equals("browser") || kind.equals("files") || kind.equals("store")) {
            top = 0xff35b6ff; bottom = 0xff0867ef;
        } else if (kind.equals("settings") || kind.equals("camera")) {
            top = 0xffdfe4eb; bottom = 0xffa4adbb;
        } else if (kind.equals("calculator")) { top = 0xff55565c; bottom = 0xff292a30; }
        paint.setShader(new LinearGradient(0, 0, 0, 100, top, bottom, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 100, 100, paint); paint.setShader(null);
        paint.setColor(Color.WHITE);
        switch (kind) {
            case "settings":
                paint.setColor(0xff606975);
                for (int i = 0; i < 12; i++) {
                    canvas.save(); canvas.rotate(i * 30, 50, 50);
                    canvas.drawRoundRect(45, 17, 55, 35, 2, 2, paint); canvas.restore();
                }
                canvas.drawCircle(50, 50, 27, paint);
                paint.setColor(0xffcbd2dc); canvas.drawCircle(50, 50, 17, paint);
                paint.setColor(0xff606975); canvas.drawCircle(50, 50, 10, paint); break;
            case "browser":
                canvas.drawCircle(50, 50, 33, paint);
                paint.setColor(0xff38a9ef); canvas.drawCircle(50, 50, 29, paint);
                for (int i = 0; i < 12; i++) {
                    canvas.save(); canvas.rotate(i * 30, 50, 50); paint.setColor(Color.WHITE);
                    canvas.drawRect(49, 23, 51, 28, paint); canvas.restore();
                }
                Path needle = new Path(); needle.moveTo(65, 29); needle.lineTo(53, 54); needle.lineTo(46, 47); needle.close();
                paint.setColor(0xffff4b50); canvas.drawPath(needle, paint);
                needle.reset(); needle.moveTo(35, 71); needle.lineTo(53, 54); needle.lineTo(46, 47); needle.close();
                paint.setColor(Color.WHITE); canvas.drawPath(needle, paint); break;
            case "camera":
                paint.setColor(0xff373c43); canvas.drawRoundRect(18, 33, 82, 73, 9, 9, paint);
                canvas.drawRoundRect(32, 25, 52, 39, 4, 4, paint);
                paint.setColor(0xffb8c0cb); canvas.drawCircle(50, 52, 17, paint);
                paint.setColor(0xff202b3b); canvas.drawCircle(50, 52, 12, paint); break;
            case "files":
                paint.setColor(0xffd8edff); canvas.drawRoundRect(18, 27, 48, 45, 5, 5, paint);
                canvas.drawRoundRect(18, 35, 82, 75, 7, 7, paint);
                paint.setColor(Color.WHITE); canvas.drawRoundRect(18, 41, 82, 77, 7, 7, paint); break;
            case "store":
                paint.setStrokeWidth(7); paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(33, 72, 61, 26, paint); canvas.drawLine(43, 26, 70, 72, paint);
                canvas.drawLine(23, 61, 77, 61, paint); break;
            case "calculator":
                for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
                    paint.setColor(col == 2 ? 0xffff9e24 : 0xffd8d9dd);
                    canvas.drawCircle(29 + col * 21, 43 + row * 18, 7, paint);
                }
                paint.setColor(0xffb2b8b0); canvas.drawRoundRect(20, 18, 80, 30, 3, 3, paint); break;
            case "photos":
                for (int i = 0; i < 8; i++) {
                    canvas.save(); canvas.rotate(i * 45, 50, 50);
                    paint.setColor(Color.HSVToColor(new float[]{i * 45, .72f, .98f}));
                    canvas.drawOval(39, 15, 61, 54, paint); canvas.restore();
                }
                break;
            default:
                if (original instanceof AdaptiveIconDrawable) {
                    AdaptiveIconDrawable adaptive = (AdaptiveIconDrawable) original;
                    drawLayer(canvas, adaptive.getBackground(), 0, 100);
                    drawLayer(canvas, adaptive.getForeground(), -25, 125);
                } else {
                    drawLayer(canvas, original, 9, 91);
                }
        }
        canvas.restoreToCount(save);
    }
    private static void drawLayer(Canvas canvas, Drawable layer, int start, int end) {
        if (layer == null) return;
        Rect old = new Rect(layer.getBounds());
        try { layer.setBounds(start, start, end, end); layer.draw(canvas); }
        finally { layer.setBounds(old); }
    }
    @Override public int getIntrinsicWidth() { return original.getIntrinsicWidth() > 0 ? original.getIntrinsicWidth() : 100; }
    @Override public int getIntrinsicHeight() { return original.getIntrinsicHeight() > 0 ? original.getIntrinsicHeight() : 100; }
    @Override public void setAlpha(int alpha) { opacity = alpha; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter value) { filter = value; invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
