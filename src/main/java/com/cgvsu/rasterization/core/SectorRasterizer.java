package com.cgvsu.rasterization.core;

/**
 * Растеризация сектора окружности.
 *
 * Идея: ограничить фигуру прямоугольником (bounding box), пробежать по всем
 * его пикселям и закрасить только те, что лежат внутри исходной фигуры.
 * Пиксель принадлежит сектору, если он внутри круга и лежит между
 * лучами OA и OB (проверка через знаки векторных произведений).
 *
 * Углы отсчитываются в градусах против часовой стрелки от направления "вправо",
 * как на обычной математической плоскости (на экране ось y направлена вниз,
 * поэтому внутри алгоритма знак y переворачивается).
 */
public final class SectorRasterizer {

    /** Допуск для сравнения с нулём: пиксели ровно на луче должны попадать в сектор. */
    private static final double EPS = 1e-9;

    private SectorRasterizer() {
    }

    /**
     * Заливает сектор одним цветом.
     *
     * @param xc       x центра
     * @param yc       y центра
     * @param r        радиус в пикселях
     * @param startDeg угол луча OA в градусах
     * @param sweepDeg раствор сектора в градусах (от OA к OB против часовой стрелки), не более 180
     * @param color    цвет заливки
     */
    public static void fillSector(PixelSink sink, int xc, int yc, int r,
                                  double startDeg, double sweepDeg, int color) {
        if (sweepDeg > 180) {
            throw new IllegalArgumentException("Пока поддерживаются секторы не больше 180 градусов");
        }
        double ax = Math.cos(Math.toRadians(startDeg));
        double ay = Math.sin(Math.toRadians(startDeg));
        double bx = Math.cos(Math.toRadians(startDeg + sweepDeg));
        double by = Math.sin(Math.toRadians(startDeg + sweepDeg));

        for (int x = xc - r; x <= xc + r; x++) {
            for (int y = yc - r; y <= yc + r; y++) {
                if (!insideCircle(x, y, xc, yc, r)) {
                    continue;
                }
                // Вектор OP в математической системе координат (y вверх).
                double vx = x - xc;
                double vy = -(y - yc);
                if (insideSmallSector(ax, ay, bx, by, vx, vy)) {
                    sink.setPixel(x, y, color);
                }
            }
        }
    }

    /** Z-компонента векторного произведения (ax, ay) x (bx, by). */
    static double cross(double ax, double ay, double bx, double by) {
        return ax * by - ay * bx;
    }

    /**
     * Лежит ли вектор v между OA и OB, если поворот от A к B против часовой
     * стрелки не превышает 180 градусов.
     * M = A x V >= 0 (V слева от A) и N = V x B >= 0 (B слева от V).
     */
    static boolean insideSmallSector(double ax, double ay, double bx, double by,
                                     double vx, double vy) {
        double m = cross(ax, ay, vx, vy);
        double n = cross(vx, vy, bx, by);
        return m >= -EPS && n >= -EPS;
    }

    /** Условие (x - xc)^2 + (y - yc)^2 <= r^2 в целых числах (без sqrt и ошибок округления). */
    static boolean insideCircle(int x, int y, int xc, int yc, int r) {
        long dx = x - xc;
        long dy = y - yc;
        return dx * dx + dy * dy <= (long) r * r;
    }
}