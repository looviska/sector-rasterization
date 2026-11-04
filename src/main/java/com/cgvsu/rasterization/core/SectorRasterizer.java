package com.cgvsu.rasterization.core;

/**
 * Растеризация сектора окружности.
 *
 * Идея: ограничить фигуру прямоугольником (bounding box), пробежать по всем
 * его пикселям и закрасить только те, что лежат внутри исходной фигуры.
 */
public final class SectorRasterizer {

    private SectorRasterizer() {
    }

    /**
     * Заливает круг (пока без учёта углов сектора) одним цветом.
     *
     * @param xc    x центра
     * @param yc    y центра
     * @param r     радиус в пикселях
     * @param color цвет заливки
     */
    public static void fillSector(PixelSink sink, int xc, int yc, int r, int color) {
        for (int x = xc - r; x <= xc + r; x++) {
            for (int y = yc - r; y <= yc + r; y++) {
                if (insideCircle(x, y, xc, yc, r)) {
                    sink.setPixel(x, y, color);
                }
            }
        }
    }

    /** Условие (x - xc)^2 + (y - yc)^2 <= r^2 в целых числах (без sqrt и ошибок округления). */
    static boolean insideCircle(int x, int y, int xc, int yc, int r) {
        long dx = x - xc;
        long dy = y - yc;
        return dx * dx + dy * dy <= (long) r * r;
    }
}