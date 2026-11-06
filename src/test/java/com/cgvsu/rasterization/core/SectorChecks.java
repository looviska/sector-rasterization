package com.cgvsu.rasterization.core;

import java.util.Random;

/**
 * Простой набор проверок без внешних библиотек (JUnit недоступен офлайн).
 * Запуск: scripts/check.sh
 */
public final class SectorChecks {

    private static int passed = 0;
    private static int failed = 0;

    /** Приёмник, запоминающий закрашенные пиксели в квадрате вокруг центра. */
    static final class Grid implements PixelSink {
        final int xc, yc, r;
        final int[][] cells; // 0 = не закрашен, иначе argb | 0 (хранится цвет)
        final boolean[][] painted;
        int writes = 0;

        Grid(int xc, int yc, int r) {
            this.xc = xc;
            this.yc = yc;
            this.r = r;
            int size = 2 * r + 3;
            cells = new int[size][size];
            painted = new boolean[size][size];
        }

        @Override
        public void setPixel(int x, int y, int argb) {
            int gx = x - xc + r + 1;
            int gy = y - yc + r + 1;
            if (gx < 0 || gy < 0 || gx >= painted.length || gy >= painted.length) {
                throw new AssertionError("Пиксель вне ожидаемой области: " + x + "," + y);
            }
            painted[gx][gy] = true;
            cells[gx][gy] = argb;
            writes++;
        }

        boolean at(int x, int y) {
            return painted[x - xc + r + 1][y - yc + r + 1];
        }

        int colorAt(int x, int y) {
            return cells[x - xc + r + 1][y - yc + r + 1];
        }

        int count() {
            int n = 0;
            for (boolean[] col : painted) {
                for (boolean b : col) {
                    if (b) n++;
                }
            }
            return n;
        }
    }

    static void check(boolean cond, String name) {
        if (cond) {
            passed++;
            System.out.println("  ok    " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    static boolean inDisk(int dx, int dy, int r) {
        return (long) dx * dx + (long) dy * dy <= (long) r * r;
    }

    /** Независимый эталон: сравнение углов через atan2. Возвращает null на границе луча (неоднозначно). */
    static Boolean referenceInside(int dx, int dyScreen, double startDeg, double sweepDeg) {
        if (dx == 0 && dyScreen == 0) {
            return true; // центр принадлежит любому непустому сектору
        }
        if (sweepDeg >= 360) {
            return true;
        }
        double ang = Math.toDegrees(Math.atan2(-dyScreen, dx)); // y вверх
        double rel = ((ang - startDeg) % 360 + 360) % 360;
        double tol = 1e-6;
        if (rel < tol || rel > 360 - tol || Math.abs(rel - sweepDeg) < tol) {
            return null; // пиксель на луче
        }
        return rel < sweepDeg;
    }

    static void testFullCircleMatchesDefinition() {
        System.out.println("Полный круг");
        int r = 40;
        Grid g = new Grid(100, 100, r);
        SectorRasterizer.fillSector(g, 100, 100, r, 0, 360, 0xFFFF0000);
        boolean exact = true;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                if (g.at(100 + dx, 100 + dy) != inDisk(dx, dy, r)) exact = false;
            }
        }
        check(exact, "закрашены ровно пиксели с dx^2 + dy^2 <= r^2");
        double area = Math.PI * r * r;
        check(Math.abs(g.count() - area) / area < 0.01, "площадь отличается от pi*r^2 менее чем на 1%");
        check(g.writes == g.count(), "каждый пиксель записан ровно один раз");
    }

    static void testQuadrant() {
        System.out.println("Четверть круга 0..90");
        int r = 30;
        Grid g = new Grid(50, 50, r);
        SectorRasterizer.fillSector(g, 50, 50, r, 0, 90, 0xFF00FF00);
        boolean allInside = true, allCovered = true;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                if (!inDisk(dx, dy, r)) continue;
                boolean upperRight = dx >= 0 && -dy >= 0; // dy экранный
                if (g.at(50 + dx, 50 + dy) != upperRight) {
                    allInside = false;
                    allCovered = false;
                }
            }
        }
        check(allInside && allCovered, "закрашена ровно правая верхняя четверть вместе с границами");
        check(g.at(50, 50), "центр закрашен");
    }

    static void testHalves() {
        System.out.println("Полукруги и дополнения");
        int r = 25;
        int[] sweeps = {30, 90, 120, 179, 180, 181, 270, 350};
        for (int sweep : sweeps) {
            double start = 37;
            Grid a = new Grid(60, 60, r);
            Grid b = new Grid(60, 60, r);
            SectorRasterizer.fillSector(a, 60, 60, r, start, sweep, 0xFF000000);
            SectorRasterizer.fillSector(b, 60, 60, r, start + sweep, 360 - sweep, 0xFF000000);
            boolean union = true;
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    boolean covered = a.at(60 + dx, 60 + dy) || b.at(60 + dx, 60 + dy);
                    if (covered != inDisk(dx, dy, r)) union = false;
                }
            }
            check(union, "сектор " + sweep + " + дополнение " + (360 - sweep) + " покрывают весь круг");
        }
    }

    static void testDegenerate() {
        System.out.println("Вырожденные случаи");
        Grid g = new Grid(20, 20, 10);
        SectorRasterizer.fillSector(g, 20, 20, 10, 0, 0, 0xFFFFFFFF);
        check(g.count() == 0, "раствор 0 - ничего не рисуется");
        SectorRasterizer.fillSector(g, 20, 20, 10, 0, -45, 0xFFFFFFFF);
        check(g.count() == 0, "отрицательный раствор - ничего не рисуется");
        SectorRasterizer.fillSector(g, 20, 20, 10, 123, 720, 0xFFFFFFFF);
        Grid full = new Grid(20, 20, 10);
        SectorRasterizer.fillSector(full, 20, 20, 10, 0, 360, 0xFFFFFFFF);
        check(g.count() == full.count(), "раствор 720 равен полному кругу");
        Grid zeroR = new Grid(20, 20, 1);
        SectorRasterizer.fillSector(zeroR, 20, 20, 0, 0, 90, 0xFFFF00FF);
        check(zeroR.count() == 1 && zeroR.at(20, 20), "радиус 0 - один пиксель центра");
        Grid neg = new Grid(20, 20, 1);
        SectorRasterizer.fillSector(neg, 20, 20, -5, 0, 90, 0xFFFFFFFF);
        check(neg.count() == 0, "отрицательный радиус - ничего не рисуется");
    }

    static void testAgainstReference() {
        System.out.println("Сравнение с эталоном на atan2 (случайные секторы)");
        Random rnd = new Random(42);
        int sectors = 400;
        int mismatches = 0;
        int compared = 0;
        for (int i = 0; i < sectors; i++) {
            int r = 3 + rnd.nextInt(40);
            double start = rnd.nextDouble() * 720 - 360;
            double sweep = 1 + rnd.nextDouble() * 358;
            Grid g = new Grid(80, 80, r);
            SectorRasterizer.fillSector(g, 80, 80, r, start, sweep, 0xFF123456);
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    boolean painted = g.at(80 + dx, 80 + dy);
                    if (!inDisk(dx, dy, r)) {
                        if (painted) mismatches++;
                        continue;
                    }
                    Boolean ref = referenceInside(dx, dy, start, sweep);
                    if (ref == null) continue;
                    compared++;
                    if (painted != ref) mismatches++;
                }
            }
        }
        System.out.println("        сравнено пикселей: " + compared);
        check(mismatches == 0, "расхождений с эталоном: " + mismatches);
    }

    static void testInterpolation() {
        System.out.println("Интерполяция цвета");
        int r = 50;
        int cc = ColorUtils.rgb(255, 0, 0);
        int ce = ColorUtils.rgb(0, 0, 255);
        Grid g = new Grid(100, 100, r);
        SectorRasterizer.fillSector(g, 100, 100, r, 0, 360, cc, ce);
        check(g.colorAt(100, 100) == cc, "в центре цвет центра");
        check(g.colorAt(150, 100) == ce, "на дуге (расстояние r) цвет края");
        int mid = g.colorAt(125, 100); // t = 0.5
        check(Math.abs(ColorUtils.red(mid) - 128) <= 1 && Math.abs(ColorUtils.blue(mid) - 128) <= 1
                && ColorUtils.green(mid) == 0, "на половине радиуса цвет посередине");
        boolean monotone = true;
        int prevRed = 256;
        for (int x = 100; x <= 150; x++) {
            int red = ColorUtils.red(g.colorAt(x, 100));
            if (red > prevRed) monotone = false;
            prevRed = red;
        }
        check(monotone, "красный канал монотонно убывает вдоль радиуса");
        boolean sameDist = g.colorAt(100 + 30, 100) == g.colorAt(100, 100 + 30)
                && g.colorAt(100 - 30, 100) == g.colorAt(100, 100 - 30);
        check(sameDist, "цвет зависит только от расстояния до центра");
    }

    static void testColorUtils() {
        System.out.println("ColorUtils");
        int c0 = ColorUtils.argb(0, 10, 20, 30);
        int c1 = ColorUtils.argb(200, 110, 120, 130);
        check(ColorUtils.lerp(c0, c1, 0) == c0, "lerp(t=0) = c0");
        check(ColorUtils.lerp(c0, c1, 1) == c1, "lerp(t=1) = c1");
        check(ColorUtils.lerp(c0, c1, -3) == c0 && ColorUtils.lerp(c0, c1, 7) == c1, "t ограничивается [0, 1]");
        int m = ColorUtils.lerp(c0, c1, 0.5);
        check(ColorUtils.alpha(m) == 100 && ColorUtils.red(m) == 60
                && ColorUtils.green(m) == 70 && ColorUtils.blue(m) == 80, "lerp(t=0.5) - среднее по каналам");
        int p = ColorUtils.argb(1, 2, 3, 4);
        check(ColorUtils.alpha(p) == 1 && ColorUtils.red(p) == 2
                && ColorUtils.green(p) == 3 && ColorUtils.blue(p) == 4, "упаковка и распаковка каналов");
    }

    public static void main(String[] args) {
        testColorUtils();
        testFullCircleMatchesDefinition();
        testQuadrant();
        testHalves();
        testDegenerate();
        testAgainstReference();
        testInterpolation();
        System.out.println();
        System.out.println("Итого: пройдено " + passed + ", провалено " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}