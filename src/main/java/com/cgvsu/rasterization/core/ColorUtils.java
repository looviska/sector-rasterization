package com.cgvsu.rasterization.core;

/** Вспомогательные функции для работы с цветом в формате ARGB (int). */
public final class ColorUtils {

    private ColorUtils() {
    }

    public static int argb(int a, int r, int g, int b) {
        return (clamp255(a) << 24) | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
    }

    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    public static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    public static int red(int argb) {
        return (argb >>> 16) & 0xFF;
    }

    public static int green(int argb) {
        return (argb >>> 8) & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    /**
     * Линейная интерполяция цвета: c = c0 + t * (c1 - c0) отдельно для
     * каналов a, r, g, b. Параметр t ограничивается отрезком [0, 1].
     */
    public static int lerp(int c0, int c1, double t) {
        if (t <= 0) {
            return c0;
        }
        if (t >= 1) {
            return c1;
        }
        return argb(
                lerpChannel(alpha(c0), alpha(c1), t),
                lerpChannel(red(c0), red(c1), t),
                lerpChannel(green(c0), green(c1), t),
                lerpChannel(blue(c0), blue(c1), t));
    }

    private static int lerpChannel(int v0, int v1, double t) {
        return (int) Math.round(v0 + t * (v1 - v0));
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }
}