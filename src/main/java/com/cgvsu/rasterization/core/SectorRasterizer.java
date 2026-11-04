package com.cgvsu.rasterization.core;

/**
 * Растеризация сектора окружности.
 *
 * Идея: ограничить фигуру прямоугольником (bounding box) и закрасить только те
 * пиксели, что лежат внутри исходной фигуры. Чтобы не проверять лишние пиксели
 * вне круга, используется scanline: для каждой строки считаются границы хорды.
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
     * @see #fillSector(PixelSink, int, int, int, double, double, int, int)
     */
    public static void fillSector(PixelSink sink, int xc, int yc, int r,
                                  double startDeg, double sweepDeg, int color) {
        fillSector(sink, xc, yc, r, startDeg, sweepDeg, color, color);
    }

    /**
     * Заливает сектор с линейной интерполяцией цвета от центра к дуге:
     * c = centerColor + t * (edgeColor - centerColor), где t = dist / r.
     *
     * @param xc          x центра
     * @param yc          y центра
     * @param r           радиус в пикселях
     * @param startDeg    угол луча OA в градусах
     * @param sweepDeg    раствор сектора в градусах (от OA к OB против часовой стрелки).
     *                    Значение {@code <= 0} - ничего не рисуется, {@code >= 360} - полный круг
     * @param centerColor цвет в центре круга (ARGB)
     * @param edgeColor   цвет на дуге окружности (ARGB)
     */
    public static void fillSector(PixelSink sink, int xc, int yc, int r,
                                  double startDeg, double sweepDeg,
                                  int centerColor, int edgeColor) {
        if (sweepDeg <= 0 || r < 0) {
            return;
        }
        double ax = Math.cos(Math.toRadians(startDeg));
        double ay = Math.sin(Math.toRadians(startDeg));
        double bx = Math.cos(Math.toRadians(startDeg + sweepDeg));
        double by = Math.sin(Math.toRadians(startDeg + sweepDeg));

        // Идея scanline: главная ось - y. Для каждой строки сразу известны границы
        // хорды круга, поэтому пиксели вне круга даже не проверяются.
        for (int y = yc - r; y <= yc + r; y++) {
            int half = halfChord(y - yc, r);
            for (int x = xc - half; x <= xc + half; x++) {
                // Вектор OP в математической системе координат (y вверх).
                double vx = x - xc;
                double vy = -(y - yc);
                if (insideSector(sweepDeg, ax, ay, bx, by, vx, vy)) {
                    sink.setPixel(x, y, colorAt(x, y, xc, yc, r, centerColor, edgeColor));
                }
            }
        }
    }

    /**
     * Половина длины хорды круга на расстоянии dy от центра:
     * наибольшее целое h, для которого h^2 + dy^2 &lt;= r^2.
     * Считается в целых числах: sqrt даёт приближение, затем оно уточняется.
     */
    static int halfChord(int dy, int r) {
        long n = (long) r * r - (long) dy * dy;
        if (n < 0) {
            return -1; // строка вне круга: цикл по x не выполнится
        }
        long h = (long) Math.sqrt((double) n);
        while (h * h > n) {
            h--;
        }
        while ((h + 1) * (h + 1) <= n) {
            h++;
        }
        return (int) h;
    }

    /**
     * Цвет пикселя при линейной интерполяции по расстоянию от центра.
     * Зависимость линейная, поэтому доля пройденного пути t = dist / r
     * применяется одинаково ко всем каналам цвета.
     */
    static int colorAt(int x, int y, int xc, int yc, int r, int centerColor, int edgeColor) {
        if (r == 0) {
            return centerColor;
        }
        double dist = Math.hypot(x - xc, y - yc);
        return ColorUtils.lerp(centerColor, edgeColor, dist / r);
    }

    /** Z-компонента векторного произведения (ax, ay) x (bx, by). */
    static double cross(double ax, double ay, double bx, double by) {
        return ax * by - ay * bx;
    }

    /**
     * Принадлежность вектора v сектору с раствором sweepDeg.
     * <ul>
     *   <li>sweep &gt;= 360: сектор - весь круг, угол не проверяется;</li>
     *   <li>sweep &lt;= 180: прямая проверка между OA и OB;</li>
     *   <li>sweep &gt; 180: векторные произведения "между" работают только для углов
     *       меньше развёрнутого, поэтому проверяем дополнение: точка принадлежит
     *       сектору, если она НЕ лежит строго внутри сектора от OB к OA
     *       (его раствор 360 - sweep &lt; 180). Точки на лучах остаются в секторе.</li>
     * </ul>
     */
    static boolean insideSector(double sweepDeg, double ax, double ay, double bx, double by,
                                double vx, double vy) {
        if (sweepDeg >= 360) {
            return true;
        }
        if (sweepDeg <= 180) {
            return insideSmallSector(ax, ay, bx, by, vx, vy);
        }
        double m = cross(bx, by, vx, vy);   // V слева от B
        double n = cross(vx, vy, ax, ay);   // A слева от V
        boolean strictlyInComplement = m > EPS && n > EPS;
        return !strictlyInComplement;
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
}